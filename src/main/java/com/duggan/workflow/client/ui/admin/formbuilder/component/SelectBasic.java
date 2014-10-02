package com.duggan.workflow.client.ui.admin.formbuilder.component;

import java.util.ArrayList;
import java.util.List;

import com.duggan.workflow.client.ui.component.DropDownList;
import com.duggan.workflow.shared.model.DataType;
import com.duggan.workflow.shared.model.StringValue;
import com.duggan.workflow.shared.model.Value;
import com.duggan.workflow.shared.model.form.KeyValuePair;
import com.duggan.workflow.shared.model.form.Property;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class SelectBasic extends FieldWidget implements IsSelectionField{

	private static SelectBasicUiBinder uiBinder = GWT
			.create(SelectBasicUiBinder.class);
	
	private final Widget widget;

	@UiField Element lblEl;
	@UiField DropDownList<KeyValuePair> lstItems;
	@UiField HTMLPanel panelControls;
	@UiField InlineLabel lblComponent;
	@UiField SpanElement spnMandatory;
	
	interface SelectBasicUiBinder extends UiBinder<Widget, SelectBasic> {
	}

	public SelectBasic() {
		super();
		addProperty(new Property(MANDATORY, "Mandatory", DataType.CHECKBOX, id));
		addProperty(new Property(SQLDS, "Data Source", DataType.SELECTBASIC));
		addProperty(new Property(SQLSELECT, "Sql", DataType.STRINGLONG));
		addProperty(new Property(SELECTIONTYPE, "Reference", DataType.STRING));		
		widget= uiBinder.createAndBindUi(this);
		add(widget);
		UIObject.setVisible(spnMandatory, false);
		UIObject.setVisible(lblComponent.getElement(), false);
	}
	
	public SelectBasic(final Property property){
		this();
		lblEl.setInnerText(property.getCaption());
		
		setSelectionValues(property.getSelectionValues());
		Value val = property.getValue();
		if(val!=null)
			setValue(val.getValue());
		
		lstItems.addValueChangeHandler(new ValueChangeHandler<KeyValuePair>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<KeyValuePair> event) {
				KeyValuePair p = event.getValue();
				if(p==null){
					property.setValue(null);
				}else{
					Value value = new StringValue(null,property.getName(),p==null? null: p.getKey());
					property.setValue(value);
				}
				
			}
		});
		
	}

	@Override
	public FieldWidget cloneWidget() {
		return new SelectBasic();
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
		lstItems.setTitle(help);
	}
	
	@Override
	public Value getFieldValue() {
		KeyValuePair kvp = lstItems.getValue();
		
		String value = null;
		
		if(kvp!=null){
			value = kvp.getKey();
		}
		
		if(value!=null){
			return new StringValue(field.getLastValueId(), field.getName(), value);
		}
		
		return null;
	}
	
	@Override
	protected DataType getType() {
		return DataType.SELECTBASIC;
	}
	
	@Override
	public void setReadOnly(boolean isReadOnly) {		
		this.readOnly = isReadOnly || isComponentReadOnly();
		
		UIObject.setVisible(lstItems.getElement(),!this.readOnly);
		UIObject.setVisible(lblComponent.getElement(), this.readOnly);
		UIObject.setVisible(spnMandatory, (!this.readOnly && isMandatory()));
	}

	@Override
	public void setSelectionValues(List<KeyValuePair> values) {
		if(designMode && (getPropertyValue(SQLDS)!=null || getPropertyValue(SQLSELECT)!=null)){
			//design mode
			values =new ArrayList<KeyValuePair>();
			 
			//design mode; and loaded from a different system/subsystem
			//the user doesnt see these loaded values i.e the drop down cant show the values
			//loaded in design mode
			
			//we need to disable this capability so that in case the user changes from db loading
			//to manual listing, the web browser does not sending data loaded from another subsystem
			// as new data provided manually
		}
		
		if(values!=null){
			lstItems.setItems(values);
		}
		
		//design mode values set here before save is called
		//iff these we manually entered/ not referenced from another ds
		
		field.setSelectionValues(values);
		
	}

	@Override
	public List<KeyValuePair> getValues() {
		return lstItems.values();
	}
	
	@Override
	public void setValue(Object value) {
		super.setValue(value);
		if(value==null){
			return;
		}
		
		String key = (String)value;
		lstItems.setValueByKey(key);
		
		KeyValuePair keyValuePair = lstItems.getValue();		
		if(keyValuePair!=null){
			lblComponent.setText(keyValuePair.getValue());
		}else{
			lblComponent.setText(value.toString());
		}
	}
	
	@Override
	public Widget getComponent(boolean small) {
				
//		if(!readOnly){
//			return lstItems;
//		}
		return panelControls;
	}
	
}
