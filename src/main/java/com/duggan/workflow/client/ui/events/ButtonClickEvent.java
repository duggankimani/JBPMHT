package com.duggan.workflow.client.ui.events;

import java.util.HashMap;

import com.duggan.workflow.shared.model.Value;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class ButtonClickEvent extends
		GwtEvent<ButtonClickEvent.ButtonClickHandler> {

	public static Type<ButtonClickHandler> TYPE = new Type<ButtonClickHandler>();
	private String requestType;
	private boolean isValidateForm=true;
	private String trigger =null;

	public interface ButtonClickHandler extends EventHandler {
		void onButtonClick(ButtonClickEvent event);
	}
	
	private HashMap<String,Value> values = new HashMap<String,Value>();

	public ButtonClickEvent(String requestType, HashMap<String,Value> values) {
		this.requestType = requestType;
		this.values = values;
	}

	public String getRequestType() {
		return requestType;
	}

	@Override
	protected void dispatch(ButtonClickHandler handler) {
		handler.onButtonClick(this);
	}

	@Override
	public Type<ButtonClickHandler> getAssociatedType() {
		return TYPE;
	}

	public static Type<ButtonClickHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source, String requestType, HashMap<String,Value> values) {
		source.fireEvent(new ButtonClickEvent(requestType, values));
	}

	public HashMap<String, Value> getValues() {
		return values;
	}

	public void setValues(HashMap<String, Value> values) {
		this.values = values;
	}

	public boolean isValidateForm() {
		return isValidateForm;
	}

	public void setValidateForm(boolean isValidateForm) {
		this.isValidateForm = isValidateForm;
	}

	public String getTrigger() {
		return trigger;
	}

	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}
}
