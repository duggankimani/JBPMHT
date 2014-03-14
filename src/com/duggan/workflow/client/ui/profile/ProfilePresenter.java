package com.duggan.workflow.client.ui.profile;

import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.duggan.workflow.client.place.NameTokens;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.google.inject.Inject;
import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.duggan.workflow.client.ui.home.HomePresenter;

public class ProfilePresenter extends
		Presenter<ProfilePresenter.MyView, ProfilePresenter.MyProxy> {

	public interface MyView extends View {
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.profile)
	public interface MyProxy extends ProxyPlace<ProfilePresenter> {
	}

	@Inject
	public ProfilePresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy) {
		super(eventBus, view, proxy);
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, HomePresenter.ACTIVITIES_SLOT, this);
	}

	@Override
	protected void onBind() {
		super.onBind();
	}
}
