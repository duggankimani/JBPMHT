package com.duggan.workflow.server.actionhandlers;

import com.duggan.workflow.server.dao.helper.ProcessDaoHelper;
import com.duggan.workflow.shared.model.TaskNotification;
import com.duggan.workflow.shared.requests.GetNotificationTemplateRequest;
import com.duggan.workflow.shared.responses.GetNotificationTemplateResult;
import com.gwtplatform.dispatch.rpc.server.ExecutionContext;
import com.gwtplatform.dispatch.shared.ActionException;
import com.wira.commons.shared.response.BaseResponse;

public class GetNotificationTemplateRequestHandler extends
		AbstractActionHandler<GetNotificationTemplateRequest, GetNotificationTemplateResult> {

	@Override
	public Class<GetNotificationTemplateRequest> getActionType() {
		return GetNotificationTemplateRequest.class;
	}

	@Override
	public void execute(GetNotificationTemplateRequest action,
			BaseResponse actionResult, ExecutionContext execContext)
			throws ActionException {
		
		TaskNotification notification = ProcessDaoHelper.getTaskNotificationTemplate(
				action.getNodeId(), action.getStepName(),
				action.getProcessDefId(), action.getCategory(),
				action.getAction());
		((GetNotificationTemplateResult)actionResult).setNotification(notification);
	}

}
