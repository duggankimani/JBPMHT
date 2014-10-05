package com.duggan.workflow.client.ui.admin.formbuilder.component;

import com.duggan.workflow.client.ui.component.DoubleField;
import com.duggan.workflow.client.ui.events.OperandChangedEvent;
import com.duggan.workflow.client.util.AppContext;
import com.duggan.workflow.client.util.ENV;
import com.duggan.workflow.shared.model.DataType;
import com.duggan.workflow.shared.model.DoubleValue;
import com.duggan.workflow.shared.model.StringValue;
import com.duggan.workflow.shared.model.Value;
import com.duggan.workflow.shared.model.form.KeyValuePair;
import com.duggan.workflow.shared.model.form.Property;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class NumberField extends FieldWidget{
	
	private static NumberFieldUiBinder uiBinder = GWT
			.create(NumberFieldUiBinder.class);

	interface NumberFieldUiBinder extends UiBinder<Widget, NumberField> {
	}

	@UiField Element lblEl;
	@UiField DoubleField txtComponent;
	@UiField InlineLabel lblReadOnly;
	@UiField HTMLPanel panelControls;
	@UiField SpanElement spnMandatory;
	
	private final Widget widget;
	
	
	public NumberField(){
		super();
		addProperty(new Property(MANDATORY, "Mandatory", DataType.CHECKBOX, id));
		addProperty(new Property(PLACEHOLDER, "Place Holder", DataType.STRING, id));
		addProperty(new Property(READONLY, "Read Only", DataType.CHECKBOX));
		addProperty(new Property(FORMULA, "Formula", DataType.STRING));
		addProperty(new Property(ALIGNMENT, "Alignment", DataType.SELECTBASIC, 
				new KeyValuePair("left", "Left"),
				new KeyValuePair("center", "Center"),
				new KeyValuePair("right", "Right")));

		widget = uiBinder.createAndBindUi(this);
		add(widget);
		UIObject.setVisible(spnMandatory, false);

	}
	
	public NumberField(final Property property){
		this();
		lblEl.setInnerHTML(property.getCaption());
		txtComponent.setName(property.getName());
		
		Value textValue = property.getValue();
		String text = textValue==null? "": 
			textValue.getValue()==null? "": textValue.getValue().toString();
		
		txtComponent.setText(text);
		txtComponent.setClass("input-large"); //Smaller TextField
		designMode=true;
		
		final String name = property.getName();
		
		txtComponent.addValueChangeHandler(new ValueChangeHandler<Double>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				Double value  = event.getValue();
				Value previousValue = property.getValue();
				if(previousValue==null){
					previousValue = new StringValue();
				}else{
					if(event.getValue().equals(previousValue.getValue())){
						return;
					}
				}
				
				
				previousValue.setValue(value);
				property.setValue(previousValue);
				
				/**
				 * This allows visual properties including Caption, Place Holder, help to be 
				 * Synchronised with the form field, so that the changes are observed immediately
				 * 
				 * All other Properties need not be synched this way 
				 */
				if(name.equals(CAPTION) || name.equals(PLACEHOLDER) || name.equals(HELP)){				
					firePropertyChanged(property, value);
				}
				
			}
		});
		
		//name.equals()

	}

	
	protected void registerValueChangeHandler(){
		txtComponent.addValueChangeHandler(new ValueChangeHandler<Double>() {

			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				Double value = event.getValue();
				//ENV.setContext(field.getName(),field.getQualifiedName(), value);
				ENV.setContext(field, value);
				//System.err.println("Change event fired -> "+value);
				//fire based on actual name-- other fields are aware of actuals
				AppContext.fireEvent(new OperandChangedEvent(field.getDocSpecificName(), value, field.getDetailId()));
			}
			
		});
	}
	
	@Override
	public FieldWidget cloneWidget() {
		return new NumberField();
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
	
	@Override
	public void setReadOnly(boolean isReadOnly) {
		this.readOnly = isReadOnly || isComponentReadOnly();
		
		UIObject.setVisible(txtComponent.getElement(),!this.readOnly);
		UIObject.setVisible(lblReadOnly.getElement(), this.readOnly);
		
		UIObject.setVisible(spnMandatory, (!this.readOnly && isMandatory()));
	}

	@Override
	public Widget getComponent(boolean small) {
				
		if(!readOnly)
		if(small){
			txtComponent.setClass("input-medium");
		}
		return panelControls;
	}
	
	@Override
	protected void setAlignment(String alignment) {		
		txtComponent.getElement().getStyle().setTextAlign(TextAlign.valueOf(alignment.toUpperCase()));		
		panelControls.getElement().getStyle().setTextAlign(TextAlign.valueOf(alignment.toUpperCase()));
		
	}

	
	@Override
	protected DataType getType() {
		return DataType.DOUBLE;
	}
	
	@Override
	public Value getFieldValue() {
		Double value = txtComponent.getValue();
		
		if(value==null)
			return null;
		
		
		return new DoubleValue(field.getLastValueId(), field.getName(),value);
	}
	
	@Override
	public void setValue(Object value) {
		if(value!=null){
			
			if(!(value instanceof Double)){
				try{
					value = new Double(value.toString());
				}catch(Exception e){return;}
			}
			
			ENV.setContext(field, (Double)value);
			txtComponent.setValue((Double)value);
			
			NumberFormat fmt = NumberFormat.getDecimalFormat();
		    String formatted = fmt.format((Double)value);
			lblReadOnly.setText(formatted);
		}

	}
	
	
	@Override
	protected void onLoad() {
		super.onLoad();
		if(field.getDocId()!=null)
			txtComponent.addValueChangeHandler(new ValueChangeHandler<Double>() {
				@Override
				public void onValueChange(ValueChangeEvent<Double> event) {
						ENV.setContext(field, event.getValue());
				}
			});
	}
	
	
}
