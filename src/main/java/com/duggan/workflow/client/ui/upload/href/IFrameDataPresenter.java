package com.duggan.workflow.client.ui.upload.href;

import com.duggan.workflow.client.ui.events.CloseAttatchmentEvent;
import com.duggan.workflow.client.ui.events.CloseAttatchmentEvent.CloseAttatchmentHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class IFrameDataPresenter extends
		PresenterWidget<IFrameDataPresenter.IFrameView> implements CloseAttatchmentHandler{

	public interface IFrameView extends PopupView {
		HasClickHandlers getDoneButton();
		void setInfo(String uri, String title);
	}

	@Inject
	public IFrameDataPresenter(final EventBus eventBus, final IFrameView view) {
		super(eventBus, view);
	}

	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(CloseAttatchmentEvent.TYPE, this);
//		getView().getDoneButton().addClickHandler(new ClickHandler() {
//			
//			@Override
//			public void onClick(ClickEvent event) {
//				fireEvent(new ReloadAttachmentsEvent());
//			}
//		});
	}

	@Override
	public void onCloseAttatchment(CloseAttatchmentEvent event) {
		getView().hide();
	}

	public void setInfo(String uri, String title) {
		getView().setInfo(uri, title);
	}
}
