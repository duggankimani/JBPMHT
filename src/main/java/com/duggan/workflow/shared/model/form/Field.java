package com.duggan.workflow.shared.model.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.duggan.workflow.shared.model.DataType;
import com.duggan.workflow.shared.model.Value;

/**
 * Represents A Form Field
 * 
 * @author duggan
 *
 */
public class Field extends FormModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Property> properties = new ArrayList<Property>();
	private List<KeyValuePair> selectionValues = new ArrayList<KeyValuePair>();
	private Value value;
	private DataType type;
	private Long formId=-1L;
	private int position;
	private Long parentId;
	private List<Field> fields = new ArrayList<Field>();
	private transient String gridName="";
	private transient Long lineRefId = null;
	private transient String docId="";
	
	public Field() {
		docId="TempD";
	}
	
	public List<Property> getProperties() {
		return properties;
	}
	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}
	public Value getValue() {
		return value;
	}
	public void setValue(Value value) {
		this.value = value;
	}
	public DataType getType() {
		return type;
	}
	public void setType(DataType type) {
		this.type = type;
	}
	public Long getFormId() {
		return formId;
	}
	public void setFormId(Long formId) {
		this.formId = formId;
	}
	
	@Override
	public String toString() {
		String str = "[Id= "+getId()+";" +
				" FormId = "+getFormId()+";"+
				" Name = "+getName()+";"+
				" Caption = "+getCaption()+";"+
				" Type = "+type+";" +
				" Position = "+position+"]";
		
		return str;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public List<KeyValuePair> getSelectionValues() {
		return selectionValues;
	}

	public void setSelectionValues(List<KeyValuePair> selectionValues) {
		this.selectionValues = selectionValues;
	}

	public List<Field> getFields() {
		return fields;
	}

	public boolean isDetailField() {
		return type == DataType.GRID;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public void addField(Field field) {
		fields.add(field);
	}

	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof Field)) {
			return false;
		}
		
		if(Id!=null){
			return Id.equals(((Field)obj).Id);
		}
		
		return super.equals(obj);
	}

	public boolean contains(Field retrieved) {

		return fields.contains(retrieved);
	}

	public void setFields(List<Field> fieldList) {
		this.fields = fieldList;
	}
	
	public Field clone(){
		return clone(false);
	}
	
	public Field clone(boolean fullClone){
		Field field = new Field();
		field.setCaption(caption);
		field.setName(name);
		field.setPosition(position);
		field.setType(type);
		field.setFormId(null);
		field.setId(null);
		
		if(fullClone){
			field.setFormId(formId);
			field.setId(Id);
			field.setParentId(parentId);
			field.setDocId(docId);
		}
	
		if(value!=null){
			field.setValue(value.clone(fullClone));
		}
		
		field.setSelectionValues(getSelectionValues());
		
		for(Field fld: fields){
			field.addField(fld.clone());
		}
		
		for(Property p: properties){
			field.addProperty(p.clone(fullClone));
		}
		
		return field;
	}

	private void addProperty(Property prop) {
		properties.add(prop);
	}
	
	public void sortFields(){
		if(fields!=null && fields.size()>1){
			//Grid Field order Fields
			Collections.sort(fields, new Comparator<Field>() {
				@Override
				public int compare(Field o1, Field o2) {
					int i = o1.getPosition();
					int j = o2.getPosition();
					if(i<j)
						return -1;
					
					if(i==j)
						return 0;
					
					return 1;
					
				}
				
			});
		}
	}

	public Property getProperty(String propertyName) {
		
		Property property=null;
		for(Property prop: properties){
			if(prop.getName()!=null)
			if(prop.getName().equals(propertyName)){
				property = prop;
				break;
			}
		}
		
		return property;
	}
	
	public String getPropertyValue(String key) {

		Property property = getProperty(key);

		if (property == null)
			return null;

		Value value = property.getValue();
		if (value == null)
			return null;

		return value.getValue() == null ? null : value.getValue().toString();
	}
	
	public Long getLastValueId(){
		if(value!=null){
			return value.getId();
		}
		
		return null;
	}

	public String getDocSpecificName(){
		return (gridName.isEmpty()? "" : gridName+"_")+name+getSuffix(docId);
	}
	
	public static String getSuffix(String documentId){
		return documentId+"D";//Delimited with a D
	}
	
	public String getQualifiedName() {
		if(lineRefId!=null){
			return getDocSpecificName()+getSeparator()+lineRefId;
		}
		return getDocSpecificName();
	}
	
//	public static String getQualifiedName(String documentId, String fieldName){
//		return fieldName+getSuffix(documentId)+getSeparator();
//	}
	
	public Long getLineRefId() {
		return lineRefId;
	}

	public void setLineRefId(Long lineRefId) {
		this.lineRefId = lineRefId;
	}
	
	/**
	 * Aggregatable field
	 * 
	 * @return
	 */
	public boolean isAggregate(){
		return this.parentId!=null;
	}

	public static String getSeparator() {
		
		return "";
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
		
		if(fields!=null){
			for(Field field: fields){
				field.setDocId(docId);
			}
		}
	}

	public String getGridName() {
		return gridName;
	}

	public void setGridName(String gridName) {
		//this.gridName = gridName;
	}

}
