package com.duggan.workflow.shared.model.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import com.wira.commons.shared.models.Listable;

@XmlSeeAlso({KeyValuePair.class,Property.class,Field.class})
@XmlRootElement(name="form")
@XmlAccessorType(XmlAccessType.FIELD)
public class Form extends FormModel implements Listable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlTransient
	private ArrayList<Field> fields;
	
	@XmlTransient
	private Long processDefId;
	
	private String processRefId;
	
	/**
	 * HashMap<Parent,Children> dependency HashMap
	 */
	@XmlTransient
	private HashMap<String, ArrayList<String>> dependencies = new HashMap<String, ArrayList<String>>();
	
	@XmlTransient
	private ArrayList<String> formulae = new ArrayList<String>();
	
	public Form() {
	}
	
	public Form(String refId, String formId, String caption) {
		setRefId(refId);
		setName(formId);
		setCaption(caption);
	}

	public void setProperties(ArrayList<Property> properties) {
		for(Property property: properties){
			addValue(new KeyValuePair(property.getName(), getStringValue(property.getValue())));
		}
	}

	public ArrayList<Field> getFields() {
		return fields;
	}

	public void setFields(ArrayList<Field> fields) {
		this.fields = fields;
	}

	@Override
	public String getDisplayName() {
		return getCaption();
	}
	
	public Form clone(){
		return clone(false);
	}
	
	public Form clone(boolean fullClone){
		Form form = new Form(null, null, null);
		
		form.setProps(getProps());
		
		if(fields!=null)
		for(Field field: fields){
			form.addField(field.clone());
		}
		
		return form;
	}

	public void addField(Field field) {
		if(fields==null)
			fields = new ArrayList<Field>();
		
		if(fields.contains(field)){
			fields.remove(field);
		}
		
		fields.add(field);
	}
	
	@Override
	public String toString() {
		return "[Form Id="+getRefId()
				+",Name="+name
				+",caption="+caption+"]";
	}

	public Long getProcessDefId() {
		return processDefId;
	}

	public void setProcessDefId(Long processDefId) {
		this.processDefId = processDefId;
	}

	public void addFieldDependency(ArrayList<String> parentFields, String childField) {
		for(String parentField: parentFields){
			ArrayList<String> children = dependencies.get(parentField);
			if(children==null){
				children = new ArrayList<String>();
			}
			children.add(childField);
			dependencies.put(parentField, children);
		}
	}

	public HashMap<String, ArrayList<String>> getDependencies() {
		return dependencies;
	}

	public void setDependencies(HashMap<String, ArrayList<String>> dependencies) {
		this.dependencies = dependencies;
	}

	public String getProcessRefId() {
		return processRefId;
	}

	public void setProcessRefId(String processRefId) {
		this.processRefId = processRefId;
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public ArrayList<String> getFormulae() {
		return formulae;
	}

	public void setFormulae(ArrayList<String> formulae) {
		this.formulae = formulae;
	}

}
