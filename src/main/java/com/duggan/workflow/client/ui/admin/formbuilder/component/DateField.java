package com.duggan.workflow.client.ui.admin.formbuilder.component;

import java.util.Date;

import com.duggan.workflow.client.ui.component.DateInput;
import com.duggan.workflow.client.ui.util.DateUtils;
import com.duggan.workflow.shared.model.DataType;
import com.duggan.workflow.shared.model.DateValue;
import com.duggan.workflow.shared.model.Value;
import com.duggan.workflow.shared.model.form.Property;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class DateField extends FieldWidget {

	private static DateFieldUiBinder uiBinder = GWT
			.create(DateFieldUiBinder.class);

	interface DateFieldUiBinder extends UiBinder<Widget, DateField> {
	}
	
	private final Widget widget;
	
	@UiField Element lblEl;
	@UiField DateInput dateBox;
	@UiField HTMLPanel panelControls;
	@UiField InlineLabel lblComponent;
	@UiField SpanElement spnMandatory;
	
	public DateField() {
		super();
		addProperty(new Property(MANDATORY, "Mandatory", DataType.CHECKBOX, id));
		addProperty(new Property("DATEFORMAT", "Date Format", DataType.STRING));
		addProperty(new Property(READONLY, "Read Only", DataType.CHECKBOX));
		widget = uiBinder.createAndBindUi(this);	
		add(widget);
		UIObject.setVisible(spnMandatory, false);
	}
	
	/**
	 * This is an edit property field - This is a field
	 * used to edit a single property
	 * 
	 * @param property
	 */
	public DateField(Property property) {
		this();
		
		String caption = property.getCaption();
		String name = property.getName();
		Value val = property.getValue();
		designMode=false;
		
	}

	@Override
	public FieldWidget cloneWidget() {
		return new DateField();
	}	
	
	@Override
	protected void setCaption(String caption) {
		lblEl.setInnerHTML(caption);
	}
	
	@Override
	protected void setPlaceHolder(String placeHolder) {
		//txtComponent.setPlaceholder(placeHolder);
	}
	
	@Override
	protected void setHelp(String help) {
		dateBox.setTitle(help);
	}
	
	@Override
	protected DataType getType() {
		return DataType.DATE;
	}
	
	@Override
	public Value getFieldValue() {
		Date dt = dateBox.getDate();
		
		if(dt==null){
			return null;
		}
		
		return new DateValue(field.getLastValueId(), field.getName(), dt);
	}
	
	@Override
	public void setValue(Object value) {
		super.setValue(value);
		if(value!=null){
			dateBox.setValue((Date)value);
			lblComponent.setText(DateUtils.DATEFORMAT.format((Date)value));
		}
	}
	
	@Override
	public void setReadOnly(boolean isReadOnly) {
		this.readOnly = isReadOnly || isComponentReadOnly();
		
		UIObject.setVisible(dateBox.getElement(),!this.readOnly);
		UIObject.setVisible(lblComponent.getElement(), this.readOnly);
		
		UIObject.setVisible(spnMandatory, (!this.readOnly && isMandatory()));
	}
	
	@Override
	public Widget getComponent(boolean small) {
		
		if(!readOnly)
			if(small){
				dateBox.setStyle("input-small");
			}
		
		return panelControls;
	}
}
