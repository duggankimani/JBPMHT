package com.duggan.workflow.client.ui.admin.formbuilder.component;

import java.util.ArrayList;

import com.duggan.workflow.client.ui.events.PropertyChangedEvent;
import com.duggan.workflow.client.util.AppContext;
import com.duggan.workflow.shared.model.form.KeyValuePair;
import com.duggan.workflow.shared.model.form.Property;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class InputSelection extends Composite {

	private static InputSelectionUiBinder uiBinder = GWT
			.create(InputSelectionUiBinder.class);

	interface InputSelectionUiBinder extends UiBinder<Widget, InputSelection> {
	}
	
	@UiField VerticalPanel vPanel;
	TextBox lastComponent;
	Property property;

	public InputSelection(Property property) {
		this.property = property;
		initWidget(uiBinder.createAndBindUi(this));
		addComponent(cloneTextBox());
	}

	public TextBox cloneTextBox(){
		final TextBox txtBox = new TextBox();
		txtBox.addStyleName("input-xlarge");
		txtBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			boolean added = false; 
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String val = event.getValue();

				PropertyChangedEvent evt = new PropertyChangedEvent(property.getFieldRefId(), property.getName(), getValues(), true);
				AppContext.getEventBus().fireEventFromSource(evt, this);
				
				if(val.isEmpty()){
					txtBox.removeFromParent();
				}
				if(!added){
					added=true;
					addEmptyComponent(txtBox);
				}
			}
		});
		return txtBox;
	}
	
	protected void addEmptyComponent(TextBox source) {

		if(! (lastComponent == source) ){
			return;
		}
		
		addComponent(cloneTextBox());
	}

	protected void addComponent(TextBox txtBox) {
		this.lastComponent = txtBox;
		vPanel.add(txtBox);
	}

	public ArrayList<KeyValuePair> getValues(){
		int count = vPanel.getWidgetCount();
		ArrayList<KeyValuePair> ArrayList = new ArrayList<KeyValuePair>();
		
		for(int i=0; i<count; i++){
			TextBox txtBox= (TextBox)vPanel.getWidget(i);
			String val = txtBox.getValue();
			
			if(val.isEmpty()){
				continue;
			}
			
			KeyValuePair pair = new KeyValuePair();
			pair.setKey(val);
			pair.setValue(val);
			ArrayList.add(pair);
		}
		
		return ArrayList;
	}
	
	public void setValues(ArrayList<KeyValuePair> values){
		vPanel.clear();
		if(values!=null){
			
			for(KeyValuePair pair: values){
				TextBox txtBox = cloneTextBox();
				txtBox.setValue(pair.getValue());
				txtBox.setName(pair.getName());
				addComponent(txtBox);
			}
			
		}
		addComponent(cloneTextBox());
	}
	
}
