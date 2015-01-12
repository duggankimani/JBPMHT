package com.duggan.workflow.server.dao.helper;

import static com.duggan.workflow.server.dao.helper.DocumentDaoHelper.getType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.drools.definition.process.Node;
import org.jbpm.task.Task;

import com.duggan.workflow.client.ui.admin.processitem.NotificationCategory;
import com.duggan.workflow.server.dao.ProcessDaoImpl;
import com.duggan.workflow.server.dao.model.ADDocType;
import com.duggan.workflow.server.dao.model.ADForm;
import com.duggan.workflow.server.dao.model.ADOutputDoc;
import com.duggan.workflow.server.dao.model.ADTaskNotification;
import com.duggan.workflow.server.dao.model.ADTaskStepTrigger;
import com.duggan.workflow.server.dao.model.ADTrigger;
import com.duggan.workflow.server.dao.model.DocumentModel;
import com.duggan.workflow.server.dao.model.LocalAttachment;
import com.duggan.workflow.server.dao.model.ProcessDefModel;
import com.duggan.workflow.server.dao.model.TaskStepModel;
import com.duggan.workflow.server.db.DB;
import com.duggan.workflow.server.helper.jbpm.JBPMHelper;
import com.duggan.workflow.shared.model.Actions;
import com.duggan.workflow.shared.model.DocumentType;
import com.duggan.workflow.shared.model.ProcessDef;
import com.duggan.workflow.shared.model.Status;
import com.duggan.workflow.shared.model.TaskNotification;
import com.duggan.workflow.shared.model.TaskStepDTO;
import com.duggan.workflow.shared.model.TaskStepTrigger;
import com.duggan.workflow.shared.model.Trigger;
import com.duggan.workflow.shared.model.TriggerType;

public class ProcessDefHelper {

	static Logger log = Logger.getLogger(ProcessDefHelper.class);
	
	public static ProcessDef save(ProcessDef processDef) {
		ProcessDaoImpl dao = DB.getProcessDao();
		ProcessDefModel model = get(processDef);

		dao.save(model);

		return get(model);
	}

	public static void delete(Long processDefId) {
		ProcessDaoImpl dao = DB.getProcessDao();
		dao.remove(dao.getProcessDef(processDefId));
	}

	public static void delete(ProcessDef processDef) {
		ProcessDaoImpl dao = DB.getProcessDao();
		ProcessDefModel model = get(processDef);

		dao.remove(model);
	}

	public static ProcessDef getProcessDef(Long processDefId) {
		ProcessDaoImpl dao = DB.getProcessDao();

		ProcessDefModel model = dao.getProcessDef(processDefId);

		ProcessDef def = get(model);

		return def;
	}

	public static List<ProcessDef> getAllProcesses() {

		ProcessDaoImpl dao = DB.getProcessDao();

		List<ProcessDefModel> process = dao.getAllProcesses();

		List<ProcessDef> processDefs = new ArrayList<>();

		if (processDefs != null) {
			for (ProcessDefModel model : process) {
				ProcessDef processDef = get(model);
				processDefs.add(processDef);
			}
		}

		return processDefs;
	}

	public static ProcessDef get(ProcessDefModel model) {

		ProcessDef def = new ProcessDef();
		def.setDocTypes(getDocTypes(model.getDocumentTypes()));
		def.setName(model.getName());
		def.setProcessId(model.getProcessId());
		def.setId(model.getId());

		boolean running = JBPMHelper.get().isProcessingRunning(
				model.getProcessId());
		def.setStatus(running ? Status.RUNNING
				: Status.INACTIVE);

		def.setDescription(model.getDescription());

		def.setLastModified(model.getUpdated() == null ? model.getCreated()
				: model.getUpdated());

		List<LocalAttachment> attachments = DB.getAttachmentDao()
				.getAttachmentsForProcessDef(model);

//		LocalAttachment attachment = (attachments == null || attachments.size() == 0) ? null
//				: attachments.get(0);

		if (attachments != null) {
			for(LocalAttachment attach: attachments){
				def.addFile(AttachmentDaoHelper.get(attach));
			}			
//			def.setFileName(attachment.getName());
//			def.setFileId(attachment.getId());
		}
		
		
		List<LocalAttachment> image = DB.getAttachmentDao()
				.getAttachmentsForProcessDef(model,true);

		if(image!=null && !image.isEmpty()){
			def.setImageId(image.get(0).getId());
			
			def.setImageName(image.get(0).getName());
		}
			

		List<DocumentType> docTypes = new ArrayList<>();

		Collection<ADDocType> docModels = model.getDocumentTypes();
		
		for (ADDocType doc : docModels) {
			docTypes.add(getType(doc));
		}

		def.setDocTypes(docTypes);

		return def;
	}

	private static List<DocumentType> getDocTypes(
			Collection<ADDocType> processDocuments) {
		if (processDocuments == null) {
			return new ArrayList<>();
		}

		List<DocumentType> docs = new ArrayList<>();

		for (ADDocType model : processDocuments) {
			docs.add(getType(model));
		}

		return docs;
	}

	public static ProcessDefModel get(ProcessDef processDef) {
		ProcessDaoImpl dao = DB.getProcessDao();

		ProcessDefModel model = new ProcessDefModel();
		Long id = processDef.getId();

		if (id != null) {
			model = dao.getProcessDef(id);
			model.clear();
		}

		model.setName(processDef.getName());
		model.setDescription(processDef.getDescription());
		model.setProcessId(processDef.getProcessId());

		List<DocumentType> types = processDef.getDocTypes();

		if (types != null) {
			for (DocumentType type : types) {
				ADDocType adtype = getType(type);
				model.addDocType(adtype);
			}
		}

		return model;
	}
	
	public static List<TaskStepDTO> createTaskSteps(List<TaskStepDTO> steps){
		ProcessDaoImpl dao = DB.getProcessDao();
		List<TaskStepDTO> list = new ArrayList<>();
		
		Long processDefId = steps.get(0).getProcessDefId();
		Long nodeId = steps.get(0).getNodeId();
		int size = dao.getStepCount(processDefId,nodeId);
		
		for(TaskStepDTO step: steps){
			
			int previousPos = -1;
			int newPos = -1;
			if(step.getId()!=null){
				previousPos = dao.getById(TaskStepModel.class, step.getId()).getSequenceNo();
				newPos = step.getSequenceNo();
			}else{
				newPos=previousPos=++size;
				step.setSequenceNo(newPos);
			}
			
			if(previousPos!=newPos){
				//Changed sequence
				TaskStepModel previousModel = 
						dao.getTaskStepBySequenceNo(step.getProcessDefId(),step.getNodeId(),newPos);
				if(previousModel!=null){
					//Swap the positions of affected models
					previousModel.setSequenceNo(previousPos);
					dao.createStep(previousModel);
				}
			}
			
			TaskStepModel model = getStep(step);
			if(!step.isActive()){
				dao.cascadeDelete(model);
				recalculatePositionsForSubsequentSteps(step);
				continue;
			}
			
			//Creating/ Updating model
			dao.createStep(model);
						
			list.add(getStep(model));
		}
		
		return list;
	}

	/**
	 * Called After step delete</p>
	 * Recalculates positions of subsequent steps after delete
	 * <p>
	 * @param sequenceNo
	 */
	private static void recalculatePositionsForSubsequentSteps(TaskStepDTO deletedModel) {
		ProcessDaoImpl dao = DB.getProcessDao();
		List<TaskStepModel> models  = dao.getTaskStepsAfterSeqNo(deletedModel.getProcessDefId(),
				deletedModel.getNodeId(),
				deletedModel.getSequenceNo()); 
		for(TaskStepModel model: models){
			int seqNo = model.getSequenceNo();
			model.setSequenceNo(seqNo-1);
			dao.save(model);
		}
	}

	private static TaskStepDTO getStep(TaskStepModel model) {

		TaskStepDTO dto = new TaskStepDTO();
		dto.setCondition(model.getCondition());
		dto.setFormName(model.getForm()==null? null: model.getForm().getCaption());
		dto.setFormId(model.getForm()==null? null: model.getForm().getId());
		dto.setId(model.getId());
		dto.setMode(model.getMode());
		dto.setNodeId(model.getNodeId());
		dto.setOutputDocName(model.getDoc()==null?null: model.getDoc().getName());
		dto.setOutputDocId(model.getDoc()==null?null: model.getDoc().getId());
		dto.setProcessDefId(model.getProcessDef().getId());
		dto.setSequenceNo(model.getSequenceNo());
		dto.setStepName(model.getStepName());
		
		
		return dto;
	}

	private static TaskStepModel getStep(TaskStepDTO step) {
		ProcessDaoImpl dao = DB.getProcessDao();
		
		TaskStepModel model = new TaskStepModel();
		if(step.getId()!=null){
			model = dao.getById(TaskStepModel.class, step.getId());
		}
		model.setCondition(step.getCondition());
		model.setForm(step.getFormId()==null? null: dao.getById(ADForm.class, step.getFormId()));
		model.setDoc(step.getOutputDocId()==null? null: dao.getById(ADOutputDoc.class, step.getOutputDocId()));
		model.setMode(step.getMode());
		model.setNodeId(step.getNodeId());
		model.setProcessDef(dao.getById(ProcessDefModel.class, step.getProcessDefId()));
		model.setSequenceNo(step.getSequenceNo());
		model.setStepName(step.getStepName());
		
		return model;
	}
	
	public static List<TaskStepDTO> getSteps(String processId, Long nodeId){
		ProcessDaoImpl dao = DB.getProcessDao();
		List<TaskStepModel> list = dao.getTaskSteps(processId, nodeId);
		List<TaskStepDTO> dtos = new ArrayList<>();
		
		for(TaskStepModel model: list){
			dtos.add(getStep(model));
		}
		
		return dtos;
	}

	public static List<TaskStepDTO> getTaskStepsByTaskId(Long taskId) {
		Task task = JBPMHelper.get().getSysTask(taskId);
		Node node = null;
		
		try{
			node= JBPMHelper.get().getNode(task);
		}catch(Exception e){
			log.warn(e.getMessage());
		}
		
		if(node==null){
			return new ArrayList<>();
		}
		
		List<TaskStepModel> models= DB.getProcessDao().getTaskSteps(task.getTaskData().getProcessId(), node.getId());
		
		List<TaskStepDTO> steps = new ArrayList<>();
		for(TaskStepModel m: models){
			steps.add(getStep(m));
		}
		return steps;
	}

	public static List<TaskStepDTO> getTaskStepsByDocumentId(Long documentId) {


		DocumentModel docModel = DB.getDocumentDao().getById(documentId);
		
		String processId = docModel.getProcessId();
		if(processId==null && docModel.getType()!=null){
			processId = docModel.getType().getProcessDef().getProcessId();
		}
		List<TaskStepModel> models= DB.getProcessDao().getTaskSteps(processId, null);
	
		List<TaskStepDTO> steps = new ArrayList<>();
		for(TaskStepModel m: models){
			steps.add(getStep(m));
		}
		return steps;
	}

	public static List<Trigger> getTriggers() {
		ProcessDaoImpl dao = DB.getProcessDao();
		
		List<Trigger> triggers = new ArrayList<>();
		List<ADTrigger> adTriggers = dao.getTriggers();
		
		for(ADTrigger adtrigger: adTriggers){
			triggers.add(getTrigger(adtrigger));
		}
		
		return triggers;
	}

//	public static List<Trigger> getTriggers(Long taskStepId, TriggerType type) {
//
//		ProcessDaoImpl dao = DB.getProcessDao();
//		
//		List<Trigger> triggers = new ArrayList<>();
//		List<ADTrigger> adTriggers = dao.getTriggers(taskStepId, type);
//		
//		for(ADTrigger adtrigger: adTriggers){
//			triggers.add(getTrigger(adtrigger));
//		}
//		
//		return triggers;
//	}

	public static Trigger saveTrigger(Trigger trigger) {
		ProcessDaoImpl dao = DB.getProcessDao();
		ADTrigger po = getTrigger(trigger);
		
		if(trigger.isActive()){
			dao.save(po);
		}else{
			dao.cascadeDeleteTrigger(po);
			return trigger;
		}
		
		Trigger saved  = getTrigger(po);
		
		return saved;
	}

	private static Trigger getTrigger(ADTrigger po) {
		
		Trigger trigger = new Trigger();
		trigger.setId(po.getId());
		trigger.setName(po.getName());
		trigger.setScript(po.getScript());
		trigger.setImports(po.getImports());
		return trigger;
	}

	private static ADTrigger getTrigger(Trigger trigger) {
		
		ProcessDaoImpl dao = DB.getProcessDao();
		ADTrigger po = new ADTrigger();
		
		if(trigger.getId()!=null){
			po = dao.getById(ADTrigger.class, trigger.getId());
		}
		
		po.setName(trigger.getName());
		po.setImports(trigger.getImports());
		po.setScript(trigger.getScript());
		return po;
	}

	public static List<TaskStepTrigger> getTaskStepTriggers(Long taskStepId, TriggerType type) {
		ProcessDaoImpl dao = DB.getProcessDao();
		
		Collection<ADTaskStepTrigger> adTriggers= dao.getTaskStepTriggers(taskStepId, type);
		
		List<TaskStepTrigger> triggers = new ArrayList<>();
		for(ADTaskStepTrigger adTrigger: adTriggers){
			TaskStepTrigger trigger  = getStepTrigger(adTrigger);
			triggers.add(trigger);
		}
		
		return triggers;
	}
	
	public static int getTriggerCount(Long taskStepId, TriggerType type){
		
		ProcessDaoImpl dao = DB.getProcessDao();

		return dao.getTaskCount(taskStepId, type);
	}

	public static TaskStepTrigger saveTaskStepTrigger(TaskStepTrigger trigger) {
		ProcessDaoImpl dao = DB.getProcessDao();
		
		ADTaskStepTrigger adTaskStep = getStepTrigger(trigger);
		
		if(trigger.isActive()){
			dao.save(adTaskStep);
		}else{
			
			dao.delete(adTaskStep);
			return trigger;
		}
		
		return getStepTrigger(adTaskStep);
	}

	private static TaskStepTrigger getStepTrigger(ADTaskStepTrigger adTaskStep) {
		
		TaskStepTrigger stepTrigger = new TaskStepTrigger();
		stepTrigger.setTrigger(getTrigger(adTaskStep.getTrigger()));
		stepTrigger.setTaskStepId(adTaskStep.getTaskStep().getId());
		stepTrigger.setType(adTaskStep.getType());
		stepTrigger.setId(adTaskStep.getId());
		
		return stepTrigger;
	}

	private static ADTaskStepTrigger getStepTrigger(TaskStepTrigger trigger) {
		ProcessDaoImpl dao = DB.getProcessDao();
		ADTaskStepTrigger adTaskStep = new ADTaskStepTrigger();
		
		if(trigger.getId()!=null){
			adTaskStep = dao.getById(ADTaskStepTrigger.class, trigger.getId());
		}
		
		adTaskStep.setTaskStep(dao.getById(TaskStepModel.class, trigger.getTaskStepId()));
		
		adTaskStep.setType(trigger.getType());
		adTaskStep.setTrigger(dao.getById(ADTrigger.class, trigger.getTrigger().getId()));
		
		return adTaskStep;
	}
	
	public static TaskNotification saveTaskNotification(TaskNotification notification){
		ProcessDaoImpl dao = DB.getProcessDao();
		ADTaskNotification model = getTaskNotificationModel(notification);
		dao.save(model);
		return getTaskNotification(model);
	}

	private static TaskNotification getTaskNotification(ADTaskNotification model) {
		
		TaskNotification notification = new TaskNotification();
		if(model==null){
			return notification;
		}
		notification.setId(model.getId());
		notification.setAction(model.getAction());
		notification.setCategory(model.getCategory());
		notification.setEnableNotification(model.isEnableNotification());
		notification.setNotificationTemplate(model.getNotificationTemplate());
		notification.setTargets(model.getReceipients());
		notification.setUseDefaultNotification(model.isUseDefaultNotification());
		notification.setNodeId(model.getNodeId());
		notification.setStepName(model.getStepName());
		notification.setProcessDefId(model.getProcessDefId());
		notification.setSubject(model.getSubject());
		
		return notification;
	}
	
	private static ADTaskNotification getTaskNotificationModel(
			TaskNotification notification) {

		ProcessDaoImpl dao = DB.getProcessDao();
		ADTaskNotification model = new ADTaskNotification();
		if(notification.getId()!=null){
			model= dao.getTaskNotificationById(notification.getId());
		}
		
		model.setAction(notification.getAction());
		model.setCategory(notification.getCategory());
		model.setEnableNotification(notification.isEnableNotification());
		model.setNotificationTemplate(notification.getNotificationTemplate());
		model.setReceipients(notification.getTargets());
		model.setUseDefaultNotification(notification.isUseDefaultNotification());
		model.setNodeId(notification.getNodeId());
		model.setStepName(notification.getStepName());
		model.setProcessDefId(notification.getProcessDefId());
		model.setSubject(notification.getSubject());
		
		return model;
	}
	
	public static TaskNotification getTaskNotificationTemplate(Long nodeId, String stepName, Long processDefId, 
			NotificationCategory category, Actions action){
		ProcessDaoImpl dao = DB.getProcessDao();
		ADTaskNotification model = dao.getTaskNotification(nodeId, stepName, processDefId, category,action);
		return getTaskNotification(model);
	}

}
