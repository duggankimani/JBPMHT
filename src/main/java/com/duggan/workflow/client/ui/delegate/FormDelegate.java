package com.duggan.workflow.client.ui.delegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.duggan.workflow.client.ui.admin.formbuilder.component.FieldWidget;
import com.duggan.workflow.client.ui.admin.formbuilder.component.HTMLForm;
import com.duggan.workflow.client.ui.component.IssuesPanel;
import com.duggan.workflow.shared.model.Value;
import com.duggan.workflow.shared.model.form.Field;
import com.duggan.workflow.shared.model.form.FormModel;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

public class FormDelegate {

	public boolean isValid(IssuesPanel issues, ComplexPanel panelFields) {
		// txtDescription.getValue();
		boolean isValid = true;
		issues.clear();
		
		int fields = panelFields.getWidgetCount();
		
		for(int i=0; i<fields; i++){
			Widget widget = panelFields.getWidget(i);
			
			if(!(widget instanceof FieldWidget)){
				continue;
			}
			
			FieldWidget fieldWidget = (FieldWidget)widget;
			if(!fieldWidget.isValid() && fieldWidget.isVisible()){
				isValid=false;
				//issues.addError("'"+fieldWidget.getField().getCaption()+"' is Mandatory");
			}
			
		}
		
		return isValid;
	}
	
	/**
	 * Read field values
	 * 
	 * @param panelFields
	 * @return
	 */
	public HashMap<String, Value> getValues(ComplexPanel panelFields){
		HashMap<String,Value> values = new HashMap<String, Value>();
		
		int fields = panelFields.getWidgetCount();
		
		for(int i=0; i<fields; i++){
			Widget widget = panelFields.getWidget(i);
			if(!(widget instanceof FieldWidget)){
				continue;
			}
			
			FieldWidget fieldWidget = (FieldWidget)widget;
			Field field = fieldWidget.getField();
			//DISABLED BY DUGGAN 15th/Sep/2015 - Due to Field Triggers that may update readonly fields
			
//			if(fieldWidget.isReadOnly() && !fieldWidget.isFormularField()){
//				continue;
//			}
			
			Value fieldValue = fieldWidget.getFieldValue();
			if(fieldValue!=null) {
				assert field.getName()!=null;
				assert !field.getName().isEmpty();
				fieldValue.setKey(field.getName());
			}		
			
			//HTML Forms
			if(fieldWidget instanceof HTMLForm){
				ArrayList<Value> htmlFormValues = ((HTMLForm)fieldWidget).getFieldValues();
				for(Value v: htmlFormValues){
					values.put(v.getKey(), v);
				}
			}
			values.put(field.getName(), fieldValue);
		}
		
		return values;
		
	}
	
	/**
	 * Set form fields
	 * 
	 * @param fields
	 * @param panelFields
	 */
	public void setFields(ArrayList<Field> fields, ComplexPanel panelFields) {
		Collections.sort(fields, new Comparator<FormModel>() {
			public int compare(FormModel o1, FormModel o2) {
				Field field1 = (Field)o1;
				Field field2 = (Field)o2;
				
				Integer pos1 = field1.getPosition();
				Integer pos2 = field2.getPosition();
				
				return pos1.compareTo(pos2);
			};
			
		});
		for(Field field: fields){
			FieldWidget fieldWidget = FieldWidget.getWidget(field.getType(), field, false);
			panelFields.add(fieldWidget);
			
		}
	}

	boolean isNullOrEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}


	public void setReadOnly(boolean isReadOnly, ComplexPanel panelFields) {
		int fields = panelFields.getWidgetCount();
		
		for(int i=0; i<fields; i++){
			Widget widget = panelFields.getWidget(i);
			if(!(widget instanceof FieldWidget)){
				continue;
			}
			
			FieldWidget fieldWidget = (FieldWidget)widget;
			fieldWidget.setReadOnly(isReadOnly);
		}	
	}

}
