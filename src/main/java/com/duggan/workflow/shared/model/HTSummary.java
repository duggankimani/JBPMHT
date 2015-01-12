package com.duggan.workflow.shared.model;

import java.io.Serializable;
import java.util.Date;

public class HTSummary extends Doc implements Serializable{

	private static final long serialVersionUID = -3021583190508951117L;
	private long id;
	private String name;//Display Name
	private String taskName;//Code used for mapping to FormBuilder forms --17/Oct/2014 - this has changed
	private Date startDateDue;//Task Must have started by this time
	private Date endDateDue;//Task must have ended by this time 
	private Date completedOn;//Date this task was completed
	private Date lastUpdate;
	private HTStatus status;
	private Date created;
	private String subject;
	private Integer priority;
	private String description;
	private Long documentRef;
	private Long processInstanceId;
	private HTUser owner;
	private Date documentDate;
	private DocStatus docStatus;
	private Delegate delegate;
	
	public Long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(Long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public HTSummary() {
	}

	public HTSummary(long id) {
		this.id= id;
	}
	
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getTaskName() {
		return taskName;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public HTStatus getStatus() {
		return status;
	}

	public void setStatus(HTStatus status) {
		this.status = status;
	}

	@Override
	public Date getCreated() {
		
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getCaseNo() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getDocumentRef() {
		return documentRef;
	}

	public void setDocumentRef(Long documentRef) {
		this.documentRef = documentRef;
	}

	@Override
	public Date getDocumentDate() {
		
		return documentDate;
	}
	
	@Override
	public HTUser getOwner() {	
		return owner;
	}

	public void setOwner(HTUser owner) {
		this.owner = owner;
	}

	public void setDocumentDate(Date documentDate) {
		this.documentDate = documentDate;
	}

	public DocStatus getDocStatus() {
		return docStatus;
	}

	public void setDocStatus(DocStatus docStatus) {
		this.docStatus = docStatus;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartDateDue() {
		return startDateDue;
	}

	public void setStartDateDue(Date startDateDue) {
		this.startDateDue = startDateDue;
	}

	public Date getEndDateDue() {
		return endDateDue;
	}

	public void setEndDateDue(Date endDateDue) {
		this.endDateDue = endDateDue;
	}
	
	public Delegate getDelegate() {
		return delegate;
	}

	public void setDelegate(Delegate delegate) {
		this.delegate = delegate;
	}

	public Date getCompletedOn() {
		return completedOn;
	}

	public void setCompletedOn(Date completedOn) {
		this.completedOn = completedOn;
	}

	@Override
	public Long getDocumentId() {
		return getDocumentRef();
	}

}
