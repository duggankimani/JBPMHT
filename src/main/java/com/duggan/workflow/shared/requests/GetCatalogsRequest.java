package com.duggan.workflow.shared.requests;

import com.duggan.workflow.shared.responses.BaseResponse;
import com.duggan.workflow.shared.responses.GetCatalogsResponse;

public class GetCatalogsRequest extends BaseRequest<GetCatalogsResponse> {

	private String catalogRefId;
	private boolean isLoadViews;
	
	public GetCatalogsRequest() {
	}
	
	public GetCatalogsRequest(String catalogRefId) {
		this.catalogRefId = catalogRefId;
	}
	
	public GetCatalogsRequest(boolean isLoadViews) {
		this.isLoadViews = isLoadViews;
	}
	
	@Override
	public BaseResponse createDefaultActionResponse() {
		return new GetCatalogsResponse();
	}

	public boolean isLoadViews() {
		return isLoadViews;
	}

	public String getCatalogRefId() {
		return catalogRefId;
	}

}
