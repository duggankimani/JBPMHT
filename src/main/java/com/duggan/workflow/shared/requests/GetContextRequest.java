package com.duggan.workflow.shared.requests;

import com.duggan.workflow.shared.responses.BaseResponse;
import com.duggan.workflow.shared.responses.GetContextRequestResult;

public class GetContextRequest extends BaseRequest<GetContextRequestResult> {

	public GetContextRequest() {
	}
	
	@Override
	public BaseResponse createDefaultActionResponse() {
	
		return new GetContextRequestResult();
	}
}
