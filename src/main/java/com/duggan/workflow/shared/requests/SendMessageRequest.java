package com.duggan.workflow.shared.requests;

import com.duggan.workflow.shared.model.RequestInfoDto;
import com.duggan.workflow.shared.responses.SendMessageResponse;
import com.wira.commons.shared.request.BaseRequest;
import com.wira.commons.shared.response.BaseResponse;

public class SendMessageRequest extends
		BaseRequest<SendMessageResponse> {

	private RequestInfoDto dto;

	@SuppressWarnings("unused")
	private SendMessageRequest() {
		// For serialization only
	}

	public SendMessageRequest(RequestInfoDto dto) {
		this.dto = dto;
	}
	
	@Override
	public BaseResponse createDefaultActionResponse() {
		
		return new SendMessageResponse();
	}

	public RequestInfoDto getDto() {
		return dto;
	}

	public void setDto(RequestInfoDto dto) {
		this.dto = dto;
	}
}
