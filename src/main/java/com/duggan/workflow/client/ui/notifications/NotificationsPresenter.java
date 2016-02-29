package com.duggan.workflow.client.ui.notifications;

import java.util.List;

import com.duggan.workflow.client.service.ServiceCallback;
import com.duggan.workflow.client.ui.notifications.note.NotePresenter;
import com.duggan.workflow.shared.events.BeforeNotificationsLoadEvent;
import com.duggan.workflow.shared.events.BeforeNotificationsLoadEvent.BeforeNotificationsLoadHandler;
import com.duggan.workflow.shared.events.NotificationsLoadEvent;
import com.duggan.workflow.shared.events.NotificationsLoadEvent.NotificationsLoadHandler;
import com.duggan.workflow.shared.model.Notification;
import com.duggan.workflow.shared.model.NotificationType;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.common.client.IndirectProvider;
import com.gwtplatform.common.client.StandardProvider;
import com.gwtplatform.dispatch.rpc.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class NotificationsPresenter extends
		PresenterWidget<NotificationsPresenter.MyView> implements
		NotificationsLoadHandler, BeforeNotificationsLoadHandler {

	public interface MyView extends View {
	}

	@Inject
	DispatchAsync dispatcher;

	IndirectProvider<NotePresenter> notesFactory;

	public static final Object NOTE_SLOT = new Object();

	@Inject
	public NotificationsPresenter(final EventBus eventBus, final MyView view,
			Provider<NotePresenter> noteProvider) {
		super(eventBus, view);
		notesFactory = new StandardProvider<NotePresenter>(noteProvider);
	}

	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(NotificationsLoadEvent.TYPE, this);
		addRegisteredHandler(BeforeNotificationsLoadEvent.TYPE, this);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
	}

	@Override
	public void onNotificationsLoad(NotificationsLoadEvent event) {
		
		List<Notification> notes = event.getNotifications();
		NotificationsPresenter.this.setInSlot(NOTE_SLOT, null);
		
		if(notes!=null)
		for (final Notification note : notes) {
			if(note.getNotificationType()==NotificationType.FILE_UPLOADED)
				continue;
			
			notesFactory.get(new ServiceCallback<NotePresenter>() {
				@Override
				public void processResult(NotePresenter result) {
					result.setNotification(note,true);
					NotificationsPresenter.this.addToSlot(NOTE_SLOT, result);
				}
			});
		}
	}
	
	@Override
	public void onBeforeNotificationsLoad(BeforeNotificationsLoadEvent event) {
		NotificationsPresenter.this.setInSlot(NOTE_SLOT, null);
	}
}
