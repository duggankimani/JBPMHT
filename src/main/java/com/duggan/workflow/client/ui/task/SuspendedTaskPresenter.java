package com.duggan.workflow.client.ui.task;

import com.duggan.workflow.client.model.TaskType;
import com.duggan.workflow.client.place.NameTokens;
import com.duggan.workflow.client.ui.document.GenericDocumentPresenter;
import com.duggan.workflow.client.ui.home.HomePresenter;
import com.duggan.workflow.client.ui.home.HomeTabData;
import com.duggan.workflow.client.ui.login.LoginGateKeeper;
import com.duggan.workflow.client.ui.save.CreateDocPresenter;
import com.duggan.workflow.client.ui.save.form.GenericFormPresenter;
import com.duggan.workflow.client.ui.tasklistitem.DateGroupPresenter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SuspendedTaskPresenter extends AbstractTaskPresenter<SuspendedTaskPresenter.ISuspendedView, SuspendedTaskPresenter.ISuspendedTaskProxy>{


	public interface ISuspendedView extends com.duggan.workflow.client.ui.task.AbstractTaskPresenter.ITaskView{}
	
	@ProxyCodeSplit
	@NameToken({NameTokens.suspended})
	@UseGatekeeper(LoginGateKeeper.class)
	public interface ISuspendedTaskProxy extends TabContentProxyPlace<SuspendedTaskPresenter> {
	}
	
	static int index=4;
	@TabInfo(container = HomePresenter.class)
    static TabData getTabLabel(LoginGateKeeper adminGatekeeper) {
        return new HomeTabData(TaskType.SUSPENDED.name(),"Suspended","",4, adminGatekeeper);
    }
	
	@Inject
	public SuspendedTaskPresenter(EventBus eventBus, ISuspendedView view,
			ISuspendedTaskProxy proxy, Provider<CreateDocPresenter> docProvider,
			Provider<GenericFormPresenter> formProvider,
			Provider<GenericDocumentPresenter> docViewProvider,
			Provider<DateGroupPresenter> dateGroupProvider) {
		super(eventBus, view, proxy, docProvider, formProvider, docViewProvider,
				dateGroupProvider);
		currentTaskType=TaskType.SUSPENDED;
		getView().setTaskType(currentTaskType);
	}
	
}
