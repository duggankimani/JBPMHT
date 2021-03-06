package com.duggan.workflow.server.actionhandlers;

import java.util.HashMap;
import java.util.Map;

import com.duggan.workflow.server.sms.SMSIntegration;
import com.duggan.workflow.shared.model.Value;
import com.duggan.workflow.shared.requests.GenericRequest;
import com.duggan.workflow.shared.responses.GenericResponse;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.rpc.server.ExecutionContext;
import com.gwtplatform.dispatch.shared.ActionException;
import com.wira.commons.shared.models.ErrorCodes;
import com.wira.commons.shared.response.BaseResponse;

public class GenericRequestActionHandler extends
		AbstractActionHandler<GenericRequest, GenericResponse> {

	@Inject
	public GenericRequestActionHandler() {
	}

	@Override
	public void execute(GenericRequest action, BaseResponse actionResult,
			ExecutionContext execContext) throws ActionException {
		
		try{
//			Class clazz = Class.forName(action.getHandlerClass()); TODO
//			clazz.newInstance();
//			action.getValues();
			SMSIntegration integration = new SMSIntegration();
			Map<String, Value> values = action.getValues();
			Map<String, String> vals = new HashMap<>();
			for(String key: values.keySet()){
				Value val = values.get(key);
				if(val!=null){
					vals.put(key, val.getValue()==null? null: val.getValue().toString());
				}
			}
			
			String errorMessage = integration.execute(vals);
			if(errorMessage!=null && !errorMessage.isEmpty()){
				actionResult.setErrorMessage(errorMessage);
				actionResult.setErrorCode(ErrorCodes.INTEGRATION_SMS.ordinal());
			}
			
		}catch(Exception e){
			actionResult.setErrorCode(ErrorCodes.INTEGRATION_SMS.ordinal());
			e.printStackTrace();
			actionResult.setErrorMessage(e.getMessage());
		}
		
	}
	
	@Override
	public Class<GenericRequest> getActionType() {
		return GenericRequest.class;
	}
}
