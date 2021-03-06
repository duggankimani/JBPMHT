package com.wira.login.client.signin;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.rpc.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.wira.commons.client.security.CurrentUser;
import com.wira.commons.client.service.ServiceCallback;
import com.wira.commons.client.util.Definitions;
import com.wira.commons.shared.models.CurrentUserDto;
import com.wira.login.shared.model.ActionType;
import com.wira.login.shared.request.LoginRequest;
import com.wira.login.shared.response.LoginRequestResult;

public class LoginPresenter extends Presenter<LoginPresenter.ILoginView, LoginPresenter.ILoginProxy>  {
    
	interface ILoginView extends View {
		boolean isValid();
		HasClickHandlers getLoginButton();
		String getUsername();
		String getPassword();
		TextBox getUserNameBox();
		TextBox getPasswordBox();
		void clearViewItems(boolean b);
		void clearErrors();
		void showLoginProgress(boolean show);
		void setLoginButtonEnabled(boolean isEnabled);
		void setError(String error);
	}
	
    @NoGatekeeper
    @NameToken("/signin")
    @ProxyStandard
    interface ILoginProxy extends ProxyPlace<LoginPresenter> {
    }

//	@Inject
//	PlaceManager placeManager;

	@Inject
	DispatchAsync requestHelper;

	private static final Logger LOGGER = Logger.getLogger(LoginPresenter.class
			.getName());

	private final CurrentUser currentUser;

	@Inject
	LoginPresenter(EventBus eventBus, ILoginView view, ILoginProxy proxy,
			final CurrentUser currentUser) {
		super(eventBus, view, proxy, RevealType.Root);
		this.currentUser = currentUser;
	}

	@Override
	protected void onBind() {
		super.onBind();

		getView().getLoginButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (getView().isValid()) {
					callServerLoginAction(new LoginRequest(getView()
							.getUsername(), getView().getPassword()));
				}
			}
		});

		KeyDownHandler keyHandler = new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					if (getView().isValid()) {
						callServerLoginAction(new LoginRequest(getView()
								.getUsername(), getView().getPassword()));
					}
				}
			}
		};

		getView().getUserNameBox().addKeyDownHandler(keyHandler);
		getView().getPasswordBox().addKeyDownHandler(keyHandler);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request); 
		tryLoggingInWithCookieFirst();
	}

	protected void onReset() {
		getView().clearViewItems(true);
	};

	private void callServerLoginAction(final LoginRequest logInAction) {
		getView().clearErrors();
		if (logInAction.getActionType() == ActionType.VIA_CREDENTIALS) {
			getView().showLoginProgress(true);
		}

		requestHelper.execute(logInAction,
				new ServiceCallback<LoginRequestResult>() {
					@Override
					public void processResult(LoginRequestResult result) {
						getView().clearErrors();
						if (result.getCurrentUserDto().isLoggedIn()) {
							setLoggedInCookie(result.getLoggedInCookie());
//							setSystemDetails(result.getVersion(), result.getOrganizationName());
						}

						if (result.getActionType() == ActionType.VIA_COOKIE) {
							onLoginCallSucceededForCookie(result
									.getCurrentUserDto());
						} else {
							onLoginCallSucceeded(result.getCurrentUserDto());
						}
						
						getView().showLoginProgress(false);
						
						if(result.getCurrentUserDto().isLoggedIn()){
							//Update Context
//							AppContext.updateContext(result.getCurrentUserDto().getUser(),
//									result.getVersion(), result.getOrganizationName(),
//									result.getReportView());
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						getView().showLoginProgress(false);
						LOGGER.log(Level.SEVERE,
								"Wrong username or password......");
						getView().setError("Wrong Email or password..");
					}

				});
	}

	protected void setSystemDetails(String organizationName) {
//		AppContext.setOrganizationName(organizationName);
	}

	private void onLoginCallSucceededForCookie(CurrentUserDto currentUserDto) {
		getView().setLoginButtonEnabled(true);
		if (currentUserDto.isLoggedIn()) {
			currentUser.fromCurrentUserDto(currentUserDto);
			redirectToLoggedOnPage();
//			AppContext.fireEvent(new ContextLoadedEvent(currentUser.getUser(), systemVersion));
		}
	}

	private void onLoginCallSucceeded(CurrentUserDto currentUserDto) {
		if (currentUserDto.isLoggedIn()) {
			currentUser.fromCurrentUserDto(currentUserDto);
			redirectToLoggedOnPage();
//			AppContext.fireEvent(new ContextLoadedEvent(currentUser.getUser(), systemVersion));
		} else {
			getView().setError("Wrong username or password");
		}
	}

	private void tryLoggingInWithCookieFirst() {

		String loggedInCookie = Cookies.getCookie(Definitions.AUTHENTICATIONCOOKIE);
		LoginRequest logInAction = new LoginRequest(loggedInCookie);
		callServerLoginAction(logInAction);
	}

//	public void redirectToLoggedOnPage() {
//		String redirect = "";
//		redirect = NameTokens.getOnLoginDefaultPage();
//		String token = placeManager.getCurrentPlaceRequest().getParameter(
//				Definitions.REDIRECT, redirect);
//		PlaceRequest placeRequest = new Builder().nameToken(token).build();
//		placeManager.revealPlace(placeRequest);
//
//	}
	
	public void redirectToLoggedOnPage() {
		String redirect = "index.html";
		Window.Location.replace(redirect);
//		String token = placeManager.getCurrentPlaceRequest().getParameter(
//				Definitions.REDIRECT, redirect);
//		
//		
//		String url = "";
//		if(token.lastIndexOf("/")>0){
//			String[] elems = token.split("/");
//			for(int i=0; i<elems.length; i++){
//				String element = "/"+elems[i];
//				
//				if(i==0){
//					url = placeManager.buildHistoryToken(new Builder().
//							nameToken(element).build());
//				}else{
//					url = url.concat(element);
//				}
//			}
//			
//			url = url.substring(1);
//			GWT.log("History item - "+url);
//			History.replaceItem(url, true);
//		}else{
//			PlaceRequest placeRequest = new Builder().
//					nameToken(token).build();
//			placeManager.revealPlace(placeRequest);
//		}

	}


	public void setLoggedInCookie(String value) {
		String path = "/";
		String domain = getDomain();
		int maxAge = Definitions.REMEMBER_ME_DAYS * 24 * 60 * 60 * 1000;
		Date expires = new Date(new Date().getTime()+maxAge); 
		boolean secure = false;

//		Cookies.setCookie(Definitions.AUTHENTICATIONCOOKIE, value, expires, domain,
//				path, secure);
	}

	public static String getDomain() {
		String domain = GWT.getHostPageBaseURL().replaceAll(".*//", "")
				.replaceAll("/", "").replaceAll(":.*", "");

		return "localhost".equalsIgnoreCase(domain) ? null : domain;
	}   
	

	@Override
	protected void onUnbind() {
		super.onUnbind();
	}
}