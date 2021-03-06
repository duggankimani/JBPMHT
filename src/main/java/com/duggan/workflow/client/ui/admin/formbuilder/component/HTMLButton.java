package com.duggan.workflow.client.ui.admin.formbuilder.component;

import java.util.ArrayList;
import java.util.HashMap;

import com.duggan.workflow.client.ui.AppManager;
import com.duggan.workflow.client.ui.OnOptionSelected;
import com.duggan.workflow.client.ui.component.ActionLink;
import com.duggan.workflow.client.ui.events.ButtonClickEvent;
import com.duggan.workflow.client.util.AppContext;
import com.duggan.workflow.client.util.ENV;
import com.duggan.workflow.shared.model.BooleanValue;
import com.duggan.workflow.shared.model.DataType;
import com.duggan.workflow.shared.model.StringValue;
import com.duggan.workflow.shared.model.Value;
import com.duggan.workflow.shared.model.form.KeyValuePair;
import com.duggan.workflow.shared.model.form.Property;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

public class HTMLButton extends FieldWidget {

	Anchor aButton;
	private Element ankaElement;

	static String ICONSTYLE = "ICONSTYLE";
	static String BUTTONSTYLE = "BUTTONSTYLE";
	static String CONFIRMMESSAGE = "CONFIRMMESSAGE";
	public static String SUBMITTYPE = "SUBMITTYPE";
	static String VALUES = "VALUES";
	static String VALIDATEFORM = "VALIDATEFORM";
	static String CUSTOMHANDLERCLASS = "CUSTOMHANDLERCLASS";

	public HTMLButton(Element ankaElement, boolean designMode) {
		super();
		this.designMode = designMode;
		boolean isHTMLWrappedField = true;
		this.ankaElement = ankaElement;
		aButton = ActionLink.wrap(ankaElement, isHTMLWrappedField);

		addProperty(new Property(BUTTONSTYLE, "Button Style", DataType.STRING));
		addProperty(new Property(ICONSTYLE, "Icon Style", DataType.STRING));
		addProperty(new Property(CONFIRMMESSAGE, "Confirm Message",
				DataType.STRINGLONG));
		addProperty(new Property(SUBMITTYPE, "Type", DataType.SELECTBASIC,
				new KeyValuePair("StartProcess", "Start Process"),
				new KeyValuePair("CompleteProcess", "Complete Process")));

		addProperty(new Property(VALUES, "Values", DataType.STRING));
		addProperty(new Property(CUSTOMTRIGGER, "Trigger", DataType.STRING));
		Property property = new Property(VALIDATEFORM, "Validate Form",
				DataType.CHECKBOX);
		property.setValue(new BooleanValue(null, VALIDATEFORM, true));
		addProperty(property);

		setProperty(NAME, ankaElement.getId());
		field.setName(ankaElement.getId());
		// field Properties update
		field.setProperties(getProperties());

		if (designMode) {
			aButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					showProperties(0);
				}
			});
		} else {
			aButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final String message = getPropertyValue(CONFIRMMESSAGE);
					if (message != null) {
						AppManager.showPopUp("Confirm", message,
								new OnOptionSelected() {

									@Override
									public void onSelect(String key) {
										if (key.equals("Yes")) {
											submit();
										}
									}

								}, "Yes", "Cancel");
					} else {
						submit();
					}

				}

			});
		}

	}

	@Override
	public FieldWidget cloneWidget() {
		return new HTMLButton(ankaElement, designMode);
	}

	@Override
	protected DataType getType() {
		return DataType.BUTTON;
	}

	@Override
	protected void setCaption(String caption) {
	}

	private void submit() {
		String submitType = getPropertyValue(SUBMITTYPE);
		String trigger = getPropertyValue(CUSTOMTRIGGER);
		ButtonClickEvent event = new ButtonClickEvent(submitType, getValues());
		event.setTrigger(trigger);
		AppContext.fireEvent(event);
	}

	private HashMap<String, Value> getValues() {

		HashMap<String, Value> values = new HashMap<String, Value>();

		String vals = getPropertyValue(VALUES);
		if (vals != null) {
			String[] array = vals.split(",");
			for (String v : array) {
				String[] valueSet = v.split("=");
				if (valueSet.length != 2) {
					continue;
				}

				String key = valueSet[0];
				String value = valueSet[1];
				if (key != null && value != null) {
					ArrayList<String> names = new ArrayList<String>();
					getNames(value, names, 0);

					if (names.size() > 0) {
						String buttonQualifiedName = field.getQualifiedName();

						for (String fieldName : names) {
							String qualifiedName = buttonQualifiedName.replace(
									field.getName(), fieldName);
							Object fieldValue = ENV.getValue(qualifiedName);

							if (fieldValue != null) {
								value = value.replaceAll("\\{" + fieldName
										+ "\\}", fieldValue.toString());
							}
						}
					}

					// System.err.println("Added >> "+key+" = "+value);
					values.put(key,
							new StringValue(null, key.trim(), value.trim()));
				}
			}
		}

		return values;
	}

	private static void getNames(String originalStr, ArrayList<String> names,
			int startPos) {
		if (startPos == -1) {
			return;
		}
		if (startPos >= originalStr.length()) {
			return;
		}

		int oIndex = originalStr.indexOf('{', startPos);
		if (oIndex == -1) {
			return;
		}

		int cIdx = originalStr.indexOf("}", oIndex);
		if (cIdx == -1)
			return;
		names.add(originalStr.substring(oIndex + 1, cIdx));

		startPos = cIdx + 1;
		getNames(originalStr, names, startPos);
	}

	@Override
	protected void setHelp(String help) {
		if (help != null)
			aButton.setTitle(help);
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;

		if (!isReadOnly()) {
			aButton.setVisible(true);
		} else {
			aButton.setVisible(!readOnly);
		}
	}

	public boolean isReadOnly() {

		Object readOnly = getValue(READONLY);

		if (readOnly == null)
			return true;

		return (Boolean) readOnly;
	}

	public Widget getComponent() {
		return this;
	}

	@Override
	public Widget getInputComponent() {
		return aButton;
	}

	@Override
	public Element getViewElement() {
		return null;
	}

	@Override
	public void setComponentValid(boolean isValid) {

	}

}
