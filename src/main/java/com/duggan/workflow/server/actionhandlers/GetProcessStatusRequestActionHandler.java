package com.duggan.workflow.server.actionhandlers;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.duggan.workflow.server.helper.jbpm.JBPMHelper;
import com.duggan.workflow.shared.model.NodeDetail;
import com.duggan.workflow.shared.requests.GetProcessStatusRequest;
import com.duggan.workflow.shared.responses.GetProcessStatusRequestResult;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.rpc.server.ExecutionContext;
import com.gwtplatform.dispatch.shared.ActionException;
import com.wira.commons.shared.response.BaseResponse;

public class GetProcessStatusRequestActionHandler extends
		AbstractActionHandler<GetProcessStatusRequest, GetProcessStatusRequestResult> {

	static Logger logger = Logger.getLogger(GetProcessStatusRequestActionHandler.class);
	
	@Inject
	public GetProcessStatusRequestActionHandler() {
	}

	@Override
	public void execute(GetProcessStatusRequest action,
			BaseResponse actionResult, ExecutionContext execContext)
			throws ActionException {
		GetProcessStatusRequestResult result = (GetProcessStatusRequestResult)actionResult;
		
		try{
			result.setNodes((ArrayList<NodeDetail>) JBPMHelper.get().getWorkflowProcessDia(action.getProcessInstanceId()));
		}catch(Exception e){
			e.printStackTrace();
			logger.error("Loading Workflow diagram failed cause: "+e.getMessage());
			// no throwing exceptions back to the client
		}
		
	}
	
	@Override
	public Class<GetProcessStatusRequest> getActionType() {
		return GetProcessStatusRequest.class;
	}
}
