package com.duggan.workflow.client.ui.admin.formbuilder.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class InlineRadio extends FieldWidget {

	private static InlineRadioUiBinder uiBinder = GWT
			.create(InlineRadioUiBinder.class);

	interface InlineRadioUiBinder extends UiBinder<Widget, InlineRadio> {
	}
	
	private final Widget widget;
	
	@UiField Element lblEl;
	
	public InlineRadio() {
		super();
		widget= uiBinder.createAndBindUi(this);
		
		add(widget);
	}

	@Override
	public FieldWidget cloneWidget() {
		return new InlineRadio();
	}
	
	@Override
	protected void setCaption(String caption) {
		lblEl.setInnerHTML(caption);
	}
	
	@Override
	protected void setPlaceHolder(String placeHolder) {
		//txtComponent.setPlaceholder(placeHolder);
	}
	
	@Override
	protected void setHelp(String help) {
		//txtComponent.setTitle(help);
	}

}
