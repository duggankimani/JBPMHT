package com.duggan.workflow.client.ui.header;

import java.util.Date;
import java.util.HashMap;

import com.duggan.workflow.client.model.TaskType;
import com.duggan.workflow.client.service.TaskServiceCallback;
import com.duggan.workflow.client.ui.events.AdminPageLoadEvent;
import com.duggan.workflow.client.ui.events.AdminPageLoadEvent.AdminPageLoadHandler;
import com.duggan.workflow.client.ui.events.AfterSaveEvent;
import com.duggan.workflow.client.ui.events.AfterSaveEvent.AfterSaveHandler;
import com.duggan.workflow.client.ui.events.AlertLoadEvent;
import com.duggan.workflow.client.ui.events.ContextLoadedEvent;
import com.duggan.workflow.client.ui.events.ContextLoadedEvent.ContextLoadedHandler;
import com.duggan.workflow.client.ui.events.LoadAlertsEvent;
import com.duggan.workflow.client.ui.events.LoadAlertsEvent.LoadAlertsHandler;
import com.duggan.workflow.client.ui.events.NotificationsLoadEvent;
import com.duggan.workflow.client.ui.notifications.NotificationsPresenter;
import com.duggan.workflow.client.util.AppContext;
import com.duggan.workflow.shared.model.HTUser;
import com.duggan.workflow.shared.model.Version;
import com.duggan.workflow.shared.requests.GetAlertCount;
import com.duggan.workflow.shared.requests.GetAlertCountResult;
import com.duggan.workflow.shared.requests.GetNotificationsAction;
import com.duggan.workflow.shared.requests.LogoutAction;
import com.duggan.workflow.shared.requests.MultiRequestAction;
import com.duggan.workflow.shared.responses.GetNotificationsActionResult;
import com.duggan.workflow.shared.responses.LogoutActionResult;
import com.duggan.workflow.shared.responses.MultiRequestActionResult;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.rpc.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class HeaderPresenter extends PresenterWidget<HeaderPresenter.IHeaderView> 
implements AfterSaveHandler, AdminPageLoadHandler, ContextLoadedHandler, LoadAlertsHandler{

	public interface IHeaderView extends View {
		HasClickHandlers getLogout();
		void setValues(String userNames, String userGroups, String orgName);
		Anchor getNotificationsButton();
		void setPopupVisible();
		void setCount(Integer count);
		HasBlurHandlers getpopupContainer();
		void setLoading(boolean b);
		void setAdminPageLookAndFeel(boolean isAdminPage);
		void changeFocus();
		void showAdminLink(boolean admin);
		void setVersionInfo(Date created, String date, String version);
		void setImage(HTUser currentUser);
	}

	@Inject DispatchAsync dispatcher;
	
	@Inject PlaceManager placeManager;
	
	@Inject NotificationsPresenter notifications;
	
	boolean onFocus =true;
	
	
	@ContentSlot
	public static final Type<RevealContentHandler<?>> NOTIFICATIONS_SLOT = new Type<RevealContentHandler<?>>();

	
	@Inject
	public HeaderPresenter(final EventBus eventBus, final IHeaderView view) {
		super(eventBus, view);
		alertTimer.scheduleRepeating(alertReloadInterval);
	}

	
	static int alertReloadInterval = 60 * 1000 * 5; //5 mins
	static long lastLoad=0;
    private Timer alertTimer = new Timer() {

        @Override
        public void run() {
            loadAlertCount();
        }
    };
    
	@Override
	protected void onBind() {
		super.onBind();
		this.addRegisteredHandler(AfterSaveEvent.TYPE, this);
		this.addRegisteredHandler(AdminPageLoadEvent.TYPE, this);
		this.addRegisteredHandler(ContextLoadedEvent.TYPE, this);
		this.addRegisteredHandler(LoadAlertsEvent.TYPE, this);
		getView().getLogout().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				logout();
			}
		});
		
		getView().getNotificationsButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				//Clicks must be atleast 5 min apart
				long currentTime = System.currentTimeMillis();
				if((currentTime-lastLoad)> alertReloadInterval){
					lastLoad=currentTime;
				}else{
					return;
				}
				
				loadAlerts();
			}
		});
	}

	protected void loadAlerts() {
		
		dispatcher.execute(new GetNotificationsAction(AppContext.getUserId()),
				new TaskServiceCallback<GetNotificationsActionResult>() {
				
			@Override
			public void processResult(
					GetNotificationsActionResult notificationsResult) {

				assert notificationsResult!=null;
				fireEvent(new NotificationsLoadEvent(notificationsResult.getNotifications()));
				getView().setLoading(false);
			}
		});
	}

	/**
	 * Called too many times - reloading context/ alert counts from here
	 * slows the application down.
	 * 
	 * TODO: Find Out why
	 */
	@Override
	protected void onReset() {		
		super.onReset();
		setInSlot(NOTIFICATIONS_SLOT, notifications);
//		loadAlertCount();
//		AppContext.reloadContext();
	}
	
	protected void loadAlertCount() {
		alertTimer.cancel();
		getView().setLoading(true);
		MultiRequestAction action = new MultiRequestAction();
		action.addRequest(new GetAlertCount());
		//action.addRequest(new GetNotificationsAction(AppContext.getUserId()));
		
		dispatcher.execute(action, new TaskServiceCallback<MultiRequestActionResult>() {
			@Override
			public void processResult(MultiRequestActionResult results) {
				
				
				GetAlertCountResult result = (GetAlertCountResult)results.get(0);				
				HashMap<TaskType,Integer> alerts = result.getCounts();				
				getView().setCount(alerts.get(TaskType.NOTIFICATIONS));				
				fireEvent(new AlertLoadEvent(alerts));				
				
//				GetNotificationsActionResult notificationsResult = (GetNotificationsActionResult)results.get(1);
//				assert notificationsResult!=null;
//				fireEvent(new NotificationsLoadEvent(notificationsResult.getNotifications()));
//				getView().setLoading(false);
				
				alertTimer.schedule(alertReloadInterval);
			}
		});
		
	}

	protected void logout() {
		dispatcher.execute(new LogoutAction(), new TaskServiceCallback<LogoutActionResult>() {
			@Override
			public void processResult(LogoutActionResult result) {
				AppContext.destroy();
				placeManager.revealErrorPlace("login");
			}
		});
	}

	@Override
	public void onAfterSave(AfterSaveEvent event) {
		loadAlertCount();
	}

	@Override
	public void onAdminPageLoad(AdminPageLoadEvent event) {
		getView().setAdminPageLookAndFeel(event.isAdminPage());
	}

	@Override
	public void onContextLoaded(ContextLoadedEvent event) {
		HTUser currentUser = event.getCurrentUser();
		getView().setImage(currentUser);
		getView().showAdminLink(currentUser.isAdmin());
		getView().setValues(currentUser.getSurname(), currentUser.getGroupsAsString(), event.getOrganizationName());
		
		Version version = event.getVersion();
		getView().setVersionInfo(version.getCreated(), version.getDate(), version.getVersion());
		loadAlertCount();
	}

	@Override
	public void onLoadAlerts(LoadAlertsEvent event) {
		loadAlertCount();
	}
	
	
}
