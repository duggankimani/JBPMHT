package com.duggan.workflow.client.ui.admin.trigger.taskstep;

import java.util.ArrayList;
import java.util.HashMap;

import com.duggan.workflow.client.service.TaskServiceCallback;
import com.duggan.workflow.client.ui.AppManager;
import com.duggan.workflow.client.ui.OnOptionSelected;
import com.duggan.workflow.client.ui.OptionControl;
import com.duggan.workflow.client.ui.admin.trigger.save.SaveTriggerView;
import com.duggan.workflow.client.ui.component.TextField;
import com.duggan.workflow.client.ui.events.EditTriggerEvent;
import com.duggan.workflow.client.ui.events.EditTriggerEvent.EditTriggerHandler;
import com.duggan.workflow.client.ui.events.LoadTriggersEvent;
import com.duggan.workflow.client.ui.events.LoadTriggersEvent.LoadTriggersHandler;
import com.duggan.workflow.shared.model.TaskStepDTO;
import com.duggan.workflow.shared.model.TaskStepTrigger;
import com.duggan.workflow.shared.model.Trigger;
import com.duggan.workflow.shared.model.TriggerType;
import com.duggan.workflow.shared.requests.GetTaskStepTriggersRequest;
import com.duggan.workflow.shared.requests.GetTriggerCountRequest;
import com.duggan.workflow.shared.requests.MultiRequestAction;
import com.duggan.workflow.shared.requests.SaveTaskStepTriggerRequest;
import com.duggan.workflow.shared.requests.SaveTriggerRequest;
import com.duggan.workflow.shared.responses.GetTaskStepTriggersResponse;
import com.duggan.workflow.shared.responses.GetTriggerCountResponse;
import com.duggan.workflow.shared.responses.MultiRequestActionResult;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.rpc.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class TaskStepTriggerPresenter extends
		PresenterWidget<TaskStepTriggerPresenter.ITaskStepTriggerView> implements LoadTriggersHandler,
		EditTriggerHandler{

	public interface ITaskStepTriggerView extends View{
		void setOpenHandler(OpenHandler<TreeItem> openHandler);
		void addStep(TaskStepDTO step, HashMap<TriggerType, Integer> counts);
		void clearTree();
		void setTriggers(TreeItem item, ArrayList<TaskStepTrigger> triggers);

	}

	@Inject DispatchAsync dispatcher;
	
	@Inject
	public TaskStepTriggerPresenter(final EventBus eventBus, final ITaskStepTriggerView view) {
		super(eventBus, view);
	}

	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(EditTriggerEvent.TYPE, this);
		addRegisteredHandler(LoadTriggersEvent.TYPE, this);
		getView().setOpenHandler(openHandler);
	}
	
	OpenHandler<TreeItem> openHandler = new OpenHandler<TreeItem>() {
		@Override
		public void onOpen(OpenEvent<TreeItem> event) {
			TreeItem item = event.getTarget();
			if(item.getUserObject()!=null){
				loadItems(item);
			}
		}
	};

	public void setTaskSteps(final ArrayList<TaskStepDTO> steps){
		MultiRequestAction action = new MultiRequestAction();
		getView().clearTree();
		
		for(TaskStepDTO step: steps){
			action.addRequest(new GetTriggerCountRequest(step.getId()));
		}
		
		dispatcher.execute(action, new TaskServiceCallback<MultiRequestActionResult>() {
			
			@Override
			public void processResult(MultiRequestActionResult aResponse) {
				int i=0;
				for(TaskStepDTO step: steps){
					GetTriggerCountResponse triggerCount = (GetTriggerCountResponse)aResponse.get(i);
					getView().addStep(step, triggerCount.getCounts());
					i++;
				}
			}
		});
		
	}

	protected void loadItems(final TreeItem item) {
		
		TaskStepDTO dto = (TaskStepDTO)item.getUserObject();
		dispatcher.execute(new GetTaskStepTriggersRequest(dto.getId(), dto.getTriggerType()), 
				new TaskServiceCallback<GetTaskStepTriggersResponse>() {
			
			@Override
			public void processResult(GetTaskStepTriggersResponse aResponse) {
				ArrayList<TaskStepTrigger> triggers = aResponse.getTriggers();
				getView().setTriggers(item, triggers);
			}
		});
	}

	@Override
	public void onLoadTriggers(LoadTriggersEvent event) {
		TreeItem item = event.getItem();
		loadItems(item);
	}
	
	@Override
	public void onEditTrigger(EditTriggerEvent event) {
		if(!isVisible()){
			return;
		}
		final Trigger trigger = event.getTrigger();
		final TaskStepTrigger stepTrigger = event.getStepTrigger();
		final TreeItem item = event.getItem();
		
		if(trigger!=null){
			showEditPopup(item,trigger);
		}
		
		if(stepTrigger!=null){
			showEditStepConditions(item, stepTrigger);
		}
		
	}

	private void showEditStepConditions(final TreeItem item,
			final TaskStepTrigger stepTrigger) {
		final TextField condition = new TextField();
		condition.addStyleName("input-xlarge");
		condition.setValue(stepTrigger.getCondition());
		AppManager.showPopUp("Edit Condition", condition, new OnOptionSelected() {
			
			@Override
			public void onSelect(String name) {
				if(name.equals("Save")){
					stepTrigger.setCondition(condition.getValue());
					save(item, stepTrigger);
				}
			}

		}, "Save", "Cancel");
	}

	protected void save(final TreeItem item, TaskStepTrigger stepTrigger) {
		MultiRequestAction requests = new MultiRequestAction();
		requests.addRequest(new SaveTaskStepTriggerRequest(stepTrigger));
		dispatcher.execute(requests, new TaskServiceCallback<MultiRequestActionResult>() {
			@Override
			public void processResult(MultiRequestActionResult aResult) {
				if(item!=null){
					loadItems(item);
				}
			}
		});
	}

	private void showEditPopup(final TreeItem item,Trigger trigger) {
		final SaveTriggerView view = new SaveTriggerView(trigger);
		AppManager.showPopUp("Edit Trigger",view,new OptionControl(){
			@Override
			public void onSelect(String name) {						
				if(name.equals("Save") && view.isValid()){
					Trigger trigger = view.getTrigger();
					save(item, trigger);
					hide();
				}
				
				if(name.equals("Cancel")){
					hide();
				}
			}

		},"Save", "Cancel");
	}
	
	private void save(final TreeItem item, Trigger trigger) {
		MultiRequestAction requests = new MultiRequestAction();
		requests.addRequest(new SaveTriggerRequest(trigger));
		dispatcher.execute(requests, new TaskServiceCallback<MultiRequestActionResult>() {
			@Override
			public void processResult(MultiRequestActionResult aResult) {
				if(item!=null){
					loadItems(item);
				}
			}
		});
	}

}
