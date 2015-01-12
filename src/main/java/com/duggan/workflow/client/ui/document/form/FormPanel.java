package com.duggan.workflow.client.ui.document.form;

import static com.duggan.workflow.client.ui.util.DateUtils.MONTHDAYFORMAT;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.duggan.workflow.client.ui.admin.formbuilder.HasProperties;
import com.duggan.workflow.client.ui.admin.formbuilder.component.FieldWidget;
import com.duggan.workflow.client.ui.admin.formbuilder.component.SingleButton;
import com.duggan.workflow.client.ui.admin.formbuilder.component.TextArea;
import com.duggan.workflow.client.ui.component.IssuesPanel;
import com.duggan.workflow.client.ui.delegate.FormDelegate;
import com.duggan.workflow.client.ui.util.DateUtils;
import com.duggan.workflow.shared.model.BooleanValue;
import com.duggan.workflow.shared.model.DataType;
import com.duggan.workflow.shared.model.DateValue;
import com.duggan.workflow.shared.model.Doc;
import com.duggan.workflow.shared.model.DocStatus;
import com.duggan.workflow.shared.model.Document;
import com.duggan.workflow.shared.model.DocumentLine;
import com.duggan.workflow.shared.model.GridValue;
import com.duggan.workflow.shared.model.MODE;
import com.duggan.workflow.shared.model.StringValue;
import com.duggan.workflow.shared.model.Value;
import com.duggan.workflow.shared.model.form.Field;
import com.duggan.workflow.shared.model.form.Form;
import com.duggan.workflow.shared.model.form.FormModel;
import com.duggan.workflow.shared.model.form.Property;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.LegendElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Runtime form
 * 
 * @author duggan
 *
 */
public class FormPanel extends Composite {

	private static FormPanelUiBinder uiBinder = GWT
			.create(FormPanelUiBinder.class);

	interface FormPanelUiBinder extends UiBinder<Widget, FormPanel> {
	}

	@UiField HTMLPanel panelFields;
	@UiField LegendElement divFormCaption;
	@UiField SpanElement divFormHelp;
	@UiField IssuesPanel issues;
	boolean isReadOnly=true;

	@UiField SpanElement spnCreated;
	@UiField SpanElement spnDeadline;
	
	FormDelegate formDelegate = new FormDelegate();
	MODE mode = MODE.VIEW;
	
	public FormPanel(Form form,Doc doc){		
		this(form,doc, MODE.VIEW);
	}
	
	public FormPanel(Form form,Doc doc,MODE mode) {
		initWidget(uiBinder.createAndBindUi(this));
		this.mode = mode;
		
		form.getCaption();
		divFormHelp.setInnerText("");
		if(form.getProperties()!=null)
		for(Property prop: form.getProperties()){
			if(prop.getName()!=null){
				if(prop.getName().equals(HasProperties.CAPTION)){
					Value val = prop.getValue();
					if(val!=null){
						divFormCaption.setInnerHTML(((StringValue)val).getValue());
					}
					
				}
				if(prop.getName().equals(HasProperties.HELP)){
					Value val = prop.getValue();
					if(val!=null){
						divFormHelp.setInnerHTML(((StringValue)val).getValue());
					}
				}
			}
		}
		
		List<Field> fields = form.getFields();
		Collections.sort(fields, new Comparator<FormModel>() {
			public int compare(FormModel o1, FormModel o2) {
				Field field1 = (Field)o1;
				Field field2 = (Field)o2;
				
				Integer pos1 = field1.getPosition();
				Integer pos2 = field2.getPosition();
				
				return pos1.compareTo(pos2);
			};
			
		});
		
		bind(fields, doc);
		
	}
	
	void bind(List<Field> fields, Doc doc){
		Map<String, Value> values = doc.getValues();
		
		for(Field field: fields){
			String name = field.getName();
			field.setDocId(doc.getId()+""); //Add DocId to all field
			
			if(name==null || name.isEmpty()){
				continue;
			}
						
			if(field.getType()==DataType.GRID){
				List<DocumentLine> lines=doc.getDetails().get(field.getName());
				if(lines!=null){
					GridValue value = new GridValue();
					value.setKey(field.getName());
					value.setCollectionValue(lines);
					//System.err.println(">>"+lines.size());
					field.setValue(value);
				}
				
				bind(field);
				continue;
				
			}else if(field.getType()==DataType.BUTTON){
				String submitType = field.getPropertyValue(SingleButton.SUBMITTYPE);
				if(submitType!=null){
					if(submitType.equals("CompleteProcess")){
						//Override default complete
						//getView().overrideDefaultCompleteProcess();
					}else if(submitType.equals("StartProcess")){
						//Override default start
						//getView().overrideDefaultStartProcess();
					}
					
				}
				
				if(doc instanceof Document){
					DocStatus status = ((Document)doc).getStatus();
					if(status==DocStatus.DRAFTED){
						Property prop = new Property(HasProperties.READONLY, "Read only", DataType.BOOLEAN);
						prop.setValue(new BooleanValue(null, HasProperties.READONLY, false));
						field.getProperties().add(prop);
					}
				}
			}
			
			Value value = values.get(name);
			field.setValue(value);
			
			if(value==null){
				if(name.equals("subject")){
					value = new StringValue(doc.getCaseNo());
				}
				
				if(name.equals("description")){
					value = new StringValue(doc.getDescription());
				}
				
				if(name.equals("docDate")){
					value = new DateValue(doc.getCreated());
				}
				field.setValue(value);
			}
		
			//Bind this field to the form
			bind(field);
		}
	}
	
	public void bind(Field field){

		FieldWidget fieldWidget = FieldWidget.getWidget(field.getType(), field, false);
		if(mode==MODE.VIEW){
			//set read only 
			fieldWidget.setReadOnly(true);
		}
		
		if(fieldWidget instanceof TextArea){
			((TextArea) fieldWidget).getContainer().removeStyleName("hidden");
		}
		
		panelFields.add(fieldWidget);
	}
	
	public void setCreated(Date created){

		if (created!= null){
			String timeDiff =  MONTHDAYFORMAT.format(created);//DateUtils.getTimeDifferenceAsString(created);
			spnCreated.setInnerText(timeDiff);
			//TIMEFORMAT12HR.format(created)+" ("+timeDiff+" )");
		}
			
	}
	
	public void setCompletedOn(Date completedOn){
		if (completedOn!= null){
			String timeDiff =  MONTHDAYFORMAT.format(completedOn);//DateUtils.getTimeDifferenceAsString(created);
			spnDeadline.setInnerText("Done "+timeDiff);
			//TIMEFORMAT12HR.format(created)+" ("+timeDiff+" )");
		}
	}
	
	public void setDeadline(Date endDateDue) {
		if(endDateDue==null){
			return;
		}

		String deadline="";
		String timeDiff =  DateUtils.getTimeDifferenceAsString(endDateDue);
		
		if(timeDiff != null){
			deadline =  MONTHDAYFORMAT.format(endDateDue);
					//TIMEFORMAT12HR.format(endDateDue)+" ("+timeDiff+" )";
		}

		if(DateUtils.isOverdue(endDateDue)){
			spnDeadline.removeClassName("hidden");
			spnDeadline.getStyle().setColor("#DD4B39");
		}else if(DateUtils.isDueInMins(30, endDateDue)){
			spnDeadline.removeClassName("hidden");
			spnDeadline.getStyle().setColor("#F89406");
		}
		
		spnDeadline.setInnerText("Due "+deadline);
	}

	public boolean isValid(){
		boolean isValid = formDelegate.isValid(issues, panelFields);;
		
		if(!isValid){
			issues.getElement().scrollIntoView();
		}
		return isValid;
	}
	
	public Map<String, Value> getValues(){
		return formDelegate.getValues(panelFields);		
	}
	
	public void setReadOnly(boolean readOnly){
		this.isReadOnly = readOnly;
		formDelegate.setReadOnly(readOnly, (ComplexPanel)panelFields);
	}

	public boolean isReadOnly() {
		return isReadOnly;
	}
	
}
