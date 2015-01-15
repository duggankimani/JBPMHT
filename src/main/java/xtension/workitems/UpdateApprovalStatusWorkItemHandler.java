package xtension.workitems;

import org.apache.log4j.Logger;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

import com.duggan.workflow.server.dao.helper.DocumentDaoHelper;

/**
 * This work item handler updates the approval status
 * of the local document. It should be called if the
 * document is completed(approved to the end) or 
 * rejected by any of the approvers in the process
 * 
 * @author duggan
 *
 */
public class UpdateApprovalStatusWorkItemHandler implements WorkItemHandler{
	
	private Logger log = Logger.getLogger(UpdateApprovalStatusWorkItemHandler.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Long documentId = new Long(workItem.getParameter("DocumentId").toString());
		
		Object isApproved = workItem.getParameter("isApproved");
		
		if(isApproved==null){
			//nothing to update
			log.warn("UpdateApproval status [DocumentId+"+documentId+"] cannot be performed. Reason [isApproved==null]");
			manager.completeWorkItem(workItem.getId(), workItem.getParameters());
			return;
		}
		
		/*
		 * This work item is meant to primarily record premature
		 * end of process by an approver rejecting a document
		 * 
		 */
		
		String work = workItem.getId()+" : "+workItem.getName()+"; Process Completed = "+workItem.getParameter("isProcessComplete")+
				" :: Approved = "+isApproved;
		Boolean processCompleted = workItem.getParameter("isProcessComplete")==null ? false: 
			new Boolean(workItem.getParameter("isProcessComplete").toString());
				
		//only update document if process is completed or the document was rejected
		if(processCompleted || !(Boolean)isApproved){
			DocumentDaoHelper.saveApproval(new Long(documentId.toString()), (Boolean)isApproved);
//			System.err.println("SAVE :: "+work);
		}else{
//			System.err.println("Ignore :: "+work);
		}
		
		manager.completeWorkItem(workItem.getId(), workItem.getParameters());
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		
	}

	
}
