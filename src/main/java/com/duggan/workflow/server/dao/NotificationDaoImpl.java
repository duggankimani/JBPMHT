package com.duggan.workflow.server.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.time.DateUtils;

import com.duggan.workflow.server.dao.model.NotificationModel;
import com.duggan.workflow.server.helper.auth.LoginHelper;
import com.duggan.workflow.server.helper.session.SessionHelper;
import com.duggan.workflow.shared.model.NotificationType;
import com.duggan.workflow.shared.model.UserGroup;

public class NotificationDaoImpl extends BaseDaoImpl{

	public NotificationModel getNotification(Long id) {
		
		List lst = getEntityManager().createQuery("FROM NotificationModel n where id= :id").setParameter("id", id).getResultList();
		
		if(lst.size()>0){
			return (NotificationModel)lst.get(0);
		}
		return null;
	}

	public NotificationModel saveOrUpdate(NotificationModel model) {
		getEntityManager().persist(model);
		return model;
	}

	@SuppressWarnings("unchecked")
	public List<NotificationModel> getAllNotifications(String userId) {

		return getEntityManager().createQuery("FROM NotificationModel n where "
				+ "(n.targetUserId=:userId or (n.createdBy=:userId and n.notificationType=:notificationType)) "
				+ "and n.created>:thirtyDays "
				+ "order by created desc")
				.setParameter("userId", userId)
				.setParameter("notificationType", NotificationType.TASKDELEGATED)
				.setParameter("thirtyDays", DateUtils.addDays(new Date(), -30))
				.getResultList();
	}

	public void markRead(Long id, boolean isRead) {
		NotificationModel model = getNotification(id);
		model.setRead(isRead);		
		saveOrUpdate(model);
	}

	public void delete(Long id) {
		getEntityManager().remove(getNotification(id));
	}

	/**
	 * 
	 * @param userId
	 * @return count
	 */
	public Integer getAlertCount(String userId) {
		
		Long count = (Long)getEntityManager().createQuery("select count(n) from NotificationModel n where " +
				"(n.targetUserId=:userId or (n.createdBy=:userId and n.notificationType=:notificationType))  " +
				"and n.isRead=:isRead and n.created>:thirtyDays")
				.setParameter("userId", userId)
				.setParameter("notificationType", NotificationType.TASKDELEGATED)
				.setParameter("isRead",false)
				.setParameter("thirtyDays", DateUtils.addDays(new Date(), -30))
				.getSingleResult();
		
		return count.intValue();
	}

	/**
	 * To handle repetition of Doc Forwarded Notification 
	 * @param documentId
	 * @param owner
	 */
	public List getNotification(Long documentId, String owner) {

		List models = getEntityManager().createQuery("FROM NotificationModel n where documentId= :documentId " +
				"and notificationType=:notificationType and owner=:owner")
				.setParameter("documentId", documentId)
				.setParameter("notificationType", NotificationType.APPROVALREQUEST_OWNERNOTE)
				.setParameter("owner", owner)
				.getResultList();
		return models;
	}

	@SuppressWarnings("unchecked")
	public List<NotificationModel> getAllNotificationsByDocumentId(Long documentId, NotificationType[] notificationTypes) {

		if(documentId==null){
			return getAllNotificationsByDocumentId(notificationTypes);
		}
		
		List<NotificationType> notes = new ArrayList<>();
		
		if(notificationTypes!=null)
		for(NotificationType type: notificationTypes){
			notes.add(type);
		}
		
		return getEntityManager().createQuery("FROM NotificationModel n " +
				"where n.documentId=:documentId " +
				"and n.notificationType in (:notificationType)" +
				"order by created desc")
		.setParameter("documentId", documentId)
		.setParameter("notificationType", notes)
		.getResultList();
		
	}
	
	@SuppressWarnings("unchecked")
	public List<NotificationModel> getAllNotificationsByDocRefId(String docRefId, NotificationType[] notificationTypes) {

		if(docRefId==null){
			return getAllNotificationsByDocumentId(notificationTypes);
		}
		
		List<NotificationType> notes = new ArrayList<>();
		
		if(notificationTypes!=null)
		for(NotificationType type: notificationTypes){
			notes.add(type);
		}
		
		return getEntityManager().createQuery("FROM NotificationModel n " +
				"where n.docRefId=:docRefId " +
				"and n.notificationType in (:notificationType)" +
				"order by created desc")
		.setParameter("docRefId", docRefId)
		.setParameter("notificationType", notes)
		.getResultList();
		
	}
	
	
	@SuppressWarnings("unchecked")
	public List<NotificationModel> getAllNotificationsByDocumentId(NotificationType[] notificationTypes) {

		List<String> notes = new ArrayList<>();
		
		if(notificationTypes!=null)
		for(NotificationType type: notificationTypes){
			notes.add(type.name());
		}
		
		String userId = SessionHelper.getCurrentUser().getUserId();
		
		List<UserGroup> groups = LoginHelper.getHelper().getGroupsForUser(userId);
		String groupsIds="";
		for(UserGroup group: groups){
			groupsIds = groupsIds.concat(group.getName()+",");
		}
		
		if(groupsIds.isEmpty()){
			return new ArrayList<>();
		}
		groupsIds = groupsIds.substring(0, groupsIds.length()-1);
		
		StringBuffer hql = new StringBuffer("Select n.id FROM localnotification n "+
		//"d.id documentId "+
		"inner join LocalDocument d on (d.id=n.documentId) "+
		"left join Task t on (t.processInstanceId=d.processInstanceId) "+
		"left join OrganizationalEntity owner on (owner.id= t.actualOwner_id and owner.DTYPE='User') "+ 
		"left join PeopleAssignments_PotOwners potowners on (potowners.task_id=t.id)  "+
		"where "+
		"t.archived = 0 and "+ 
		"(d.createdBy=? or " +
		" owner.id = ? or "+
		"( potowners.entity_id = ? or potowners.entity_id in (?) )) and " +
		"n.notificationType in (:noteTypes) and "+
		"n.created>:thirtyDays"
		 );
		
		Query query = getEntityManager().createNativeQuery(hql.toString())
				.setParameter(1, userId)
				.setParameter(2, userId)
				.setParameter(3, userId)
				.setParameter(4, groupsIds)
				.setParameter("noteTypes", notes)
				.setParameter("thirtyDays", DateUtils.addDays(new Date(), -30));
				
		
		List<BigInteger> commentIds = query.getResultList(); 
		
		List<Long> ids = new ArrayList<>();
		for(BigInteger id: commentIds){
			ids.add(id.longValue());
		}

		if(ids==null || ids.isEmpty()){
			return new ArrayList<>();
		}
		
		return getEntityManager().createQuery("FROM NotificationModel n " +
				"where n.id in (:ids) "+		
				"order by created desc")
		.setParameter("ids", ids)
		.getResultList();
		
	}

}
