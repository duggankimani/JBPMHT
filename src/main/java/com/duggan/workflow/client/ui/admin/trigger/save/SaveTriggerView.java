package com.duggan.workflow.client.ui.admin.trigger.save;

import java.util.List;

import com.duggan.workflow.client.ui.component.DropDownList;
import com.duggan.workflow.client.ui.component.IssuesPanel;
import com.duggan.workflow.client.ui.component.TextArea;
import com.duggan.workflow.client.ui.component.TextField;
import com.duggan.workflow.shared.model.Trigger;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class SaveTriggerView extends ViewImpl implements
		SaveTriggerPresenter.ISaveTriggerView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, SaveTriggerView> {
	}

	
	@UiField TextField txtName;
	@UiField TextArea txtImports;
	@UiField TextArea txtScript;
	@UiField IssuesPanel issues;
	@UiField DropDownList<Trigger> lstTrigger;
	@UiField DivElement divTriggers;
	
	@Inject
	public SaveTriggerView(final Binder binder) {
		widget = binder.createAndBindUi(this);
		
		lstTrigger.addValueChangeHandler(new ValueChangeHandler<Trigger>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Trigger> event) {
				Trigger trigger = event.getValue();
				clear();
				if(trigger!=null){
					setTrigger(trigger);
				}
			}
		});
	}
	
	@Override
	public Trigger getTrigger(){
		Trigger trigger = new Trigger();
		trigger.setName(txtName.getValue());
		trigger.setScript(txtScript.getValue());
		trigger.setImports(txtImports.getValue());
		
		return trigger;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clear() {
		txtName.setValue(null);
		txtScript.setValue(null);
		txtImports.setValue(null);
		issues.clear();
	}

	@Override
	public void setTrigger(Trigger doc) {
		txtName.setValue(doc.getName());
		txtScript.setValue(doc.getScript());	
		txtImports.setValue(doc.getImports());
	}

	@Override
	public boolean isValid() {
		issues.clear();
		boolean isValid = true;
		
		if(txtName.getValue()==null || txtName.getValue().isEmpty()){
			isValid=false;
			issues.addError("Name is mandatory");
		}
		
		return isValid;
	}
	
	boolean isNullOrEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}

	@Override
	public void setTriggers(List<Trigger> triggers) {
		
		lstTrigger.setItems(triggers);
	}

	@Override
	public void showTriggers(boolean show) {
		if(show){
			divTriggers.removeClassName("hide");
		}else{
			divTriggers.addClassName("hide");
		}
	}
	
}
