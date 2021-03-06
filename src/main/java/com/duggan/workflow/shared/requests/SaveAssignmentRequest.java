package com.duggan.workflow.shared.requests;

import com.duggan.workflow.shared.model.AssignmentDto;
import com.duggan.workflow.shared.responses.SaveAssignmentResponse;
import com.wira.commons.shared.request.BaseRequest;
import com.wira.commons.shared.response.BaseResponse;

public class SaveAssignmentRequest extends BaseRequest<SaveAssignmentResponse> {

	private AssignmentDto assignmentDto;

	@SuppressWarnings("unused")
	private SaveAssignmentRequest() {
		// For serialization only
	}

	public SaveAssignmentRequest(AssignmentDto assignmentDto) {
		this.assignmentDto = assignmentDto;
	}

	@Override
	public BaseResponse createDefaultActionResponse() {
		return new SaveAssignmentResponse();
	}

	public AssignmentDto getAssignmentDto() {
		return assignmentDto;
	}
}
