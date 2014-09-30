package com.duggan.workflow.client.ui.admin.formbuilder.component;

import java.util.ArrayList;
import java.util.List;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.HorizontalPanelDropController;
import com.duggan.workflow.client.ui.component.ActionLink;
import com.duggan.workflow.shared.model.form.Field;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GridDnD extends AbsolutePanel {

	private static GridDnDUiBinder uiBinder = GWT.create(GridDnDUiBinder.class);

	interface GridDnDUiBinder extends UiBinder<Widget, GridDnD> {
	}

	@UiField HorizontalPanel hPanel;
	@UiField HTMLPanel btnGroup;
	private DragHandler handler;
	@UiField ActionLink atxtField;
	@UiField ActionLink aRadioField;
	@UiField ActionLink alblField;
	@UiField ActionLink adateBox;
	@UiField ActionLink achckBox;
	@UiField ActionLink slctField;
	@UiField ActionLink aNumField;
	@UiField ActionLink aCurrField;
	
	private PickupDragController columnDragController;
	private HorizontalPanelDropController columnDropController;
	
	public GridDnD(final List<Field> columns) {
		getElement().getStyle().setOverflow(Overflow.VISIBLE);
		this.addStyleName("grid-layout");
		
		
		add(uiBinder.createAndBindUi(this));
		
		//Temporary Fix for Floating Issue
		//btnGroup.getParent().getElement().getStyle().setWidth(109, Unit.PCT);
		
		handler = new DragHandlerImpl(this){
			@Override
			public void onDragEnd(DragEndEvent event) {
				super.onDragEnd(event);
				Widget parent = event.getContext().draggable.getParent();
				if(!parent.equals(hPanel)){
					//removed
					VerticalPanel panel = (VerticalPanel)event.getContext().draggable;
					GridColumn col = (GridColumn)panel.getWidget(0);
					col.delete();
				}
				
				//System.err.println("GridDnD Dragged............");
				save();
			}
			
		};
		
		columnDragController = new PickupDragController(
				this, false){
			@Override
			protected void restoreSelectedWidgetsLocation() {
				// TODO Auto-generated method stub
				//super.restoreSelectedWidgetsLocation();
			}

		};
		
		columnDragController.addDragHandler(handler);
		columnDragController.setBehaviorMultipleSelection(false);
		columnDragController.setBehaviorDragStartSensitivity(5);

		//hPanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_CONTAINER);
		hPanel.setSpacing(10);

		// initialize our column drop controller
		columnDropController = new HorizontalPanelDropController(
				hPanel);
		columnDragController.registerDropController(columnDropController);

		createColumns(columns);
	}

	/**
	 * Save mechanism
	 */
	private void save(){
		List<Field> lst = getFields();
		save(lst);
	}
	
	public List<Field> getFields() {
		
		List<Field> lst = new ArrayList<Field>();
		int count = hPanel.getWidgetCount();				
		for(int i=0; i<count; i++){
			Widget w = ((VerticalPanel)hPanel.getWidget(i)).getWidget(0);
			
			if(w instanceof GridColumn){
				GridColumn col = (GridColumn)w;
				
				Field fld = col.getField();
				fld.setPosition(i);		
				lst.add(fld);
			}
		}
		
		return lst;
	}

	/**
	 * {@link GridLayout#showDesignGrid}
	 * @param lst
	 */
	protected void save(List<Field> lst) {
		
	}

	private void createColumns(List<Field> columns) {
		hPanel.getElement().getStyle().setWidth(100, Unit.PCT);
		
		for (Field col : columns) {
			// initialize a vertical panel to hold the heading and a second vertical
		      // panel
		      VerticalPanel columnCompositePanel = new VerticalPanel();
		    
		      //columnCompositePanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_COLUMN_COMPOSITE);
		      
		      //initialize inner vertical panel to hold individual widgets
		      VerticalPanel verticalPanel = new VerticalPanel();
		      //verticalPanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_CONTAINER);
		      
		      verticalPanel.addStyleName("inner");
		      
		      hPanel.add(columnCompositePanel);
				      
		      //Put together the column pieces
		      GridColumn heading = new GridColumn(col);
		      
		      columnCompositePanel.add(heading);
		      columnCompositePanel.add(verticalPanel);
		      
		      columnCompositePanel.setCellHorizontalAlignment(verticalPanel,HasHorizontalAlignment.ALIGN_CENTER);

		      // make the column draggable by its heading
		      columnDragController.makeDraggable(columnCompositePanel, heading.getDragComponent());

		      
		      for (int row = 1; row <= ROWS; row++) {
		    		verticalPanel.add(new HTML("Data"));
		        }
		}
	}
	
	private void clearCols(){
		hPanel.clear();
	}
	
	public void repaint(List<Field> fields) {
		clearCols();
		createColumns(fields);
	}
	

	int count = 0;
	
	//private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_CONTAINER = "tbody tr";

	private static final int ROWS = 3;

	//private static final int SPACING = 4;

	
	public ActionLink getAtxtField() {
		return atxtField;
	}

	public ActionLink getaRadioField() {
		return aRadioField;
	}

	public ActionLink getAlblField() {
		return alblField;
	}

	public ActionLink getAdateBox() {
		return adateBox;
	}

	public ActionLink getAchckBox() {
		return achckBox;
	}

	public ActionLink getSlctField() {
		return slctField;
	}

	public ActionLink getNumField() {
		return aNumField;
	}

	public ActionLink getCurrField() {
		return aCurrField;
	}
}
