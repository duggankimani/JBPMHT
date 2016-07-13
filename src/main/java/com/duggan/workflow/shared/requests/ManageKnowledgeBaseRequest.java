package com.duggan.workflow.shared.requests;

import com.duggan.workflow.shared.model.ManageProcessAction;
import com.duggan.workflow.shared.responses.ManageKnowledgeBaseResponse;
import com.wira.commons.shared.request.BaseRequest;
import com.wira.commons.shared.response.BaseResponse;

public class ManageKnowledgeBaseRequest extends
		BaseRequest<ManageKnowledgeBaseResponse> {

	private Long processDefId;

	private ManageProcessAction action;
	
	private boolean force;
	
	@SuppressWarnings("unused")
	private ManageKnowledgeBaseRequest() {
		// For serialization only
	}

	public ManageKnowledgeBaseRequest(Long processDefId, ManageProcessAction action, boolean force) {
		this.processDefId = processDefId;
		this.action = action;
		this.force = force;
	}

	public Long getProcessDefId() {
		return processDefId;
	}

	public ManageProcessAction getAction() {
		return action;
	}
	
	@Override
	public BaseResponse createDefaultActionResponse() {
		return new ManageKnowledgeBaseResponse();
	}

	public boolean isForce(){
		return force;
	}
}
