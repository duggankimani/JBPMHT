package com.duggan.workflow.server.dao.model;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import com.duggan.workflow.shared.model.form.KeyValuePair;

@XmlSeeAlso({KeyValuePair.class,ADProperty.class,ADField.class})
@XmlRootElement(name="form")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class ADForm extends PO implements HasProperties{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@XmlTransient
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@XmlAttribute
	@Column(length=255,unique=true)
	private String name;
	
	@XmlAttribute
	@Column(length=255)
	private String caption;
	
	@XmlAttribute
	private String processRefId;
	
	@XmlElementWrapper(name="properties")
	@XmlElement(name="property")
	@OneToMany(mappedBy="form", cascade=CascadeType.ALL)
	private Collection<ADProperty> properties = new HashSet<>();
	
	@XmlElementWrapper(name="fields")
	@XmlElement(name="field")
	@OneToMany(mappedBy="form", cascade=CascadeType.ALL)
	private Collection<ADField> fields = new HashSet<>();
	
	@Column(nullable=false)
	private Long processDefId;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public void setProperties(Collection<ADProperty> properties) {
		this.properties = properties;
	}
	
	public void addProperty(ADProperty property){
		properties.add(property);
		property.setForm(this);
	}

	public void removeProperty(ADProperty property){
		properties.remove(property);
	}
	
	public void setFields(Collection<ADField> fields) {
		this.fields = fields;
	}
	
	public void addField(ADField field){
		fields.add(field);
		field.setForm(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		ADForm other = (ADForm)obj;
		
		if(name==null){
			return false;
		}
		
		return name.equals(other.name);
	}
	
	@Override
	public int hashCode() {
		int hashcode=7;
		
		if(name==null){
			hashcode += hashcode*name.hashCode();
			return hashcode;
		}
		
		return super.hashCode();
	}

	public Long getProcessDefId() {
		return processDefId;
	}

	public void setProcessDefId(Long processDefId) {
		this.processDefId = processDefId;
	}

	public String getProcessRefId() {
		return processRefId;
	}

	public void setProcessRefId(String processRefId) {
		this.processRefId = processRefId;
	}
}
