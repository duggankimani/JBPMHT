package com.duggan.workflow.client.ui.admin.formbuilder.component;

import com.duggan.workflow.client.ui.events.PropertyChangedEvent;
import com.duggan.workflow.client.util.AppContext;
import com.duggan.workflow.shared.model.DataType;
import com.duggan.workflow.shared.model.StringValue;
import com.duggan.workflow.shared.model.Value;
import com.duggan.workflow.shared.model.form.Property;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class TextField extends FieldWidget {

	private static TextFieldUiBinder uiBinder = GWT
			.create(TextFieldUiBinder.class);

	interface TextFieldUiBinder extends UiBinder<Widget, TextField> {
	}

	@UiField Element lblEl;
	@UiField com.duggan.workflow.client.ui.component.TextField txtComponent;
	
	private final Widget widget;
	
	public TextField() {
		super();
		addProperty(new Property("PLACEHOLDER", "Place Holder", DataType.STRING, id));

		widget = uiBinder.createAndBindUi(this);
		add(widget);
	}
	
	public TextField(final Property property) {
		this();
		lblEl.setInnerHTML(property.getCaption());
		txtComponent.setName(property.getName());
		
		Value textValue = property.getValue();
		String text = textValue==null? "": textValue.getValue().toString();
		
		txtComponent.setText(text);
		txtComponent.setClass("input-large"); //Smaller TextField
		isPropertyField=true;
		
		final String name = property.getName();
		
		txtComponent.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String value  = event.getValue();
				property.setValue(new StringValue(value));
				
				if(name.equals("CAPTION") || name.equals("PLACEHOLDER") || name.equals("HELP"))
				AppContext.getEventBus().fireEventFromSource(
						new PropertyChangedEvent(property.getFieldId(),property.getName(), value), this);
				//AppContext.getEventBus().fireEvent(new );
				//AppContext.getEventBus().fireEvent(event);
				
			}
		});
		//initPropertyWidget();
	}

	@Override
	public FieldWidget cloneWidget() {
		return new TextField();
	}
	
	@Override
	protected void setCaption(String caption) {
		lblEl.setInnerHTML(caption);
	}
	
	@Override
	protected void setPlaceHolder(String placeHolder) {
		txtComponent.setPlaceholder(placeHolder);
	}
	
	@Override
	protected void setHelp(String help) {
		txtComponent.setTitle(help);
	}
	
}
