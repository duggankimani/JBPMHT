package com.duggan.workflow.shared.model.form;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.duggan.workflow.shared.model.Value;
import com.wira.commons.shared.models.Listable;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class FormModel implements Serializable, Listable {

	public static final String FORMMODEL = "FORMMODEL";
	public static final String FIELDMODEL = "FIELDMODEL";
	public static final String PROPERTYMODEL = "PROPERTYMODEL";
	
	@XmlTransient
	protected Long Id;
	private String refId;
	protected String name;
	protected String caption;
	private ArrayList<KeyValuePair> props = new ArrayList<KeyValuePair>();
	
	public FormModel() {
		
	}
	
	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if(name!=null){
			this.name = name.trim();
		}else{
			this.name = name;
		}
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	protected String getStringValue(Value value) {
		return value==null? null: value.getValue()==null? null: value.getValue().toString();
	}

	@Override
	public String getDisplayName() {
		return caption;
	}

	public ArrayList<KeyValuePair> getValues() {
		return props;
	}

	public void setValues(ArrayList<KeyValuePair> values) {
		this.props = values;
	}
	
	public void addValue(KeyValuePair pair){
		if(props.contains(pair)){
			//Replace existing pair
			props.remove(pair);
		}
		
		if(pair.getValue()==null || pair.getValue().trim().isEmpty()){
			//Ignore empty
			return;
		}
		props.add(pair);
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}
}
