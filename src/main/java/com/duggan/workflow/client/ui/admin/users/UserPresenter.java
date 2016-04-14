package com.duggan.workflow.client.ui.admin.users;

import java.util.List;

import com.duggan.workflow.client.event.CheckboxSelectionEvent;
import com.duggan.workflow.client.event.CheckboxSelectionEvent.CheckboxSelectionHandler;
import com.duggan.workflow.client.place.NameTokens;
import com.duggan.workflow.client.service.ServiceCallback;
import com.duggan.workflow.client.service.TaskServiceCallback;
import com.duggan.workflow.client.ui.AppManager;
import com.duggan.workflow.client.ui.OnOptionSelected;
import com.duggan.workflow.client.ui.OptionControl;
import com.duggan.workflow.client.ui.admin.AdminHomePresenter;
import com.duggan.workflow.client.ui.admin.TabDataExt;
import com.duggan.workflow.client.ui.admin.users.groups.GroupPresenter;
import com.duggan.workflow.client.ui.admin.users.item.UserItemPresenter;
import com.duggan.workflow.client.ui.admin.users.save.UserSavePresenter;
import com.duggan.workflow.client.ui.admin.users.save.UserSavePresenter.TYPE;
import com.duggan.workflow.client.ui.events.EditGroupEvent;
import com.duggan.workflow.client.ui.events.EditGroupEvent.EditGroupHandler;
import com.duggan.workflow.client.ui.events.EditUserEvent;
import com.duggan.workflow.client.ui.events.EditUserEvent.EditUserHandler;
import com.duggan.workflow.client.ui.events.LoadGroupsEvent;
import com.duggan.workflow.client.ui.events.LoadGroupsEvent.LoadGroupsHandler;
import com.duggan.workflow.client.ui.events.LoadUsersEvent;
import com.duggan.workflow.client.ui.events.LoadUsersEvent.LoadUsersHandler;
import com.duggan.workflow.client.ui.events.ProcessingCompletedEvent;
import com.duggan.workflow.client.ui.events.ProcessingEvent;
import com.duggan.workflow.client.ui.security.AdminGateKeeper;
import com.duggan.workflow.shared.model.HTUser;
import com.duggan.workflow.shared.model.Org;
import com.duggan.workflow.shared.model.UserGroup;
import com.duggan.workflow.shared.requests.GetGroupsRequest;
import com.duggan.workflow.shared.requests.GetOrgsRequest;
import com.duggan.workflow.shared.requests.GetUsersRequest;
import com.duggan.workflow.shared.requests.MultiRequestAction;
import com.duggan.workflow.shared.requests.SaveGroupRequest;
import com.duggan.workflow.shared.requests.SaveOrgRequest;
import com.duggan.workflow.shared.requests.SaveUserRequest;
import com.duggan.workflow.shared.responses.GetGroupsResponse;
import com.duggan.workflow.shared.responses.GetOrgsResponse;
import com.duggan.workflow.shared.responses.GetUsersResponse;
import com.duggan.workflow.shared.responses.MultiRequestActionResult;
import com.duggan.workflow.shared.responses.SaveGroupResponse;
import com.duggan.workflow.shared.responses.SaveOrgResponse;
import com.duggan.workflow.shared.responses.SaveUserResponse;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.common.client.IndirectProvider;
import com.gwtplatform.common.client.StandardProvider;
import com.gwtplatform.dispatch.rpc.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class UserPresenter extends
		Presenter<UserPresenter.MyView, UserPresenter.MyProxy> implements
		EditUserHandler, LoadUsersHandler, LoadGroupsHandler, EditGroupHandler,
		CheckboxSelectionHandler {

	public interface MyView extends View {

		HasClickHandlers getaNewUser();

		HasClickHandlers getaNewGroup();

		void setType(TYPE type);

		void bindUsers(List<HTUser> users);

		void bindGroups(List<UserGroup> groups);

		void setUserEdit(boolean value);

		void setGroupEdit(boolean value);

		HasClickHandlers getEditUser();

		HasClickHandlers getDeleteUser();

		HasClickHandlers getEditGroup();

		HasClickHandlers getDeleteGroup();

		HasClickHandlers getNewOrg();

		HasClickHandlers getEditOrg();

		HasClickHandlers getDeleteOrg();

		void bindOrgs(List<Org> orgs);

		void setOrgEdit(boolean value);
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.usermgt)
	@UseGatekeeper(AdminGateKeeper.class)
	public interface MyProxy extends TabContentProxyPlace<UserPresenter> {
	}

	@TabInfo(container = AdminHomePresenter.class)
	static TabData getTabLabel(AdminGateKeeper adminGatekeeper) {
		return new TabDataExt("Users and Groups", "icon-group", 3,
				adminGatekeeper);
	}

	public static final Object ITEMSLOT = new Object();
	public static final Object GROUPSLOT = new Object();

	IndirectProvider<UserSavePresenter> userFactory;
	IndirectProvider<UserItemPresenter> userItemFactory;
	IndirectProvider<GroupPresenter> groupFactory;

	TYPE type = TYPE.USER;

	private Object selectedModel;

	@Inject
	DispatchAsync requestHelper;

	@Inject
	public UserPresenter(final EventBus eventBus, final MyView view,
			MyProxy proxy, Provider<UserSavePresenter> addUserProvider,
			Provider<UserItemPresenter> itemProvider,
			Provider<GroupPresenter> groupProvider) {
		super(eventBus, view, proxy, AdminHomePresenter.SLOT_SetTabContent);
		userFactory = new StandardProvider<UserSavePresenter>(addUserProvider);
		userItemFactory = new StandardProvider<UserItemPresenter>(itemProvider);
		groupFactory = new StandardProvider<GroupPresenter>(groupProvider);
	}

	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(EditUserEvent.TYPE, this);
		addRegisteredHandler(LoadUsersEvent.TYPE, this);
		addRegisteredHandler(LoadGroupsEvent.TYPE, this);
		addRegisteredHandler(EditGroupEvent.TYPE, this);
		addRegisteredHandler(CheckboxSelectionEvent.getType(), this);

		getView().getaNewUser().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPopup(TYPE.USER);
			}
		});

		getView().getaNewGroup().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPopup(UserSavePresenter.TYPE.GROUP);
			}
		});

		getView().getEditUser().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPopup(UserSavePresenter.TYPE.USER, selectedModel);
			}
		});

		getView().getDeleteUser().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final HTUser user = (HTUser) selectedModel;
				AppManager.showPopUp(
						"Confirm Delete",
						new HTMLPanel("Do you want to delete user \""
								+ user.getName() + "\""),
						new OnOptionSelected() {

							@Override
							public void onSelect(String name) {
								if (name.equals("Delete")) {
									delete(user);

								}
							}

						}, "Cancel", "Delete");

			}
		});

		getView().getEditGroup().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPopup(UserSavePresenter.TYPE.GROUP, selectedModel);
			}
		});

		getView().getDeleteGroup().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final UserGroup group = (UserGroup) selectedModel;
				AppManager.showPopUp("Confirm Delete", new HTMLPanel(
						"Do you want to delete group \"" + group.getName()
								+ "\""), new OnOptionSelected() {

					@Override
					public void onSelect(String name) {
						if (name.equals("Delete")) {
							delete(group);
						}
					}

				}, "Cancel", "Delete");
			}
		});

		getView().getNewOrg().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPopup(UserSavePresenter.TYPE.ORG, null);
			}
		});

		getView().getEditOrg().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPopup(UserSavePresenter.TYPE.ORG, selectedModel);
			}
		});

		getView().getDeleteOrg().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final Org org = (Org) selectedModel;
				AppManager.showPopUp("Confirm Delete", new HTMLPanel(
						"Do you want to delete org \"" + org.getName()
								+ "\""), new OnOptionSelected() {

					@Override
					public void onSelect(String name) {
						if (name.equals("Delete")) {
							delete(org);
						}
					}

				}, "Cancel", "Delete");

			}
		});

	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);

		String page = request.getParameter("p", "USER").toUpperCase();
		try {
			type = TYPE.valueOf(page);
		} catch (Exception e) {
		}

		if (type == null) {
			type = TYPE.USER;
		}

		setType(type);
		loadData();
	}

	private void showPopup(final UserSavePresenter.TYPE type) {
		showPopup(type, null);
	}

	private void showPopup(final UserSavePresenter.TYPE type, final Object obj) {
		userFactory.get(new ServiceCallback<UserSavePresenter>() {
			@Override
			public void processResult(final UserSavePresenter result) {
				result.init(type, obj);
				
				AppManager.showPopUp("Edit " + type.displayName(), result
						.getView().asWidget(), new OptionControl() {

					@Override
					public void onSelect(String name) {
						if (name.equals("Save")) {
							if (result.getView().isValid()) {
								if (type == TYPE.GROUP) {
									UserGroup userGroup = result.getView()
											.getGroup();
									saveGroup(userGroup);
								} else if (type == TYPE.USER) {
									HTUser user = result.getView().getUser();
									saveUser(user);
								}else if(type == TYPE.ORG){
									Org org = result.getView().getOrg();
									saveOrg(org);
								}
								hide();
							}
						} else {
							hide();
						}
					}

				}, "Save", "Cancel");
			}
		});

	}
	

	private void saveOrg(Org org) {
		MultiRequestAction action = new MultiRequestAction();
		action.addRequest(new SaveOrgRequest(org));
		fireEvent(new ProcessingEvent());
		requestHelper.execute(action,
				new TaskServiceCallback<MultiRequestActionResult>() {
					@Override
					public void processResult(MultiRequestActionResult aResponse) {
						loadOrgs();
						fireEvent(new ProcessingCompletedEvent());
					}
				});
	}

	private void saveUser(HTUser user) {
		SaveUserRequest request = new SaveUserRequest(user);
		requestHelper.execute(request,
				new TaskServiceCallback<SaveUserResponse>() {
					@Override
					public void processResult(SaveUserResponse result) {
						fireEvent(new LoadUsersEvent());
					}
				});
	}

	private void saveGroup(UserGroup userGroup) {
		SaveGroupRequest request = new SaveGroupRequest(userGroup);
		requestHelper.execute(request,
				new TaskServiceCallback<SaveGroupResponse>() {
					@Override
					public void processResult(SaveGroupResponse result) {
						fireEvent(new LoadGroupsEvent());
					}
				});

	}

	void loadData() {
		if (type == TYPE.USER) {
			loadUsers();
		} else if (type == TYPE.GROUP) {
			loadGroups();
		} else {
			loadOrgs();
		}
	}

	private void loadOrgs() {
		loadOrgs(null, 0, 100);
	}

	private void loadOrgs(String searchTerm, int start, int length) {
		fireEvent(new ProcessingEvent());
		requestHelper.execute(new GetOrgsRequest(searchTerm, start, length),
				new TaskServiceCallback<GetOrgsResponse>() {
					@Override
					public void processResult(GetOrgsResponse aResponse) {
						List<Org> orgs = aResponse.getOrgs();
						getView().bindOrgs(orgs);
						fireEvent(new ProcessingCompletedEvent());
						fireEvent(new CheckboxSelectionEvent(selectedModel, true));
					}
				});
	}

	private void loadGroups() {
		GetGroupsRequest request = new GetGroupsRequest();
		fireEvent(new ProcessingEvent());
		requestHelper.execute(request,
				new TaskServiceCallback<GetGroupsResponse>() {
					@Override
					public void processResult(GetGroupsResponse result) {
						List<UserGroup> groups = result.getGroups();
						getView().bindGroups(groups);
						// loadGroups(groups);
						fireEvent(new ProcessingCompletedEvent());
						fireEvent(new CheckboxSelectionEvent(selectedModel, true));
					}
				});
	}

	private void loadUsers() {
		GetUsersRequest request = new GetUsersRequest();
		fireEvent(new ProcessingEvent());
		requestHelper.execute(request,
				new TaskServiceCallback<GetUsersResponse>() {
					@Override
					public void processResult(GetUsersResponse result) {
						List<HTUser> users = result.getUsers();
						getView().bindUsers(users);
						// loadUsers(users);
						fireEvent(new ProcessingCompletedEvent());
						fireEvent(new CheckboxSelectionEvent(selectedModel, true));
					}
				});
	}

	@Override
	public void onEditUser(EditUserEvent event) {
		showPopup(TYPE.USER, event.getUser());
	}

	@Override
	public void onLoadUsers(LoadUsersEvent event) {
		loadData();
	}

	@Override
	public void onLoadGroups(LoadGroupsEvent event) {
		loadData();
	}

	public void setType(TYPE type) {
		this.type = type;
		getView().setType(type);
	}

	@Override
	public void onEditGroup(EditGroupEvent event) {
		showPopup(type, event.getGroup());
	}

	@Override
	public void onCheckboxSelection(CheckboxSelectionEvent event) {

		selectedModel = event.getModel();
		selectItem(selectedModel, event.getValue());
		
		if(!event.getValue()){
			selectedModel = null;
		}
	}

	private void selectItem(Object model, boolean value) {

		if (model instanceof HTUser) {
			getView().setUserEdit(value);
		} else if (model instanceof UserGroup) {
			getView().setGroupEdit(value);
		}else if(model instanceof Org){
			getView().setOrgEdit(value);
		}

	}

	private void delete(HTUser user) {

		SaveUserRequest request = new SaveUserRequest(user);
		request.setDelete(true);
		requestHelper.execute(request,
				new TaskServiceCallback<SaveUserResponse>() {
					@Override
					public void processResult(SaveUserResponse result) {
						loadUsers();
					}
				});
	}

	protected void delete(UserGroup group) {
		SaveGroupRequest request = new SaveGroupRequest(group);
		request.setDelete(true);
		requestHelper.execute(request,
				new TaskServiceCallback<SaveGroupResponse>() {
					@Override
					public void processResult(SaveGroupResponse result) {
						loadGroups();
					}
				});
	}
	
	protected void delete(Org org){
		SaveOrgRequest request = new SaveOrgRequest(org);
		request.setDelete(true);
		requestHelper.execute(request,
				new TaskServiceCallback<SaveOrgResponse>() {
					@Override
					public void processResult(SaveOrgResponse result) {
						loadOrgs();
					}
				});
	}
}
