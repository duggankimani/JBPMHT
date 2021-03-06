package com.duggan.workflow.client.ui.admin.formbuilder.component;

import java.util.Date;

import com.duggan.workflow.client.ui.component.DateInput;
import com.duggan.workflow.client.ui.util.DateUtils;
import com.duggan.workflow.client.util.ENV;
import com.duggan.workflow.shared.model.BooleanValue;
import com.duggan.workflow.shared.model.DataType;
import com.duggan.workflow.shared.model.DateValue;
import com.duggan.workflow.shared.model.Value;
import com.duggan.workflow.shared.model.form.Property;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class HTMLDateField extends FieldWidget {

	@UiField
	Element lblEl;
	@UiField
	DateInput dateBox;

	private Element elementDate;

	public HTMLDateField(Element elementDate, boolean designMode) {
		super();
		this.elementDate = elementDate;
		this.designMode = designMode;
		addProperty(new Property(MANDATORY, "Mandatory", DataType.CHECKBOX, refId));
		addProperty(new Property("DATEFORMAT", "Date Format", DataType.STRING));
		addProperty(new Property(READONLY, "Read Only", DataType.CHECKBOX));
		addProperty(new Property(CUSTOMTRIGGER, "Trigger Class",
				DataType.STRING));

		// Wrap
		elementDate.setAttribute("type", "text");
		dateBox = new DateInput(elementDate);
		assert elementDate.getId() != null;

		// Set
		setProperty(NAME, elementDate.getId());
		field.setName(elementDate.getId());
		lblEl = findLabelFor(elementDate);
		if (lblEl != null) {
			field.setCaption(lblEl.getInnerHTML());
			setProperty(CAPTION, lblEl.getInnerHTML());
		}

		setProperty(HELP, elementDate.getTitle());
		setProperty(MANDATORY,
				new BooleanValue(elementDate.hasAttribute("required")));
		setProperty(READONLY,
				new BooleanValue(elementDate.hasAttribute("readonly")));

		// field Properties update
		field.setProperties(getProperties());

		dateBox.getDateInput().addValueChangeHandler(
				new ValueChangeHandler<Date>() {
					@Override
					public void onValueChange(ValueChangeEvent<Date> event) {
						execTrigger();
					}
				});

		if (designMode) {
			dateBox.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					showProperties(0);
				}
			});
		}
	}

	@Override
	public FieldWidget cloneWidget() {
		return new HTMLDateField(elementDate,designMode);
	}

	@Override
	protected void setCaption(String caption) {
		lblEl.setInnerHTML(caption);
	}

	@Override
	protected void setPlaceHolder(String placeHolder) {
		// txtComponent.setPlaceholder(placeHolder);
	}

	@Override
	protected void setHelp(String help) {
		//DATE BOX IS only a wrapper for a textfield & a span
		//It does not wrap the provided element, meaning any call to dateBox.getElement will return null
		//This method below cannot be called due to the above issue
		//TODO: Redesign date wrapping - Duggan 31/01/2017
		//dateBox.setTitle(help);
	}

	@Override
	protected DataType getType() {
		return DataType.DATE;
	}

	@Override
	public Value getFieldValue() {
		Date dt = dateBox.getDate();

		if (dt == null) {
			return null;
		}

		return new DateValue(field.getLastValueId(), field.getName(), dt);
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);
		if (value != null && value instanceof Date) {
			dateBox.setValue((Date) value);

		} else if (value != null && value instanceof String) {

			try {
				dateBox.setValue(DateUtils.DATEFORMAT.parse(value.toString()));
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void setReadOnly(boolean isReadOnly) {
		this.readOnly = isReadOnly || isComponentReadOnly();
		if(readOnly){
			dateBox.setDisabled(true);
		}else{
			dateBox.setDisabled(false);
		}
	}

	@Override
	public Widget createComponent(boolean small) {

		if (!readOnly)
			if (small) {
				dateBox.setStyle("input-small");
			}

		return null;
	}

	@Override
	protected void onLoad() {
		// TODO Auto-generated method stub
		super.onLoad();

		if (field.getDocId() != null)
			dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
				@Override
				public void onValueChange(ValueChangeEvent<Date> event) {
					ENV.setContext(field, event.getValue());
				}
			});
	}

	public Value from(String key, String val) {
		try {
			return new DateValue(null, key, DateUtils.DATEFORMAT.parse(val));
		} catch (Exception e) {
		}

		return super.from(key, val);
	}

	@Override
	public Widget getInputComponent() {
		return dateBox.getInputComponent();
	}

	@Override
	public Element getViewElement() {
		return null;
	}

	@Override
	public void setComponentValid(boolean isValid) {
	}
}
