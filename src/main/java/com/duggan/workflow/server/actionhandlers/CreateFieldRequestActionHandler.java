package com.duggan.workflow.server.actionhandlers;

import com.duggan.workflow.server.dao.helper.FormDaoHelper;
import com.duggan.workflow.shared.model.form.Field;
import com.duggan.workflow.shared.requests.CreateFieldRequest;
import com.duggan.workflow.shared.responses.CreateFieldResponse;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.rpc.server.ExecutionContext;
import com.gwtplatform.dispatch.shared.ActionException;
import com.wira.commons.shared.response.BaseResponse;

public class CreateFieldRequestActionHandler extends
		AbstractActionHandler<CreateFieldRequest, CreateFieldResponse> {

	@Inject
	public CreateFieldRequestActionHandler() {
	}

	@Override
	public void execute(CreateFieldRequest action, BaseResponse actionResult,
			ExecutionContext execContext) throws ActionException {
		Field field = action.getField();
		
		//Field rtnfield = FormDaoHelper.createField(field);
		Field rtnfield = FormDaoHelper.createJson(field);
		
		CreateFieldResponse response = (CreateFieldResponse)actionResult;
		
		response.setField(rtnfield);
	}

	@Override
	public Class<CreateFieldRequest> getActionType() {
		return CreateFieldRequest.class;
	}
}
