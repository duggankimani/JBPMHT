package com.duggan.workflow.client.ui.admin.outputdocs.save;

import java.util.Arrays;

import com.duggan.workflow.client.model.UploadContext;
import com.duggan.workflow.client.model.UploadContext.UPLOADACTION;
import com.duggan.workflow.client.ui.component.ActionLink;
import com.duggan.workflow.client.ui.component.TextArea;
import com.duggan.workflow.client.ui.component.TextField;
import com.duggan.workflow.client.ui.upload.custom.Uploader;
import com.duggan.workflow.client.util.AppContext;
import com.duggan.workflow.shared.model.OutputDocument;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class SaveOutPutDocsView extends ViewImpl implements
		SaveOutPutDocsPresenter.IOutputDocView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, SaveOutPutDocsView> {
	}

	
	@UiField(provided=true) Uploader uploader;
	@UiField TextField txtName;
	@UiField TextField txtDocRef;
	@UiField TextArea txtDescription;
	@UiField VerticalPanel currentAttachmentsPanel;
	
	@Inject
	public SaveOutPutDocsView(final Binder binder) {
		uploader=new Uploader(true);
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public OutputDocument getOutputDocument(){
		OutputDocument document = new OutputDocument();
		document.setCode(txtDocRef.getValue());
		document.setDescription(txtDescription.getValue());
		document.setName(txtName.getValue());
		
		return document;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clear() {
		txtDocRef.setValue(null);
		txtName.setValue(null);
		txtDescription.setValue(null);
		currentAttachmentsPanel.clear();
		createContext(null);
		
	}

	private void createContext(Long id) {
		uploader.clear();
		UploadContext context = new UploadContext();
		context.setAction(UPLOADACTION.UPLOADOUTPUTDOC);
		context.setContext("code", txtDocRef.getValue());
		context.setContext("description", txtDescription.getValue());
		context.setContext("name", txtName.getValue());
		context.setAccept(Arrays.asList("html","htm"));
		if(id!=null){	
			context.setContext("id", id.toString());
		}
		
		uploader.setContext(context);
	}

	@Override
	public void setOutputDoc(OutputDocument doc) {
		txtName.setValue(doc.getName());
		txtDocRef.setValue(doc.getCode());
		txtDescription.setValue(doc.getDescription());
		
		if(doc.getAttachmentName()!=null){
			ActionLink link = new ActionLink(doc.getAttachmentName());
			link.setTarget("_blank");
			UploadContext context = new UploadContext("getreport");
			context.setContext("attachmentId", doc.getAttachmentId()+"");
			context.setContext("ACTION", "GETATTACHMENT");
			link.setHref(AppContext.getBaseUrl()+context.toUrl());
			
			currentAttachmentsPanel.add(link);
		}
		
		createContext(doc.getId());
	}
	
	@Override
	public Uploader getUploader(){
		return uploader;
	}
	
}
