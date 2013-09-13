package com.duggan.workflow.server.helper.jbpm;


import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SystemEventListenerFactory;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.definition.process.Process;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.workitem.email.EmailWorkItemHandler;
import org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.Task;
import org.jbpm.task.event.TaskEventListener;
import org.jbpm.task.event.entity.TaskUserEvent;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;

import xtension.workitems.GenerateNotificationWorkItemHandler;
import xtension.workitems.SendMailWorkItemHandler;
import xtension.workitems.UpdateApprovalStatusWorkItemHandler;

import bitronix.tm.TransactionManagerServices;

import com.duggan.workflow.server.db.DB;
import com.duggan.workflow.server.helper.dao.DocumentDaoHelper;
import com.duggan.workflow.server.helper.email.EmailServiceHelper;
import com.duggan.workflow.server.helper.session.SessionHelper;
import com.duggan.workflow.shared.model.Actions;
import com.duggan.workflow.shared.model.Document;

/**
 * 
 * @author duggan
 *
 */
class BPMSessionManager{
		
	private TaskService service;
	
	private ThreadLocal<Map<Long, StatefulKnowledgeSession>> sessionStore = new ThreadLocal<>();
	
	private ThreadLocal<LocalTaskService> ltsStore = new ThreadLocal<>();
			
	public BPMSessionManager(){
		
		service = new TaskService(DB.getEntityManagerFactory(),
    			SystemEventListenerFactory.getSystemEventListener());
		
	}
	
	//processId - KnowledgeBase Map
	private Map<String, KnowledgeBase> processKnowledgeMap = new HashMap<>();
	
	private Logger logger = Logger.getLogger(BPMSessionManager.class);
	
	/**
	 * 
	 * @param processId (ID of the process to be initialized-- e.g invoice-process)
	 * @return
	 */
	private StatefulKnowledgeSession getSession(String processId){
		
		StatefulKnowledgeSession session = initializeSession(processId);		
		
		logger.warn("GETSESSION RETURNED SESSION: "+session.getId()+session.toString());
		return session;
	}
	
	private Map<Long, StatefulKnowledgeSession> getSessionStore(){
		Map<Long, StatefulKnowledgeSession> sessionMap = sessionStore.get();
		if(sessionMap==null){
			sessionMap = new HashMap<>();
			sessionStore.set(sessionMap);
		}
		
		return sessionMap;
	}
	
	/**
	 * 
	 * @param sessionId --Id of the session to be retrieved
	 * @return
	 */
	private StatefulKnowledgeSession getSession(Long sessionId, String processId){
		StatefulKnowledgeSession session=null;
		
		if(getSessionStore().get(sessionId)!=null){
			logger.warn("Retrieving from Store:  session ["+sessionId+"] for process: ["+processId+"]");
			session = getSessionStore().get(sessionId);
		}else{			
			session = initializeSession(sessionId, processId);
		}
		
		logger.warn("GETSESSION RETURNED SESSION: "+session.getId()+session.toString());
		return session;
	}

	private void registerWorkItemHandlers(StatefulKnowledgeSession session){
		//ConsoleLogger
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(session);
		
		//register work item handlers
		session.getWorkItemManager().registerWorkItemHandler("UpdateLocal", new UpdateApprovalStatusWorkItemHandler());
		session.getWorkItemManager().registerWorkItemHandler("GenerateSysNotification",new GenerateNotificationWorkItemHandler());
		session.getWorkItemManager().registerWorkItemHandler("ScheduleEmailNotification",new SendMailWorkItemHandler());
		
		EmailWorkItemHandler emailHandler = new EmailWorkItemHandler(
				EmailServiceHelper.getProperty("mail.smtp.host"),
				EmailServiceHelper.getProperty("mail.smtp.port"),
				EmailServiceHelper.getProperty("mail.smtp.user"),
				EmailServiceHelper.getProperty("mail.smtp.password"));
		emailHandler.getConnection().setStartTls(true);
		session.getWorkItemManager().registerWorkItemHandler("Email", emailHandler);

		LocalTaskService taskClient = getTaskClient();
		assert taskClient!=null;
		
		GenericHTWorkItemHandler taskHandler = new LocalHTWorkItemHandler(taskClient, session);
		//Only handle events related to the session that started the task being completed
		taskHandler.setOwningSessionOnly(true);
		taskHandler.connect();
		
		//GenericHTWorkItemHandler htHandler = this.createTaskHandler(session);
		session.getWorkItemManager().registerWorkItemHandler("Human Task", taskHandler);
	}

	/**
	 * Assuming each request starts/requires at most 1 KnowlegdeSession;
	 * a single LocalTaskService Object stored in a thread local should
	 * suffice;
	 * <p/>
	 * The assumption is based on the type of requests made
	 * <ul>
	 * <li>Foward for approval (1 doc, 1 process)
	 * <li>Start, Suspend, Resume, Claim, Complete etc > all these are requests
	 * related to a single task & therefore document; meaning one process and one session.
	 * </ul>
	 * <p/>
	 * This will only change if other processes (e.g. notification processes) linked
	 * to independent KnowledgeBases & therefore processes are started in code 
	 * 
	 * <p/>
	 * Also note the relationship between LocalTaskService and the StatefulSession
	 * <br/>
	 * StatefulSession > WorkItemHandler > LocalTaskService > TaskService
	 * 
	 * @return {@link LocalTaskService}
	 */
	public LocalTaskService getTaskClient(){		
		LocalTaskService lts =ltsStore.get(); 
		
		if(lts==null){
			lts = new LocalTaskService(service);	    	
			lts.addEventListener(new NotificationTaskEventListener());
			ltsStore.set(lts);
		}
		
		return lts;
	}
	
	
	private KnowledgeBase getKnowledgeBase(String processId, boolean forceReload){

		logger.info("Loading knowledgebase for process: "+processId+"; forceReload= "+forceReload);
		KnowledgeBase kbase= processKnowledgeMap.get(processId);
		if(kbase!=null && !forceReload){
			return kbase;
		}
		
		Resource changeset = ResourceFactory.newClassPathResource("approval-process-changeset.xml");
		KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent("approval-agent");
		kagent.applyChangeSet(changeset);
		
//    	KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//    	//builder.add(new ClassPathResource("invoice-approval.bpmn"), ResourceType.BPMN2);
//    	builder.add(new ClassPathResource("invoice-approval.bpmn"), ResourceType.BPMN2);
//    	builder.add(new ClassPathResource("send-email.bpmn"), ResourceType.BPMN2);
//    	builder.add(new ClassPathResource("beforetask-notification.bpmn"), ResourceType.BPMN2);
//    	builder.add(new ClassPathResource("aftertask-notification.bpmn"), ResourceType.BPMN2);
//    	
//    	kbase = builder.newKnowledgeBase();
		
		kbase = kagent.getKnowledgeBase();
    	ResourceFactory.getResourceChangeNotifierService().start();
    	ResourceFactory.getResourceChangeScannerService().start();
    	
    	Collection<org.drools.definition.process.Process> processes = kbase.getProcesses();
    	
    	for(Process process: processes){
    		processKnowledgeMap.put(process.getId(), kbase);
    	}
    	
    	return kbase;
	}
	
	private Environment getEnvironment() {
		Environment env = KnowledgeBaseFactory.newEnvironment();
    	env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, DB.getEntityManagerFactory());
    	env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
    
		return env;
	}

	/**
	 * Creates a StatefulKnowledgeSession
	 * 
	 * @return {@link StatefulKnowledgeSession}
	 * @throws MalformedURLException 
	 */
	private StatefulKnowledgeSession initializeSession(String processId) {
		
		KnowledgeBase kbase = getKnowledgeBase(processId,false);	
    	//Initializing a stateful session from JPAKnowledgeService    	
    	Environment env = getEnvironment();
    	
    	//JPAKnowledgeService.loadStatefulKnowledgeSession(id, kbase, configuration, environment)
    	StatefulKnowledgeSession session = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
    	registerWorkItemHandlers(session); 
    	
    	logger.warn(Thread.currentThread().getName()+":Initializing new session ["+session.getId()+"] for process: ["+processId+"]");
    	//Process Logger - to Provide data for querying process status
    	//:- How far a particular approval has gone
    	JPAWorkingMemoryDbLogger mlogger = new JPAWorkingMemoryDbLogger(session);
    	
    	getSessionStore().put(new Long(session.getId()), session);
    	assert getSessionStore().size()>0;
		return session;
	}

	private StatefulKnowledgeSession initializeSession(Long sessionId, String processId) {
		KnowledgeBase kbase = getKnowledgeBase(processId,false);	
    	//Initializing a stateful session from JPAKnowledgeService    	
    	Environment env = getEnvironment();
    	
    	StatefulKnowledgeSession session = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId.intValue(), kbase, null, env);
    	registerWorkItemHandlers(session); 
    	
    	logger.warn(Thread.currentThread().getName()+": Loading previous session ["+session.getId()+"] for process: ["+processId+"]");
    	
    	//Process Logger - to Provide data for querying process status
    	//:- How far a particular approval has gone
    	JPAWorkingMemoryDbLogger mlogger = new JPAWorkingMemoryDbLogger(session);
    	
    	getSessionStore().put(new Long(session.getId()), session);
    	assert getSessionStore().size()>0;
		return session;
	}

	public void disposeSessions(){
		logger.warn(Thread.currentThread().getName()+": Disposing Sessions..............");
		Map<Long, StatefulKnowledgeSession> sessionz = getSessionStore();
		
		assert sessionz!=null;
		
		Set<Long> keys = sessionz.keySet();
		
		for(Long key: keys){
			logger.warn(Thread.currentThread().getName()+
					": Disposing SessionID ["+key+"] : "+sessionz.get(key).toString());
			sessionz.get(key).dispose();
		}
		
		sessionz.clear();
		
		assert getSessionStore().size()==0;
		
		//clear ltsStore
		ltsStore.set(null);
	}

	public ProcessInstance startProcess(String processId,
			Map<String, Object> parameters){
		return startProcess(processId, parameters, null);
	}
	
	public ProcessInstance startProcess(String processId,
			Map<String, Object> parameters,
			Document summary) {
		
		StatefulKnowledgeSession session = getSession(processId);
		
		return startProcess(session, processId, parameters, summary);
	}
	
	private ProcessInstance startProcess(StatefulKnowledgeSession session,
			String processId,
			Map<String, Object> parameters){
		return startProcess(session, processId, parameters, null);
	}
	
	private ProcessInstance startProcess(StatefulKnowledgeSession session,
			String processId,
			Map<String, Object> parameters,
			Document summary){
		
		logger.info(">>> Starting process "+processId+"; Doc="+summary);
		if(summary!=null){
			summary.setSessionId(new Long(session.getId()+""));
			logger.warn("## Setting SessionId : "+summary.getSessionId());
			DocumentDaoHelper.save(summary);
		}
		
		ProcessInstance instance = session.startProcess(processId, parameters);
				
		return instance;
	}
	
	
	public void execute(long taskId, String userId, Actions action, Map<String, Object> values) {
	
		Map<String, Object> inputValues = JBPMHelper.getMappedData(getTaskClient().getTask(taskId));
		String docId = inputValues.get("documentId").toString();
		Document doc = DocumentDaoHelper.getDocument(Long.parseLong(docId));
		
		//initialize session - Each HT execution must run within an active StatefulKnowledgeSession
		getSession(doc.getSessionId(),"aftertask-notification");
		
		switch(action){
		case CLAIM:
			getTaskClient().claim(taskId, userId);
			break;
		case COMPLETE:
			complete(taskId, userId, values);			
			break;
		case DELEGATE:
			//get().getTaskClient().delegate(taskId, userId, targetUserId);
			break;
		case FORWARD:
			//get().getTaskClient().forward(taskId, userId, targetEntityId)
			break;
		case RESUME:
				getTaskClient().resume(taskId, userId);
			break;
		case REVOKE:
			//TODO: investigate what revoke actually does
			getTaskClient().release(taskId, userId);
			break;
		case START:
			getTaskClient().start(taskId, userId);
			break;
		case STOP:
			getTaskClient().stop(taskId, userId);
			break;
		case SUSPEND:
			getTaskClient().suspend(taskId, userId);
			break;
		}
	
	}	
	
	private boolean complete(long taskId, String userId, Map<String, Object> values){
		
		Map<String, Object> content = JBPMHelper.getMappedData(getTaskClient().getTask(taskId));
		content.putAll(values);
		//completing tasks is a single individuals responsibility
		//Notifications & Emails sent after task completion must reflect this
		content.put("ActorId", SessionHelper.getCurrentUser().getId());
		//sessionManager.getTaskService().completeWithResults(taskId, userId, content);
		getTaskClient().completeWithResults(taskId, userId, content);
		//sessionManager.getTaskService().complete(taskId, userId, null);
		return true;
	}
	
	/**
	 * 
	 * Class to generate automatic notifications
	 * for Human Tasks
	 *  
	 * @author duggan
	 *
	 */
	class NotificationTaskEventListener implements TaskEventListener {
		
		@Override
		public void taskStopped(TaskUserEvent event) {
		}
		
		@Override
		public void taskStarted(TaskUserEvent event) {
		}
		
		@Override
		public void taskSkipped(TaskUserEvent event) {
		}
		
		@Override
		public void taskReleased(TaskUserEvent event) {
		}
		
		@Override
		public void taskForwarded(TaskUserEvent event) {
		}
		
		@Override
		public void taskFailed(TaskUserEvent event) {
		}
		
		/**
		 * SEND BEFORE TASK NOTIFICATIONS
		 * 
		 * TODO:
		 * Note: If an exception occurs here, the error
		 * if silently handled by the caller - meaning its not propagated back
		 * to startProcess. The caller however generates the following exception 
		 * org.jbpm.workflow.instance.WorkflowRuntimeException: [invoice-approval:55 - HOD Approval:2] -- null
		 * .............
		 * caused by java.lang.NullPointerException
		 * at org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler.executeWorkItem(GenericHTWorkItemHandler.java:184)
		 * ..............
		 * 
		 * This method therefore must handle its own exceptions internally and mark the Trx for rollback
		 * in case an exception occurs to reverse the whole transaction 
		 * 
		 * An Exception Log should also be inserted into ErroLog table to assist in debugging
		 */
		@Override
		public void taskCreated(TaskUserEvent event){
			//session.startProcess(processId, parameters)
			String processId = "beforetask-notification";
			
			Task task = getTaskClient().getTask(event.getTaskId());
			
			Map<String, Object> taskData = JBPMHelper.getMappedData(task);
			
			Map<String, Object> values=null;
			Map<String,Object> newValues = new HashMap<>();
			
			Long documentId = Long.parseLong(taskData.get("documentId").toString());
			Document doc = DocumentDaoHelper.getDocument(documentId);
			Long sessionId=doc.getSessionId();
			
			if(sessionId==null){
				throw new IllegalArgumentException("SessionId must not be null!!!");
			}
			
			StatefulKnowledgeSession session = getSession(sessionId, processId);
			
			org.jbpm.process.instance.ProcessInstance instance = 
					(org.jbpm.process.instance.ProcessInstance)session.getProcessInstance(task.getTaskData().getProcessInstanceId());
			VariableScopeInstance variableScope = (VariableScopeInstance)instance.getContextInstance(VariableScope.VARIABLE_SCOPE);
			values=variableScope.getVariables();
			
			Set<String> keys = values.keySet();
			
			for(String key:keys){
				Object value = values.get(key);
				
				key = key.substring(0,1).toUpperCase()+key.substring(1);
				newValues.put(key, value);
			}
			
			newValues.put("GroupId", taskData.get("GroupId"));
			newValues.put("ActorId", taskData.get("ActorId"));
			newValues.put("Priority", taskData.get("Priority"));
			if(newValues.get("Priority")==null)
				newValues.put("Priority", values.get("Priority"));
							
			startProcess(session,processId, newValues);
		 
		} 
		
		/**
		 * SEND AFTER TASK NOTIFICATIONS
		 * 
		 * TODO:
		 * Note: If an exception occurs here, the error
		 * if silently handled by the caller - meaning its not propagated back
		 * to startProcess. The caller however fails with the following exception 
		 * org.jbpm.workflow.instance.WorkflowRuntimeException: [invoice-approval:55 - HOD Approval:2] -- null
		 * .............
		 * caused by java.lang.NullPointerException
		 * at org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler.executeWorkItem(GenericHTWorkItemHandler.java:184)
		 * ..............
		 * 
		 * This method therefore must handle its own exceptions internally and mark the Trx for rollback
		 * in case an exception occurs to reverse the whole transaction 
		 * 
		 * An Exception Log should also be inserted into ErroLog table to assist in debugging
		 */
		@Override
		public void taskCompleted(TaskUserEvent event) {
			
			try{
			String processId = "aftertask-notification";
			
			Task task = getTaskClient().getTask(event.getTaskId());			
			Long outputId = task.getTaskData().getOutputContentId();
			
			Map<String, Object> taskData = JBPMHelper.getMappedDataByContentId(outputId);
			
			Map<String,Object> newValues = new HashMap<>();
			
			Long documentId = Long.parseLong(taskData.get("documentId").toString());
			Document doc = DocumentDaoHelper.getDocument(documentId);
			Long processInstanceId = doc.getProcessInstanceId();
			Long sessionId= doc.getSessionId();
			
			if(sessionId==null){
				throw new IllegalArgumentException("SessionId must not be null!!!");
			}
			
			StatefulKnowledgeSession session = getSession(sessionId,processId);
			Map<String, Object> values = new HashMap<>();
			org.jbpm.process.instance.ProcessInstance instance = 
					(org.jbpm.process.instance.ProcessInstance)session.getProcessInstance(processInstanceId);
							
			//Task Data
			Set<String> keys = taskData.keySet();
			for(String key:keys){
				Object value = taskData.get(key);
				if(!key.equals("isApproved"))
					key = key.substring(0,1).toUpperCase()+key.substring(1);
				
				newValues.put(key, value);
				System.err.println("###>>>>Task Data Key :: "+key+" = "+value);
			}
			
			
			ContextInstance ctxInstance = null;
			
			if(instance!=null){
				ctxInstance = instance.getContextInstance(VariableScope.VARIABLE_SCOPE);
			}
			
			if(ctxInstance!=null){
				VariableScopeInstance variableScope = (VariableScopeInstance)ctxInstance;
				values=variableScope.getVariables();
			}
			
			//variable scope
			keys = values.keySet();
			for(String key:keys){
				Object value = values.get(key);
				
				if(!key.equals("isApproved"))
					key = key.substring(0,1).toUpperCase()+key.substring(1);
				
				if(newValues.get(key)==null){
					newValues.put(key, value);
					System.err.println("###>>>>Process Variable Key :: "+key+" = "+value);
				}
			}
				
			Object ownerId = newValues.get("OwnerId");
			if(ownerId==null){
				if(doc!=null){	
					ownerId =doc.getOwner().getId(); 
					newValues.put("OwnerId", ownerId.toString());
				}
			}
			
			startProcess(session,processId, newValues);
			
			}catch(Exception e){
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			//session.startProcess("test-variables", newValues);
		}
		
		@Override
		public void taskClaimed(TaskUserEvent event) {
		}
	}


	public Process getProcess(String processId) {
		KnowledgeBase kbase = getKnowledgeBase(processId, false);
		
		if(kbase!=null){
			return kbase.getProcess(processId);
		}
		
		return null;
	}

}
