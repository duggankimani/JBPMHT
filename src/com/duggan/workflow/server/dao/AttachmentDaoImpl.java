package com.duggan.workflow.server.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.duggan.workflow.server.dao.model.LocalAttachment;
import com.duggan.workflow.server.dao.model.ProcessDefModel;

public class AttachmentDaoImpl extends BaseDaoImpl{

	public AttachmentDaoImpl(EntityManager em) {
		super(em);
	}
	
	public LocalAttachment getAttachmentById(long id){
		Object obj = em.createQuery("FROM LocalAttachment d where id= :id").setParameter("id", id).getSingleResult();
		
		LocalAttachment attachment = null;
		
		if(obj!=null){
			attachment = (LocalAttachment)obj;
		}
		
		return attachment;
	}
	
	public List<LocalAttachment> getAttachmentsForDocument(long documentId){
		List lst  = em.createQuery("FROM LocalAttachment l where documentId= :documentId").setParameter("documentId", documentId).getResultList();
		
		return lst;
	}

	public void deactivate(long attachmentId) {
		LocalAttachment attachment = getAttachmentById(attachmentId);
		attachment.setArchived(true);
		em.persist(attachment);
	}	
	
	public void delete(long attachmentId){
		LocalAttachment attachment = getAttachmentById(attachmentId);
		
		delete(attachment);
	}

	public List<LocalAttachment> getAttachmentsForProcessDef(ProcessDefModel model){
		return getAttachmentsForProcessDef(model,false);
	}
	
	public List<LocalAttachment> getAttachmentsForProcessDef(ProcessDefModel model,  boolean isImage) {
		
		return getAttachmentsForProcessDef( model,null, isImage); 
	}

	public List<LocalAttachment> getAttachmentsForProcessDef(ProcessDefModel model,String name, boolean isImage) {

		String sql= "FROM LocalAttachment t where t.processDef=:processDef";
		
		if(isImage){
			sql= "FROM LocalAttachment t where t.processDefImage=:processDef";
		}
		
		if(name!=null && !isImage){
			sql = sql.concat(" and t.name=:attachmentName");
		}
		
		
		Query query = em.createQuery(sql)
			.setParameter("processDef", model);
			
		if(name!=null && !isImage){
			query.setParameter("attachmuentName", name);
		}
			
		@SuppressWarnings("unchecked")
		List<LocalAttachment> attachments  = query.getResultList();
		
		return attachments;
		
	}

	public boolean getHasAttachment(Long documentId) {
		
		Long count = (Long)em.createQuery("Select count(l) FROM LocalAttachment l " +
				"where documentId= :documentId")
		.setParameter("documentId", documentId)
		.getSingleResult();
		
		return count>0;
	}

	public void deleteUserImage(String userId) {
		String sql = "update localattachment set isActive=0 where imageUserId=?";
		
		Query query = em.createNativeQuery(sql).setParameter(1, userId);
		query.executeUpdate();
	}
	
	public LocalAttachment getUserImage(String userId){
		Object obj = null;
		
		try{
			obj = em.createQuery("FROM LocalAttachment d where imageUserId= :userId")
					.setParameter("userId", userId).getSingleResult();
		}catch(Exception e){			
		}
		
		
		LocalAttachment attachment = null;
		
		if(obj!=null){
			attachment = (LocalAttachment)obj;
		}
		
		return attachment;
	}
	
}
