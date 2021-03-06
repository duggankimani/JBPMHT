package com.duggan.workflow.server.helper.jbpm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.duggan.workflow.server.dao.helper.NotificationDaoHelper;
import com.duggan.workflow.server.helper.auth.LoginHelper;
import com.duggan.workflow.shared.model.ApproverAction;
import com.duggan.workflow.shared.model.Document;
import com.duggan.workflow.shared.model.Notification;
import com.duggan.workflow.shared.model.NotificationType;
import com.wira.commons.shared.models.HTUser;

public class CustomNotificationHandler {

	private static Logger logger = Logger
			.getLogger(CustomNotificationHandler.class);

	public void generate(Map<String, Object> params, NotificationType type) {

		String subject = (String) params.get("caseNo");
		String documentId = params.get("documentId") == null ? null : params
				.get("documentId").toString();
		String docRefId = params.get("docRefId") == null ? null : params.get(
				"docRefId").toString();
		String groupId = (String) params.get("GroupId");
		String actorId = (String) params.get("ActorId");

		String ownerId = null;
		if (params.get("ownerId") != null) {
			Object owner = params.get("ownerId");
			if (owner instanceof HTUser) {
				ownerId = ((HTUser) owner).getUserId();
			} else {
				ownerId = owner.toString();
			}
		}

		Object isApproved = params.get("isApproved");
		String _docTypeDesc = null;
		_docTypeDesc = params.get("_docTypeDesc") == null ? null : params.get(
				"_docTypeDesc").toString();

		Document doc = params.get("document") == null ? null
				: (Document) params.get("document");
		if (docRefId == null) {
			try {
				/*
				 * Duggan 12/08/2016 doc =
				 * DocumentDaoHelper.getDocument(Long.parseLong(documentId));
				 */
				docRefId = doc.getRefId();
			} catch (Exception e) {
			}
		}

		if (_docTypeDesc == null) {
			_docTypeDesc = doc.get("_docTypeDesc") == null ? null : doc.get(
					"_docTypeDesc").toString();
		}

		if (subject == null && doc != null) {
			subject = doc.getCaseNo();
		}

		logger.debug("Class : " + this.getClass());
		logger.debug("Subject : " + subject);
		logger.debug("NotificationType : " + type + " | " + _docTypeDesc);
		logger.debug("DocumentId : " + documentId);
		logger.debug("DocRefId : " + docRefId);
		logger.debug("GroupId : " + groupId);
		logger.debug("ActorId : " + actorId);
		logger.debug("OwnerId : " + ownerId);

		Notification notification = new Notification();
		notification.setCreated(new Date());
		if (documentId != null)
			notification.setDocumentId(new Long(documentId));
		
		assert docRefId!=null;
		notification.setDocRefId(docRefId);
		notification.setNotificationType(type);
		notification.setOwner(LoginHelper.get().getUser(ownerId));
		notification.setRead(false);
		notification.setSubject(subject);
		notification.setDocumentType(doc.getType());
		notification.setDocumentTypeDesc(_docTypeDesc);

		List<HTUser> actors = null;
		List<HTUser> potentialActors = null;

		// notification.setTargetUserId(targetUserId);
		if (actorId != null && !actorId.trim().isEmpty()) {
			actors = new ArrayList<>();
			actors.add(LoginHelper.get().getUser(actorId));
		}

		// potential users
		if (groupId != null && !groupId.trim().isEmpty()) {
			potentialActors = LoginHelper.get().getUsersForGroup(groupId);
		}

		List<HTUser> owner = new ArrayList<>();
		// Testing;;
		owner.add(LoginHelper.get().getUser(ownerId));

		ApproverAction action = isApproved == null ? ApproverAction.COMPLETED
				: (Boolean) isApproved ? ApproverAction.APPROVED
						: ApproverAction.REJECTED;

		switch (type) {
		case APPROVALREQUEST_OWNERNOTE:
			/*
			 * Commented By Duggan 3/09/2015, awaiting review of this
			 * functionality
			 * 
			 * generateNotes(owner, notification);
			 */
			break;
		case APPROVALREQUEST_APPROVERNOTE:
			/*
			 * Commented By Duggan 3/09/2015, awaiting review of this
			 * functionality
			 * 
			 * if(actors!=null){ generateNotes(actors, notification); }else{
			 * generateNotes(potentialActors, notification); }
			 */
			break;
		case TASKCOMPLETED_APPROVERNOTE:
			/*
			 * Commented By Duggan 3/09/2015, awaiting review of this
			 * functionality
			 * 
			 * notification.setApproverAction(action);generateNotes(actors,
			 * notification);
			 */
			break;
		case TASKCOMPLETED_OWNERNOTE:
			/*
			 * Commented By Duggan 3/09/2015, awaiting review of this
			 * functionality
			 * 
			 * notification.setApproverAction(action); generateNotes(owner,
			 * notification);
			 */

			break;
		case PROCESS_COMPLETED:

			break;

		case TASK_REMINDER:

			break;
		case COMMENT:
			break;
		default:
			break;
		}

	}

	private void generateNotes(List<HTUser> users, Notification notification) {

		for (HTUser user : users) {
			Notification note = notification.clone();
			note.setTargetUserId(user);
			note.setRead(false);

			if (note.getTargetUserId() == null)
				throw new IllegalArgumentException("Target Id must not be null");
			NotificationDaoHelper.saveNotification(note);
		}

	}

}
