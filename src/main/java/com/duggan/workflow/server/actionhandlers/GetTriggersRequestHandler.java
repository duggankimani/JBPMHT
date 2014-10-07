package com.duggan.workflow.server.actionhandlers;

import java.util.List;

import com.duggan.workflow.server.dao.helper.ProcessDefHelper;
import com.duggan.workflow.shared.model.Trigger;
import com.duggan.workflow.shared.requests.GetTriggersRequest;
import com.duggan.workflow.shared.responses.BaseResponse;
import com.duggan.workflow.shared.responses.GetTriggersResponse;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.rpc.server.ExecutionContext;
import com.gwtplatform.dispatch.shared.ActionException;

public class GetTriggersRequestHandler extends BaseActionHandler<GetTriggersRequest, GetTriggersResponse>{

	@Inject
	public GetTriggersRequestHandler() {
	}
	
	@Override
	public void execute(GetTriggersRequest action,
			BaseResponse actionResult, ExecutionContext execContext)
			throws ActionException {
		
		List<Trigger> triggers=null;

		triggers =  ProcessDefHelper.getTriggers();
		
		((GetTriggersResponse)actionResult).setTriggers(triggers);
	}
	
	public java.lang.Class<GetTriggersRequest> getActionType() {
		return GetTriggersRequest.class;
	};
}
