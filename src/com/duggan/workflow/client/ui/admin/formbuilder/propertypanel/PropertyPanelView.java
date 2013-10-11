package com.duggan.workflow.client.ui.admin.formbuilder.propertypanel;

import java.util.List;

import com.duggan.workflow.client.ui.admin.formbuilder.component.DateField;
import com.duggan.workflow.client.ui.admin.formbuilder.component.InlineCheckBox;
import com.duggan.workflow.client.ui.admin.formbuilder.component.TextField;
import com.duggan.workflow.shared.model.form.Property;
import com.gwtplatform.mvp.client.PopupViewImpl;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PropertyPanelView extends PopupViewImpl implements
		PropertyPanelPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, PropertyPanelView> {
	}
	
	@UiField PopupPanel popUpContainer;
	@UiField HTMLPanel pBody;
	
	@Inject
	public PropertyPanelView(final EventBus eventBus, final Binder binder) {
		super(eventBus);
		widget = binder.createAndBindUi(this);
		popUpContainer.getElement().getStyle().setDisplay(Display.BLOCK);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void showProperties(List<Property> properties) {
		clear();
		for(Property property: properties){
			switch (property.getType()) {
			case BOOLEAN:
				add(new InlineCheckBox());
				break;
				
			case DATE:
				add(new DateField(property));
				break;

			case DOUBLE:
				add(new TextField(property));
				break;

			case INTEGER:
				add(new TextField(property));
				break;

			case STRING:
				add(new TextField(property));
				break;
			}
		}
	}

//	List<Property> getValues(){
//		pBody.getWidget(0).getClass();
//	}
	
	private void clear() {
		pBody.clear();
	}

	private void add(Widget widget) {
		pBody.add(widget);
	}
}
