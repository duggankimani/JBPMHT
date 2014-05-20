package com.duggan.workflow.client.ui.component;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

public class BulletListPanel extends ComplexPanel {

	public BulletListPanel() {
		   setElement(Document.get().createULElement());
    }
 
    public void setId(String id)
    {
        // Set an attribute common to all tags
        getElement().setId(id);
    }
 
    public void setDir(String dir)
    {
        // Set an attribute specific to this tag
        ((UListElement) getElement().cast()).setDir(dir);
    }
 
    public void add(Widget w)
    {
        // ComplexPanel requires the two-arg add() method
        super.add(w, getElement());
    }
    
    public void insert(Widget child, int beforeIndex){
    	super.insert(child, getElement(), beforeIndex, true);
    }
}
