package com.duggan.workflow.client.ui.component;

import java.util.List;

import com.duggan.workflow.shared.model.Listable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class DropDownList<T extends Listable> extends Composite implements HasValueChangeHandlers<T>{

	private static DropDownListUiBinder uiBinder = GWT
			.create(DropDownListUiBinder.class);

	interface DropDownListUiBinder extends UiBinder<Widget, DropDownList> {
	}

	@UiField ListBox listBox;
	
	private List<T> items;
	private String nullText = "--Select--";
	
	T value = null;
	
	public DropDownList() {
		initWidget(uiBinder.createAndBindUi(this));
		initComponents();
	}
	
	
	
	void initComponents(){
		listBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				int selectedIndex = listBox.getSelectedIndex();
				
				String code = listBox.getValue(selectedIndex);
				value = null;
				if(code.isEmpty()){
					//setting null
					value=null;
				}else{
					value =  items.get(selectedIndex-1);
				}
				
				ValueChangeEvent.fire(DropDownList.this,value);
			}
		});
	}
	
	public void setItems(List<T> items){
		this.items = items;
		
		listBox.clear();
		listBox.addItem(nullText, "");
		
		for(T item: items){
			listBox.addItem(item.getDisplayName(), item.getName());
		}
	}
	
	public T getValue(){
		return value;
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<T> handler) {

		return this.addHandler(handler, ValueChangeEvent.getType());
	}

	public void setValue(T value) {
		
		for(int i=0; i<listBox.getItemCount(); i++){
			
			if(listBox.getValue(i).equals(value.getName())){
				listBox.setSelectedIndex(i);
				this.value = value;
			}
			
		}
		
	}
	
	public void setValueByKey(String key){
		for(int i=0; i<listBox.getItemCount(); i++){
			
			if(listBox.getValue(i).equals(key)){
				listBox.setSelectedIndex(i);
				value =  items.get(i-1);
			}
			
		}
	}

	@Override
	public void setTitle(String title) {
		listBox.setTitle(title);
	}

	public void setReadOnly(boolean readOnly) {
		if(readOnly){
			listBox.setEnabled(false);
		}
	}
	
	public List<T> values(){
		return items;
	}
	
	@Override
	public void addStyleName(String style) {
		listBox.addStyleName(style);
	}
	
	@Override
	public void removeStyleName(String style) {
		listBox.removeStyleName(style);
	}

	public String getNullText() {
		return nullText;
	}

	@Override
	public void setStyleName(String style) {
		listBox.setStyleName(style);
	}

	public void setNullText(String nullText) {
		this.nullText = nullText;
	}
}
