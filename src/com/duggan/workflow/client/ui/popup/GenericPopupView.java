package com.duggan.workflow.client.ui.popup;

import com.duggan.workflow.client.ui.AppManager;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

public class GenericPopupView extends PopupViewImpl implements
		GenericPopupPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, GenericPopupView> {
	}
		
	@UiField PopupPanel popUpPanel;
	@UiField SpanElement spnHeader;
	@UiField HTMLPanel panelBody;
	@UiField HTMLPanel panelButtons;
	@UiField HasClickHandlers aClose;
	
	@Inject
	public GenericPopupView(final EventBus eventBus, final Binder binder) {
		super(eventBus);
		widget = binder.createAndBindUi(this);
		aClose.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		int[] position=AppManager.calculatePosition(10, 50);
		popUpPanel.setPopupPosition(position[1],position[0]);
		
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setHeader(String header) {
		spnHeader.setInnerHTML(header);
	}
	
	
	public PopupPanel getPopUpPanel() {
		return popUpPanel;
	}
	
	
	@Override
	public void setInSlot(Object slot, IsWidget content) {

		if (slot == GenericPopupPresenter.BODY_SLOT) {
			panelBody.clear();
			if (content != null) {
				panelBody.add(content);
			}

		} else if (slot == GenericPopupPresenter.BUTTON_SLOT) {
			panelButtons.clear();
			if (content != null) {
				panelButtons.add(content);
			}
		}
	}
	
	@Override
	public void addToSlot(Object slot, IsWidget content) {	
		if (slot == GenericPopupPresenter.BODY_SLOT) {
			if (content != null) {
				panelBody.add(content);
			}

		} else if (slot == GenericPopupPresenter.BUTTON_SLOT) {
			if (content != null) {
				panelButtons.add(content);
			}
		}
	}
}
