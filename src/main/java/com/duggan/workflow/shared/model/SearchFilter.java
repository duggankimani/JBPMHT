package com.duggan.workflow.shared.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SearchFilter implements Serializable,IsSerializable {

	private static final long serialVersionUID = -8494519116994121416L;
	private String subject;
	private String phrase;
	private Date startDate;
	private Date endDate;
	private Integer priority;
	private Boolean hasAttachment;
	private DocumentType DocumentType;
	private String userId;
	private List<String> groupIds;
	private String processId;
	private List<String> taskStatuses;

	public SearchFilter() {
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public void setAttachment(Boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
	}

	public String getSubject() {
		return subject;
	}

	public String getPhrase() {
		return phrase;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public Integer getPriority() {
		return priority;
	}

	public Boolean hasAttachment() {
		return hasAttachment;
	}

	public DocumentType getDocType() {
		return DocumentType;
	}

	public void setDocType(DocumentType DocumentType) {
		this.DocumentType = DocumentType;
	}
	
	public boolean isEmpty(){
		if(subject!=null)
			return false;
		if(phrase!=null)
			return false;
		if(startDate!=null)
			return false;
		if(endDate!=null)
			return false;
		if(priority!=null)
			return false;
		if(hasAttachment!=null)
			return false;
		if(DocumentType!=null)
			return false;
		return  true;
		
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public List<String> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(List<String> groupIds) {
		this.groupIds = groupIds;
	}

	public List<String> getTaskStatuses() {
		return taskStatuses;
	}

	public void setTaskStatuses(List<String> taskStatuses) {
		this.taskStatuses = taskStatuses;
	}
}
