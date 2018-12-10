package com.duggan.workflow.client.ui.task;

//import static com.duggan.workflow.client.ui.task.AbstractTaskPresenter.DATEGROUP_SLOT;
import static com.duggan.workflow.client.ui.task.AbstractTaskPresenter.DOCUMENT_SLOT;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.duggan.workflow.client.event.CheckboxSelectionEvent;
import com.duggan.workflow.client.model.TaskType;
import com.duggan.workflow.client.ui.AppManager;
import com.duggan.workflow.client.ui.OnOptionSelected;
import com.duggan.workflow.client.ui.component.ActionLink;
import com.duggan.workflow.client.ui.component.Checkbox;
import com.duggan.workflow.client.ui.events.DocumentSelectionEvent;
import com.duggan.workflow.client.ui.task.DraftsPresenter.IDraftsView;
import com.duggan.workflow.client.ui.task.ParticipatedPresenter.IParticipatedView;
import com.duggan.workflow.client.ui.task.SuspendedTaskPresenter.ISuspendedView;
import com.duggan.workflow.client.ui.util.DateUtils;
import com.duggan.workflow.client.ui.util.DocMode;
import com.duggan.workflow.client.ui.util.StringUtils;
import com.duggan.workflow.client.util.AppContext;
import com.duggan.workflow.shared.event.LoadDataEvent;
import com.duggan.workflow.shared.model.Column;
import com.duggan.workflow.shared.model.Doc;
import com.duggan.workflow.shared.model.DocStatus;
import com.duggan.workflow.shared.model.Document;
import com.duggan.workflow.shared.model.HTStatus;
import com.duggan.workflow.shared.model.HTSummary;
import com.duggan.workflow.shared.model.Priority;
import com.duggan.workflow.shared.model.ProcessDef;
import com.duggan.workflow.shared.model.Schema;
import com.duggan.workflow.shared.model.Value;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class AbstractTaskView extends ViewImpl implements
		AbstractTaskPresenter.ITaskView, IDraftsView, IParticipatedView,
		ISuspendedView {

	private final Widget widget;
	
	public interface Binder extends UiBinder<Widget, AbstractTaskView> {
	}

	@UiField
	AnchorElement aProcess;

	@UiField
	HTMLPanel container;
	@UiField
	Element divDocView;
	@UiField
	HTMLPanel docContainer;
	@UiField
	FlexTable tblTasks;

	@UiField
	Element divTaskListing;

	@UiField
	Element processName;
	
	@UiField
	Element divNoData;

	@UiField
	ScrollPanel divTableListing;

	// Filter Dialog Caret
	boolean isNotDisplayed = true;

	private String processRefId;

	@UiField
	ActionLink aFilter;
	@UiField
	ActionLink aConfigure;
	
	private int currentPage = 1;

	ColumnsPanel columns = new ColumnsPanel();
	Schema defaultSchema = new Schema("_GENERAL", "_GENERAL", "GENERAL");
	
	private ArrayList<Doc> tasks;
	ArrayList<String> defaultCols = new ArrayList<String>();

	private int totalCount;

	private TaskType currentTaskType;
//
//	@UiField
//	HTMLPanel divPaginate;
//	
//	SimplePager usersPager;
	
	enum DefaultFields {
		CaseNo, Process, Task, Submitter, CurrentTask, CurrentUser, Due, Modified, Status, Priority, Notes
	}
	
	@Inject
	public AbstractTaskView(final Binder binder) {
		widget = binder.createAndBindUi(this);
		
		//divPaginate.add(usersPager);
		defaultSchema.addColumn(new Column(DefaultFields.CaseNo.name(),
				"Case No", "Case", "50px"));
		defaultSchema.addColumn(new Column(DefaultFields.Process.name(),
				"Process", "Process"));
		defaultSchema.addColumn(new Column(DefaultFields.Task.name(), "Task",
				"Task"));
		defaultSchema.addColumn(new Column(DefaultFields.Submitter.name(),
				"Submitter", "Submitter"));
//		defaultSchema.addColumn(new Column(DefaultFields.CurrentTask.name(),
//				"Current Task", "Current Task"));
		defaultSchema.addColumn(new Column(DefaultFields.CurrentUser.name(),
				"Current User", "Current User"));
		defaultSchema.addColumn(new Column(DefaultFields.Due.name(), "Due",
				"Due", "80px"));
		defaultSchema.addColumn(new Column(DefaultFields.Modified.name(),
				"Modified", "Modified", "80px"));
		defaultSchema.addColumn(new Column(DefaultFields.Priority.name(),
				"Priority", "Priority", "60px"));
		defaultSchema.addColumn(new Column(DefaultFields.Status.name(),
				"Status", "Status", "60px"));
		defaultSchema.addColumn(new Column(DefaultFields.Notes.name(), "Notes",
				"Notes", "20px"));
		reinitialize();
		
		divDocView.setId("detailed-info");
		divTableListing.getElement().getStyle().setMarginLeft(0, Unit.PX);

		docContainer.addHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// System.err.println("### ABS");
			}
		}, ClickEvent.getType());

		aFilter.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				AppManager.showPopUp("Filter Data", new Filter(),
						new OnOptionSelected() {

							@Override
							public void onSelect(String name) {

							}
						}, "Filter", "Cancel");
			}
		});

		aConfigure.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				AppManager.showPopUp("Configure Columns", columns,
						new OnOptionSelected() {

							@Override
							public void onSelect(String name) {
								bindTasks(tasks, currentPage, totalCount, false);
							}
						}, "Ok", "Cancel");
			}
		});
		
		createHeader(tblTasks);
	}

	private void reinitialize() {
		columns.getValues().clear();
		for (Column col : defaultSchema.getColumns()) {
			columns.add(col);
			defaultCols.add(col.getRefId());
		}
	}
	
	protected void createHeader(FlexTable table) {
		// table.addStyleName("fixed-layout");
		int i = table.getRowCount();
		int j = 0;
		table.setWidget(i, j++, new HTMLPanel("<strong>#</strong>"));
		table.getFlexCellFormatter().setWidth(i, (j - 1), "10px");
		table.getFlexCellFormatter().setStyleName(i, (j - 1), "th");

		HashMap<String, Column> values = columns.getValues();
		if (values.containsKey(DefaultFields.CaseNo.name())) {
			// Case No
			String width = values.get(DefaultFields.CaseNo.name()).getWidth();
			table.setWidget(i, j++,
					createHeader("<strong>Case</strong>", j - 1));
			table.getFlexCellFormatter().setWidth(i, (j - 1), width);
		}

		if (values.containsKey(DefaultFields.Process.name())) {
			String width = values.get(DefaultFields.Process.name()).getWidth();
			// Process Name
			table.setWidget(i, j++,
					createHeader("<strong>Process</strong>", j - 1	));
			if(width!=null){
				table.getFlexCellFormatter().setWidth(i, (j - 1), width);
			}
		}

		if (values.containsKey(DefaultFields.Task.name())) {
			String width = values.get(DefaultFields.Task.name()).getWidth();
			// Task
			table.setWidget(i, j++,
					createHeader("<strong>Task</strong>", j - 1));
			if(width!=null){
				table.getFlexCellFormatter().setWidth(i, (j - 1), width);
			}
		}

		if (values.containsKey(DefaultFields.Submitter.name())) {
			String width = values.get(DefaultFields.Submitter.name()).getWidth();
			// Submitted By
			table.setWidget(i, j++,
					createHeader("<strong>Submitter</strong>", j - 1));
			if(width!=null){
				table.getFlexCellFormatter().setWidth(i, (j - 1), width);
			}
		}

		if (values.containsKey(DefaultFields.CurrentTask.name())) {
			String width = values.get(DefaultFields.CurrentTask.name()).getWidth();
			// Current Task
			table.setWidget(i, j++,
					createHeader("<strong>Current Task</strong>", j - 1));
			if(width!=null){
				table.getFlexCellFormatter().setWidth(i, (j - 1), width);
			}
		}

		if (values.containsKey(DefaultFields.CurrentUser.name())) {
			String width = values.get(DefaultFields.CurrentUser.name()).getWidth();
			// Current Assignee
			table.setWidget(i, j++,
					createHeader("<strong>Current User</strong>", j - 1));
			if(width!=null){
				table.getFlexCellFormatter().setWidth(i, (j - 1), width);
			}
		}

		if (values.containsKey(DefaultFields.Due.name())) {
			String width = values.get(DefaultFields.Due.name()).getWidth();
			// DUE
			table.setWidget(i, j++, createHeader("<strong>Due</strong>", j - 1));
			table.getFlexCellFormatter().setWidth(i, (j - 1), width==null?"80px":width);
		}

		if (values.containsKey(DefaultFields.Modified.name())) {
			String width = values.get(DefaultFields.Modified.name()).getWidth();
			// MODIFIED
			table.setWidget(i, j++,
					createHeader("<strong>Modified</strong>", j - 1));
			table.getFlexCellFormatter().setWidth(i, (j - 1), width==null?"80px":width);
		}
		
		if (values.containsKey(DefaultFields.Priority.name())) {
			String width = values.get(DefaultFields.Priority.name()).getWidth();
			// STATUS
			table.setWidget(i, j++,
					createHeader("<strong>Priority</strong>", j - 1));
			table.getFlexCellFormatter().setWidth(i, (j - 1), width==null?"60px":width);
		}

		if (values.containsKey(DefaultFields.Status.name())) {
			String width = values.get(DefaultFields.Status.name()).getWidth();
			// STATUS
			table.setWidget(i, j++,
					createHeader("<strong>Status</strong>", j - 1));
			table.getFlexCellFormatter().setWidth(i, (j - 1), width==null?"60px":width);
		}

		if (values.containsKey(DefaultFields.Notes.name())) {
			String width = values.get(DefaultFields.Notes.name()).getWidth();
			// NOTES
			table.setWidget(i, j++, new HTMLPanel("<strong>Notes</strong>"));
			table.getFlexCellFormatter().setWidth(i, (j - 1), width==null?"20px":width);
		}

		for (Column column : values.values()) {
			if (defaultCols.contains(column.getRefId())) {
				continue;
			}

			String text = column.getCaption() == null ? column.getName()
					: column.getCaption();
			table.setWidget(i, j++,
					createHeader("<strong>" + text + "<strong", j - 1));
		}

		for (int col = 0; col < table.getCellCount(i); col++) {
			table.getFlexCellFormatter().setStyleName(i, col, "th");
		}
	}

	protected Widget createHeader(String heading, int idx) {
		Element content = DOM.createDiv();
		content.setInnerHTML(heading);
		content.addClassName("sortable-header-content");
		HTMLPanel header = new HTMLPanel("");
		header.getElement().appendChild(content);
		//header.getElement().appendChild(initSort(idx));
		content.setTitle(heading);
		return header;
	}

	private Element initSort(int i) {
		Sorter sorter = new Sorter("task_view_" + i) {
			@Override
			public void sort(String id, String dir) {
				super.sort(id, dir);
				// load data
			}
		};
		return sorter.getElement();
	}

	@Override
	public void addScrollHandler(ScrollHandler scrollHandler) {
		divTableListing.addScrollHandler(scrollHandler);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setInSlot(Object slot, IsWidget content) {
		if (slot == DOCUMENT_SLOT) {
			docContainer.clear();
			if (content != null) {
				docContainer.add(content);
				displayTable(false);
			} else {
				displayTable(true);
			}
		} else {
			super.setInSlot(slot, content);
		}

	}

	protected void displayTable(boolean isDisplayTable) {
		if (isDisplayTable) {
			divDocView.addClassName("hide");
			divTaskListing.removeClassName("hide");
			//divTableListing.removeStyleName("hide");
		} else {
			divDocView.removeClassName("hide");
			divTaskListing.addClassName("hide");
//			divTableListing.addStyleName("hide");
		}
	}
	
	@Override
	public void setTaskType(TaskType currentTaskType) {
		this.currentTaskType = currentTaskType;
	}

	@Override
	public void bindTasks(ArrayList<Doc> tasks, int currentPage, int totalCount, boolean isIncremental) {
		paginator(currentPage, totalCount);
		this.currentPage = currentPage;
		
		this.totalCount = totalCount;
		if (!isIncremental) {
			tblTasks.removeAllRows();
			createHeader(tblTasks);
			this.tasks = tasks;
			
		} else {
			this.tasks.addAll(tasks);
		}

		if(this.tasks.isEmpty()){
			divNoData.setInnerText("No Items To Display");
			divNoData.removeClassName("hide");
		}else if(tasks.isEmpty()){
			divNoData.removeClassName("hide");
			divNoData.setInnerText("Showing 1 - "+this.tasks.size()+" of "+this.tasks.size());
		}else{
			divNoData.setInnerText("Showing 1 - "+this.tasks.size()+" of Many");
		}
		
		int i = tblTasks.getRowCount();

		for (Doc doc : tasks) {
			int j = 0;
			//Window.alert(">>>>> "+doc.getValues());
			
			Date dateToUse = doc.getSortDate();
			InlineLabel spnTime = new InlineLabel();

			String taskActualOwner = doc.getOwner() == null ? "" : doc
					.getOwner().getFullName();

			Element spnSubject = DOM.createSpan();
			spnSubject.setInnerText("Fill in request form");

			Element spnAttach = DOM.createSpan();
			spnAttach.addClassName("icon-paper-clip clip hidden");
			if (doc.hasAttachment()) {
				spnAttach.removeClassName("hidden");
			}

			// spnPriority.setText(summaryTask.getPriority()==null? "":
			// summaryTask.getPriority().toString());
			InlineLabel spnDocIcon = new InlineLabel();
			spnDocIcon.setStyleName("doc-status");

			InlineLabel spnDescription = new InlineLabel();
			spnDescription.addStyleName("spnDescription ellipsis");

			Element spnDeadlines = DOM.createSpan();
			spnDeadlines.addClassName("spnDate");

			Element spnProcessName = DOM.createSpan();
			spnProcessName.setInnerText(doc.getProcessName());

			InlineLabel spnStatus = new InlineLabel();

			boolean isCompleted = true;
			if (doc instanceof Document) {
				isCompleted = !(((Document) doc).getStatus() == DocStatus.DRAFTED);
			}

			spnDeadlines = setDeadlines(DateUtils.addDays(doc.getCreated(), 1),
					isCompleted);

			if (doc instanceof HTSummary) {
				HTSummary summ = (HTSummary) doc;
				HTStatus status = summ.getStatus();
				spnStatus.setText(status.name());
				
				if (summ.getRefId() == null) {
					spnSubject.getStyle().setColor("red");
					spnSubject
							.setTitle("This request was not loaded correctly.");// documentId
																				// is
																				// missing
				}

				dateToUse = summ.getCreated();
				if (status.equals(HTStatus.COMPLETED)) {
					spnDocIcon.addStyleName("icon-ok");
					spnDocIcon.setTitle("Completed Task");
					dateToUse = summ.getCompletedOn();
				} else if (status.equals(HTStatus.SUSPENDED)) {
					spnDocIcon.addStyleName("icon-pause");
					spnDocIcon.setTitle("Task Currently Suspended");
				} else if (status.equals(HTStatus.INPROGRESS)) {
					spnDocIcon.addStyleName("icon-forward");
					spnDocIcon.setTitle("Task Currently in Progress");
				} else {
					spnDocIcon.addStyleName("icon-play");
					spnDocIcon.setTitle("Task Awaiting your action");
				}

				if (!StringUtils.isNullOrEmpty(summ.getName())) {
					spnSubject.setInnerText(summ.getName());
				} else if (!StringUtils.isNullOrEmpty(summ.getNodeName())) {
					spnSubject.setInnerText(summ.getNodeName());
				}

				// setTaskAction(summ.getStatus().getValidActions());

				if (!status.equals(HTStatus.COMPLETED)) {
					if (summ.getEndDateDue() != null) {
						spnDeadlines = setDeadlines(summ.getEndDateDue());
					} else {
						// default 1 day allowance
						spnDeadlines = setDeadlines(DateUtils.addDays(
								summ.getCreated(), 1));
					}
				}
			} else {
				Document document = (Document) doc;
				spnStatus.setText(document.getStatus().name());
				if (document.getStatus() != DocStatus.DRAFTED) {
					spnDocIcon.addStyleName("icon-ok");
					spnDocIcon.setTitle("Completed Task");
				} else {
					spnStatus.setText("DRAFT");
					spnDocIcon.addStyleName("icon-file-alt color-silver-dark");
				}
			}

			// Description
			String desc = doc.getCaseNo();
			if (desc == null) {
				desc = doc.get("subject") == null ? null : doc.get("subject")
						.toString();
			}
			if (desc == null) {
				desc = doc.get("caseNo") == null ? null : doc.get("caseNo")
						.toString();
			}

			if (doc.getProcessStatus() == HTStatus.COMPLETED) {
				taskActualOwner = "Completed";
				spnDescription.getElement().setInnerHTML(
						desc + " - <span style='color:green;'>"
								+ taskActualOwner + "</span>");
			} else {
				// How far in the workflow is my request
				if (doc.getTaskActualOwner() != null) {

					// Delegations are also handled here
					if (doc instanceof HTSummary) {
						HTSummary summ = (HTSummary) doc;
						if (summ.getDelegate() != null
								&& summ.getDelegate().getDelegateTo() != null) {
							taskActualOwner = "Delegated: "
									+ summ.getTaskActualOwner().getFullName();
						} else {
							taskActualOwner = doc.getTaskActualOwner()
									.getFullName();
						}
					} else {
						taskActualOwner = doc.getTaskActualOwner()
								.getFullName();
					}

				} else {
					if (doc.getPotentialOwners() != null)
						taskActualOwner = doc.getPotentialOwners();
				}

				if (doc.getProcessInstanceId() != null
						&& (taskActualOwner == null || taskActualOwner
								.isEmpty())) {
					spnDescription
							.getElement()
							.setInnerHTML(
									desc
											+ " - <span style='color:#D74819;font-size:9pt;'>UnAssigned</span>");
				} else if (doc.getProcessInstanceId() != null) {

					// Span Description
					spnDescription
							.getElement()
							.setInnerHTML(
									desc
											+ " - <span style='color:#2C3539;font-size:9pt;'>"
											+ taskActualOwner + "</span>");
				} else {
					spnDescription
							.getElement()
							.setInnerHTML(
									desc
											+ " - <span style='color:#2C3539;font-size:9pt;'>\'Draft\'</span>");
				}
			}// End of setting descriptions

			Priority priority = Priority.get(doc.getPriority());
			InlineLabel spnPriority = new InlineLabel(priority.getDisplayName());
			spnPriority.addStyleName("priority");
			switch (priority) {
			case CRITICAL:
				spnPriority.addStyleName("label label-important");
				break;

			case HIGH:
				spnPriority.addStyleName("label label-warning"); //
				break;

			default:
				spnPriority.addStyleName("label label-info");
				break;
			}
			
			

			// Several days ago
			if (dateToUse != null) {
				if (CalendarUtil.getDaysBetween(dateToUse, new Date()) >= 1) {
					spnTime.setText(DateUtils.LONGDATEFORMAT.format(dateToUse));
				} else {
					spnTime.setText(DateUtils.LONGDATEFORMAT.format(dateToUse));
				}
			}

			/**
			 * Build Table
			 */
			Checkbox box = new Checkbox(doc);
			box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					Object model = ((Checkbox) (event.getSource())).getModel();
					AppContext.fireEvent(new CheckboxSelectionEvent(model,
							event.getValue()));
				}
			});

			tblTasks.setWidget(i, j++, box);
			ActionLink link = new ActionLink(doc);
			if (doc.getCaseNo() != null)
				link.setText("#" + (doc.getCaseNo().replaceAll("Case-", "")));

			HTMLPanel casePanel = new HTMLPanel("");
			casePanel.add(link);
//			link.addClickHandler(new ClickHandler() {
//
//				@Override
//				public void onClick(ClickEvent event) {
//					Object model = ((ActionLink) event.getSource()).getModel();
//					if (model instanceof Document) {
//						Document doc = (Document) model;
//						AppContext.fireEvent(new DocumentSelectionEvent(doc
//								.getRefId(), null, DocMode.READWRITE));
//					} else {
//						Long taskId = ((HTSummary) model).getId();
//						String docRefId = ((HTSummary) model).getRefId();
//						AppContext.fireEvent(new DocumentSelectionEvent(
//								docRefId, taskId, DocMode.READ));
//					}
//				}
//			});

			HashMap<String, Column> values = columns.getValues();
			if (values.containsKey(DefaultFields.CaseNo.name())) {
				// Case No
				tblTasks.setWidget(i, j++, casePanel);
			}

			if (values.containsKey(DefaultFields.Process.name())) {
				// Process Name
				HTMLPanel subject = new HTMLPanel("");
				subject.getElement().appendChild(spnProcessName);
				tblTasks.setWidget(i, j++, subject);
			}

			if (values.containsKey(DefaultFields.Task.name())) {
				// Task
				HTMLPanel task = new HTMLPanel("");
				task.getElement().appendChild(spnSubject);
				tblTasks.setWidget(i, j++, task);
			}

			if (values.containsKey(DefaultFields.Submitter.name())) {
				// Submitted By
				InlineLabel submitter = new InlineLabel(
						doc.getOwner() == null ? "" : doc.getOwner()
								.getFullName());
				tblTasks.setWidget(i, j++, submitter);
			}

			if (values.containsKey(DefaultFields.CurrentTask.name())) {
				// Current Task
				String currentTask = doc.getCurrentTaskName() == null ? ""
						: doc.getCurrentTaskName();
				tblTasks.setWidget(i, j++, new HTMLPanel(currentTask));
			}

			if (values.containsKey(DefaultFields.CurrentUser.name())) {
				// Current Assignee
				tblTasks.setWidget(i, j++, new HTMLPanel(taskActualOwner));
			}

			if (values.containsKey(DefaultFields.Due.name())) {
				// DUE
				HTMLPanel div = new HTMLPanel("");
				div.getElement().appendChild(spnDeadlines);
				tblTasks.setWidget(i, j++, div);
			}

			if (values.containsKey(DefaultFields.Modified.name())) {
				// MODIFIED
				HTMLPanel div = new HTMLPanel("");
				div.add(spnTime);
				tblTasks.setWidget(i, j++, div);
			}
			
			HTMLPanel divPriority = new HTMLPanel("");
			divPriority.add(spnPriority);
			if (values.containsKey(DefaultFields.Priority.name())) {
				// STATUS
				tblTasks.setWidget(i, j++, divPriority);
			}
			

			HTMLPanel status = new HTMLPanel("");

			if(doc.getProcessInstanceId()!=null){
				// status.add(spnStatus);
				switch (doc.getProcessStatus()) {
				case COMPLETED:
					status.addStyleName("text-success");
					break;
				case INPROGRESS:
					status.addStyleName("text-info");
					break;

				default:
					break;
				}
				status.add(new InlineLabel(doc.getProcessStatus().name()));
			}else{
				status.add(spnStatus);
			}
			

			if (values.containsKey(DefaultFields.Status.name())) {
				// STATUS
				tblTasks.setWidget(i, j++, status);
			}

			if (values.containsKey(DefaultFields.Notes.name())) {
				// NOTES
				tblTasks.setWidget(i, j++, new HTMLPanel(""));
			}

			// Additional Columns
			for (Column column : values.values()) {
				if (defaultCols.contains(column.getRefId())) {
					continue;
				}

				if (column.getName() != null) {
					tblTasks.setWidget(i, j++, render(column, doc));
				}

			}

			tblTasks.getRowFormatter().addStyleName(i, "clickable-row");
			String taskId = (doc instanceof HTSummary)? ((HTSummary)doc).getId()+"": "";
			tblTasks.getRowFormatter().getElement(i).setAttribute("data-href", "/"+doc.getRefId()+"/"+taskId);
			++i;
		}

		initTable(tblTasks.getElement());
	}

	private native void initTable(Element table) /*-{
		var parent = this;
		$wnd.jQuery(document).ready(function($) {
			$wnd.jQuery(".clickable-row").unbind('click');
		    $wnd.jQuery(".clickable-row").click(function() {
		        var tokens = $(this).data("href").split('/');
		        var docRefId = tokens[1];
		        var taskId = "";
		        if(tokens.length>1){
		        	taskId = tokens[2];
		        }
		         //alert("docRefId="+docRefId+"; taskId="+taskId);
		        parent.@com.duggan.workflow.client.ui.task.AbstractTaskView::fireSelectionEvent(Ljava/lang/String;Ljava/lang/String;)(docRefId, taskId);
		    });
		});
	}-*/;
	
	void fireSelectionEvent(final String docRefId, final String taskId){
		if (taskId.isEmpty()) {
			AppContext.fireEvent(new DocumentSelectionEvent(docRefId, null, DocMode.READWRITE));
		} else {
			AppContext.fireEvent(new DocumentSelectionEvent(
					docRefId, Long.parseLong(taskId), DocMode.READ));
		}
	}

	private Widget render(Column column, Doc doc) {
		Label label = new Label();
		String name = column.getName();
		if (doc.getValues() != null) {
			Value val = doc.getValue(name);

			if (val != null && val.getValue() != null) {
				Object v = val.getValue(); 
				if(v instanceof Number){
					label.setWidth("100%");
					label.addStyleName("number");
					
				}
				label.setText(val.getValue() + "");
			}
		}else{
			label.setText("VAL==NULL");
		}
		return label;
	}

	private Element setDeadlines(Date endDateDue) {
		return setDeadlines(endDateDue, false);
	}

	private Element setDeadlines(Date endDateDue, boolean isCompleted) {
		Element spnDeadline = DOM.createSpan();
		spnDeadline.setInnerText(DateUtils.LONGDATEFORMAT.format(endDateDue));

		if (DateUtils.isOverdue(endDateDue)) {

			String timeDiff = DateUtils.getTimeDifference(endDateDue);

			// spnDeadline.setInnerText("Overdue");
			spnDeadline.setTitle("Overdue " + timeDiff + " Ago");
			spnDeadline.removeClassName("hidden");
			if (!isCompleted) {
				spnDeadline.getStyle().setColor("red");
			}
			// spnDeadline.addClassName("label-important");
		} else if (DateUtils.isDueInMins(30, endDateDue)) {

			String timeDiff = DateUtils.getTimeDifference(endDateDue);

			// spnDeadline.setInnerText("Due soon");
			spnDeadline.setTitle("Due in " + timeDiff);
			spnDeadline.removeClassName("hidden");
			if (!isCompleted) {
				spnDeadline.getStyle().setColor("orange");
			}
			// spnDeadline.addClassName("label-warning");
		}

		return spnDeadline;
	}

	@Override
	public void bindAlerts(HashMap<TaskType, Integer> alerts) {
	}

	@Override
	public void bindProcess(ProcessDef processDef) {
		if(processDef!=null){
			processName.setInnerText(processDef.getDisplayName());
		}else{
			processName.setInnerText("All Processes");
		}
	}

	@Override
	public void setProcessRefId(String processRefId) {
		if(this.processRefId==null || processRefId==null){
			reinitialize();
		}else if(!this.processRefId.equals(processRefId)){
			reinitialize();
		}
		
		this.processRefId = processRefId;
		processName.setInnerText("");
		
		if(currentTaskType == TaskType.CASEVIEW) {
			aProcess.setHref("#/registry");
			aFilter.addStyleName("hide");
			aConfigure.addStyleName("hide");
		}else if (processRefId == null) {
			aProcess.setHref("#/home");
			aFilter.addStyleName("hide");
			aConfigure.addStyleName("hide");
		} else {
			aProcess.setHref("#/activities/" + processRefId);
			aFilter.removeStyleName("hide");
			aConfigure.removeStyleName("hide");
		}
	}

	@Override
	public void bindProcessSchema(ArrayList<Schema> schema) {
		schema.add(0, defaultSchema);
		HashMap<String, Column> values = columns.getValues();
		columns = new ColumnsPanel(values, schema);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
	}
	
	public void onPageChange(String selectedPage) {
		AppContext.fireEvent(new LoadDataEvent(Integer.parseInt(selectedPage)));
	}
	
	public native Integer paginator(int currentPage, int recordSize)/*-{
	
		var current_page = currentPage;
		var instance = this;
		var records_per_page = 10;
		
		function prevPage()
		{
		    if (current_page > 1) {
		        current_page--;
		        changePage(current_page);
		        instance.@com.duggan.workflow.client.ui.task.AbstractTaskView::onPageChange(Ljava/lang/String;)(current_page);
		    }
		}
		
		function nextPage()
		{
		    if (current_page < numPages()) {
		        current_page++;
		        changePage(current_page);
		        instance.@com.duggan.workflow.client.ui.task.AbstractTaskView::onPageChange(Ljava/lang/String;)(current_page);
		    }
		    
		}
		
		function changePage(page)
		{
		    var btn_next = $doc.getElementById("btn_next");
		    var btn_prev = $doc.getElementById("btn_prev");
		    
		    
		    var listing_table = $doc.getElementById("liPaginator");
		    var page_span = $doc.getElementById("page");
		
		    // Validate page
		    if (page < 1) page = 1;
		    if (page > numPages()) page = numPages();
		
		    listing_table.innerHTML = "<li class='active'>"+
							"<a><span id='spnCount'>"+recordSize+"</span> records</a>"+
							"</li>"+

							"<li>"+
							"	<a>"+
							"		Page"+
							"		<strong id='page'>"+page+"</strong>"+
							"		of"+
							"		<strong id='totalPages'>"+numPages()+"</strong>"+
							"	</a>"+
							"</li>"+
							"<li>"+
							"	<a id='btn_prev'><i class='icon icon-double-angle-left'></i></a>"+
							"</li>";
		
		    for (var i = 1; i <= numPages(); i++) {
		    	var elPage =  $doc.createElement('li');
		    	if(i==page){
		    		elPage.classList.add('active');
		    		elPage.classList.add('currentpage');
		    	}
		    	
		    	if(i<8){
		    		elPage.innerHTML = '<a>'+ i + '</a>';
		    	}else if(i==8){
		    		elPage.innerHTML = '<a>...</a>';
		    	}
		    	if(i<=8){
		        	listing_table.append(elPage);
		        }
		    }
		    listing_table.innerHTML = listing_table.innerHTML+"<li>"+
				"<a id='btn_next'><i class='icon icon-double-angle-right'></i></a>"+
				"</li>";
				
		    page_span.innerHTML = page;
		
		    if (page == 1) {
		        btn_prev.style.visibility = "hidden";
		    } else {
		        btn_prev.style.visibility = "visible";
		    }
		
		    if (page == numPages()) {
		        btn_next.style.visibility = "hidden";
		    } else {
		        btn_next.style.visibility = "visible";
		    }
		    
		    
		    initEvents();
		}
		
		function initEvents(){
			$wnd.jQuery('#btn_next').click(function(){
				nextPage();
			});
			
			$wnd.jQuery('#btn_prev').click(function(){
				prevPage();
			});
		}
		
		function numPages()
		{
		    return Math.ceil(recordSize / records_per_page);
		}
		
		$wnd.jQuery(document).ready(function($) {
			changePage(currentPage);
		});
		
	}-*/;
}
