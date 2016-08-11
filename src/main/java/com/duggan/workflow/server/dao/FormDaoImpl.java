package com.duggan.workflow.server.dao;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.impl.SessionImpl;

import com.duggan.workflow.server.dao.hibernate.JsonType;
import com.duggan.workflow.server.dao.model.ADField;
import com.duggan.workflow.server.dao.model.ADForm;
import com.duggan.workflow.server.dao.model.ADKeyValuePair;
import com.duggan.workflow.server.dao.model.ADProperty;
import com.duggan.workflow.server.dao.model.ADValue;
import com.duggan.workflow.server.dao.model.PO;
import com.duggan.workflow.server.db.DB;
import com.duggan.workflow.shared.model.form.Field;
import com.duggan.workflow.shared.model.form.Form;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONUnmarshaller;

/**
 * 
 * @author duggan
 *
 */
public class FormDaoImpl extends BaseDaoImpl {

	static final Logger logger = Logger.getLogger(FormDaoImpl.class);

	public FormDaoImpl(EntityManager em) {
		super(em);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ADForm> getAllForms(Long processDefId) {

		List lst = em
				.createQuery(
						"from ADForm f where f.processDefId=:processDefId order by f.caption")
				.setParameter("processDefId", processDefId).getResultList();

		return lst;
	}

	public List<ADForm> getAllForms(String processRefId) {

		Long processDefId = DB.getProcessDao().getProcessDefId(processRefId);

		List lst = em
				.createQuery(
						"from ADForm f where "
								+ "(f.processDefId=:processDefId or f.processRefId=:processRefId)"
								+ " order by f.caption")
				.setParameter("processDefId", processDefId)
				.setParameter("processRefId", processRefId).getResultList();

		return lst;
	}

	public ADForm getForm(Long id) {

		return em.find(ADForm.class, id);
	}

	public ADField getField(Long fieldId) {
		return em.find(ADField.class, fieldId);
	}

	public ADValue getValue(Long valueId) {
		return em.find(ADValue.class, valueId);
	}

	public ADProperty getProperty(Long propertyId) {
		return em.find(ADProperty.class, propertyId);
	}

	@Override
	public void save(PO po) {
		super.save(po);

		if (po instanceof ADForm) {
			ADForm form = (ADForm) po;
			if (form.getName() == null || form.getName().isEmpty()
					|| form.getName().equals("Untitled")) {
				form.setName("Untitled" + form.getId());
			}

			if (form.getCaption() == null
					|| form.getCaption().equals("Untitled")) {
				form.setCaption("Untitled" + form.getId());
			}

			Collection<ADProperty> props = getProperties(form);
			if (props != null)
				for (ADProperty prop : props) {
					if (prop.getName().equals("NAME")) {

						if (prop.getValue() == null
								|| (prop.getValue().getStringValue() == null || !prop
										.getValue().getStringValue()
										.equals(form.getName()))) {
							ADValue value = new ADValue();
							value.setStringValue(form.getName());
							prop.setValue(value);
						}
					}

					if (prop.getName().equals("CAPTION")) {

						if (prop.getValue() == null
								|| (prop.getValue().getStringValue() == null || !prop
										.getValue().getStringValue()
										.equals(form.getCaption()))) {

							ADValue value = new ADValue();
							if (form.getCaption() != null) {
								value.setStringValue(form.getCaption());
							} else {
								value.setStringValue(form.getName());
							}

							prop.setValue(value);
						}
					}

					if (prop.getName().equals("DESCRIPTION")) {
						if (prop.getValue() == null) {
							ADValue value = new ADValue();
							value.setStringValue(form.getName());
							prop.setValue(value);
						}
					}
				}
			em.merge(form);
		}
	}

	public void deleteForm(Long formId) {
		delete(getForm(formId));
	}

	public void deleteField(Long fieldId) {
		delete(getField(fieldId));
	}

	public void deleteValue(Long valueid) {
		delete(getValue(valueid));
	}

	public void deleteProperty(Long propertyId) {
		delete(getProperty(propertyId));
	}

	public List<ADField> getFields(Long parentId) {

		@SuppressWarnings("unchecked")
		List<ADField> fields = em
				.createQuery(
						"FROM ADField fld where fld.form.id=:id order by fld.position")
				.setParameter("id", parentId).getResultList();

		return fields;
	}

	@SuppressWarnings("unchecked")
	public void setPosition(ADField fld, int previousPos, int newPos) {

		if (previousPos == -1) {
			previousPos = 1000; // new field - reset all fields with position>
								// newPos
		}
		List<ADField> fields = null;

		String hql = "FROM ADField fld where fld.form.id=:id ";
		boolean reducing = false;

		if (previousPos == newPos) {
			fld.setPosition(newPos);
			return; // no change
		}

		if (previousPos > newPos) {
			hql = hql
					.concat(" and ((fld.position>=:newPos and fld.position<:prevPos) or fld.position is null) ");
		} else {
			reducing = true;
			hql = hql
					.concat(" and ((fld.position<=:newPos and fld.position>:prevPos) or fld.position is null) ");
		}

		hql = hql.concat(" order by position");

		// System.err.println("PrevPos = "+previousPos);
		// System.err.println("NewPos = "+newPos);
		// System.err.println(hql);
		fields = em.createQuery(hql).setParameter("id", fld.getForm().getId())
				.setParameter("newPos", newPos)
				.setParameter("prevPos", previousPos).getResultList();

		int size = fields.size();

		int count = 0;
		for (ADField field : fields) {
			++count;
			// String previousStr = field.getPosition()+"";
			if (reducing) {
				field.setPosition(newPos - (size - count + 1));
			} else {

				field.setPosition(newPos + count);
			}

			// System.err.println(">>Field :: Id = "+field.getId()+"; Previous = "+previousStr+
			// " Pos - "+field.getPosition());
		}

		fld.setPosition(newPos);
	}

	public List<ADKeyValuePair> getKeyValuePairs(String type) {

		@SuppressWarnings("unchecked")
		List<ADKeyValuePair> pairs = em
				.createQuery(
						"FROM ADKeyValuePair p where p.referenceType=:type")
				.setParameter("type", type).getResultList();

		return pairs;
	}

	public Long getFormByName(String formName) {
		String sql = "select id from adform where name=:formName";

		BigInteger val = null;

		try {
			val = (BigInteger) em.createNativeQuery(sql)
					.setParameter("formName", formName).getSingleResult();

			return val.longValue();
		} catch (Exception e) {
		}

		return null;
	}

	public boolean exists(String name) {
		String sql = "select count(id) from ADForm f where f.name=:formName";
		Query query = em.createQuery(sql).setParameter("formName", name);

		Long result = (Long) query.getSingleResult();

		return result > 0;
	}

	public Collection<ADField> getFields(ADForm form) {

		return getResultList(getEntityManager().createQuery(
				"from ADField f where f.form=:form and f.parentField is null")
				.setParameter("form", form));
	}

	public Collection<ADProperty> getProperties(ADForm form) {
		return getResultList(getEntityManager().createQuery(
				"from ADProperty p where p.form=:form").setParameter("form",
				form));
	}

	public Collection<ADProperty> getProperties(ADField adfield) {
		return getResultList(getEntityManager().createQuery(
				"from ADProperty p where p.field=:field").setParameter("field",
				adfield));
	}

	public Collection<ADField> getFields(ADField adfield) {
		return getResultList(getEntityManager().createQuery(
				"from ADField f where f.parentField=:field").setParameter(
				"field", adfield));
	}
	
	public Field findJsonField(String refId){
		String sql = "select field from adfieldjson where field @> '{\"refId\":\":refId\"}'";
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("refId", refId);
		
		Field field = getSingleResultJson(sql, parameters, Field.class);
		return field;
	}

	
	public ArrayList<Field> findJsonFieldsForForm(String formRef) {
		String sql = "select field from adfieldjson where "
				+ "field @> '{\"formRef\":\":formRef\"}' "
				+ "and not field ?? 'parentRef'";//no parent field -ie. load direct children of form
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("formRef", formRef);
		
		ArrayList<Field> fields = (ArrayList<Field>)getResultListJson(sql,parameters,Field.class);
		
		return fields;
	}

	public ArrayList<Field> findJsonFieldsForField(String parentRef) {
		String sql ="select field from adfieldjson where field @> '{\"parentRef\":\":parentRef\"}'";
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("parentRef", parentRef);
		
		ArrayList<Field> fields = (ArrayList<Field>)getResultListJson(sql,parameters,Field.class);
		
		return fields;
	}

	public List<Form> findJsonFormsForProcess(String processRefId) {
		String sql ="select form from adform_json where form @> '{\"processRefId\":\":processRefId\"}'";
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("processRefId", processRefId);
		
		ArrayList<Form> forms = (ArrayList<Form>)getResultListJson(sql,parameters,Form.class);
		
		return forms;
	}
}
