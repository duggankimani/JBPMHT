package com.duggan.workflow.shared.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.wira.commons.shared.models.HTUser;

@XmlType
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Doc extends IsDoc implements Comparable<Doc> {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 1L;

	private boolean hasAttachment = false;

	@XmlTransient
	protected HashMap<String, ArrayList<DocumentLine>> details = new HashMap<String, ArrayList<DocumentLine>>();

	private String uploadedFileId;

	private String processId;
	private String processName;
	private String nodeName;
	private Long nodeId;
	protected String caseNo;

	private HTStatus processStatus = HTStatus.INPROGRESS;

	private String currentTaskName;
	private Long currentTaskId;
	private HTUser taskActualOwner;
	private String potentialOwners;
	private HTUser owner;

	public Doc() {
	}

	public String getDescription() {
		return null;
	}

	public Date getCreated() {
		return null;
	}

	public Date getDocumentDate() {
		return null;
	}

	public Integer getPriority() {
		return null;
	}

	public Object getId() {
		return null;
	}
	
	/**
	 * Sorts document/task elements in descending order hence the negative sign
	 * (-)
	 */
	@Override
	public int compareTo(Doc o) {
		Date thisDate = getSortDate(this);

		Date other = getSortDate(o);
		
		if(thisDate!=null && other==null){
			return -1;
		}else if(thisDate==null && other!=null){
			return 1;
		}else if(thisDate==null && other==null){
			return 0;
		}
		
		return (-thisDate.compareTo(other));
	}

	public Date getSortDate() {
		return getSortDate(this);
	}

	private Date getSortDate(Doc doc) {

		Date dateToUse = doc.getCreated();
		if (doc instanceof HTSummary) {
			HTSummary summ = (HTSummary) doc;
			if (summ.getStatus() == HTStatus.COMPLETED) {
				dateToUse = summ.getCompletedOn();
			}
			if(dateToUse==null){
				dateToUse = summ.getCreated();
			}
		} else {
			Document document = (Document) doc;
			if (!document.getStatus().equals(DocStatus.DRAFTED)) {
				dateToUse = document.getDateSubmitted();
			}
			if(dateToUse==null){
				dateToUse = doc.getCreated();
			}
		}
		
		

		return dateToUse;
	}

	public HTUser getOwner() {
		return owner;
	}

	public Long getProcessInstanceId() {
		return null;
	}

	public boolean hasAttachment() {
		return hasAttachment;
	}

	public void setHasAttachment(boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
	}

	public HashMap<String, ArrayList<DocumentLine>> getDetails() {
		return details;
	}

	public void setDetails(HashMap<String, ArrayList<DocumentLine>> details) {
		this.details = details;
	}

	public void addDetail(DocumentLine line) {
		String name = line.getName();

		ArrayList<DocumentLine> lines = details.get(name);
		if (lines == null) {
			lines = new ArrayList<DocumentLine>();
			details.put(name, lines);
		}
		lines.add(line);

		/*
		 * Duggan 06/10/2015 ExecuteWorkflow action submits a ArrayList of Value
		 * objects i.e HashMap<String,Value> , which works ok for all fields
		 * except grid fields updated through triggers
		 * 
		 * Grid rows updated through a trigger call to addDetail('gridName',
		 * DocumentLine) are not added to a GridValue object: they are written
		 * directly to a HashMap<String, ArrayList<DocLine>>, hence they are
		 * left out when ExecuteWorkflow is called.
		 * 
		 * To remedy this issue, We need to loop through the document lines
		 * generating a GridValue entry for each document line with no
		 * corresponding gridValue. ALTERNATIVELY, override addDetail and
		 * generate a GridValue entry there - This may be a better fit since
		 * 'after-step' triggers on the last node will not interact with the
		 * interface before calling ExecuteWorkflow.
		 * 
		 * @See Doc.addDetail()
		 */
		GridValue value = null;
		if (values.get(name) == null) {
			value = new GridValue();
			value.setKey(name);
		} else if (values.get(name) instanceof GridValue) {
			value = (GridValue) values.get(name);
		} else {
			value = new GridValue();
			value.setKey(name);
		}

		if (value.getValue().contains(line)) {
			value.getValue().remove(line);
		}
		value.getValue().add(line);
		values.put(name, value);
	}

	public void setDetails(String key, Collection<DocumentLine> values) {
		// Clear previous - Meant to avoid duplicates
		details.remove(key);
		for (DocumentLine line : values) {
			line.setName(key);
			addDetail(line);
		}
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public HTUser getTaskActualOwner() {
		return taskActualOwner;
	}

	public void setTaskActualOwner(HTUser taskActualOwner) {
		this.taskActualOwner = taskActualOwner;
	}

	public void setProcessStatus(HTStatus status) {
		this.processStatus = status;
	}

	public HTStatus getProcessStatus() {
		return processStatus;
	}

	public String getPotentialOwners() {
		return potentialOwners;
	}

	public void setPotentialOwners(String potentialOwners) {
		this.potentialOwners = potentialOwners;
	}

	public Long getDocumentId() {
		return null;
	}

	public String getCaseNo() {
		return caseNo;
	}

	public void setCaseNo(String caseNo) {
		this.caseNo = caseNo;
		setValue("caseNo", new StringValue(null, "caseNo", caseNo));
	}


	public String getUploadedFileId() {
		return uploadedFileId;
	}

	public void setUploadedFileId(String uploadedFileId) {
		this.uploadedFileId = uploadedFileId;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		Doc other = (Doc)obj;
		if(!isEqual(getRefId(),other.getRefId())){
			return false;
		}
		
		if(!isEqual(taskActualOwner,other.taskActualOwner)){
			return false;
		}
		
		if(!isEqual(currentTaskId, other.currentTaskId)){
			return false;
		}
		
		if(!isEqual(currentTaskName, other.currentTaskName)){
			return false;
		}
		
		if(!isEqual(processId,other.processId)){
			return false;
		}
		
		if(!isEqual(caseNo,other.caseNo)){
			return false;
		}
		
		if(!isEqual(processStatus,other.processStatus)){
			return false;
		}
		
		if(!isEqual(taskActualOwner,other.taskActualOwner)){
			return false;
		}
		
		if(!isEqual(potentialOwners,other.potentialOwners)){
			return false;
		}
		
		if(!isEqual(taskActualOwner,other.taskActualOwner)){
			return false;
		}
		
		if(values.size()!= other.values.size()){
			return false;
		}
		
		if(details.size()!=other.details.size()){
			return false;
		}
		
		return true;
	}

	protected boolean isEqual(Object obj1, Object obj2) {
		
		if(obj1!=null && obj2!=null){
			return obj1.equals(obj2);
		}else if(obj1==null && obj2==null){
			//Both are null
			return true;
		}
		
		//Only one of the values is null;
		return false;
	}

	public String getCurrentTaskName() {
		return currentTaskName;
	}

	public void setCurrentTaskName(String currentTaskName) {
		this.currentTaskName = currentTaskName;
	}

	public Long getCurrentTaskId() {
		return currentTaskId;
	}

	public void setCurrentTaskId(Long currentTaskId) {
		this.currentTaskId = currentTaskId;
	}

	public void setOwner(HTUser owner) {
		this.owner = owner;
	}

}
