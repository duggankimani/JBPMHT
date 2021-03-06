package com.duggan.workflow.server.actionhandlers;

import java.util.ArrayList;
import java.util.List;

import com.duggan.workflow.server.dao.helper.ProcessDaoHelper;
import com.duggan.workflow.server.dao.model.ProcessDefModel;
import com.duggan.workflow.server.db.DB;
import com.duggan.workflow.shared.model.TaskStepDTO;
import com.duggan.workflow.shared.requests.SaveTaskStepRequest;
import com.duggan.workflow.shared.responses.SaveTaskStepResponse;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.rpc.server.ExecutionContext;
import com.gwtplatform.dispatch.shared.ActionException;
import com.wira.commons.shared.response.BaseResponse;

public class SaveTaskStepRequestHandler extends
		AbstractActionHandler<SaveTaskStepRequest, SaveTaskStepResponse> {

	@Inject
	public SaveTaskStepRequestHandler() {
	}
	
	@Override
	public void execute(SaveTaskStepRequest action, BaseResponse actionResult,
			ExecutionContext execContext) throws ActionException {
		ProcessDaoHelper.createTaskSteps(action.getSteps());
		
		TaskStepDTO dto = action.getSteps().get(0);
		
		ProcessDefModel model = DB.getProcessDao().getProcessDef(dto.getProcessDefId());
		List<TaskStepDTO> allSteps = ProcessDaoHelper.getSteps(model.getProcessId(), dto.getNodeId());
		((SaveTaskStepResponse)actionResult).setList((ArrayList<TaskStepDTO>) allSteps);
	}
	
	@Override
	public Class<SaveTaskStepRequest> getActionType() {
		return SaveTaskStepRequest.class;
	}
}
