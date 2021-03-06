package com.wira.commons.shared.models;

import static com.wira.commons.shared.models.Module.ACCESSMGT;
import static com.wira.commons.shared.models.Module.CASEREGISTRY;
import static com.wira.commons.shared.models.Module.DASHBOARDS;
import static com.wira.commons.shared.models.Module.DATASOURCES;
import static com.wira.commons.shared.models.Module.DATATABLES;
import static com.wira.commons.shared.models.Module.FILEVIEWER;
import static com.wira.commons.shared.models.Module.MAILLOG;
import static com.wira.commons.shared.models.Module.PROCESSES;
import static com.wira.commons.shared.models.Module.REPORTS;
import static com.wira.commons.shared.models.Module.SETTINGS;
import static com.wira.commons.shared.models.Module.UNASSIGNED;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum PermissionName implements IsSerializable{

	ACCESSMGT_CAN_VIEW_ACCESSMGT(ACCESSMGT,101),
	ACCESSMGT_CAN_EDIT_USER(ACCESSMGT,102),
	ACCESSMGT_CAN_EDIT_ROLE(ACCESSMGT,103),
	ACCESSMGT_CAN_EDIT_UNITS(ACCESSMGT,104),
	
	PROCESSES_CAN_VIEW_PROCESSES(PROCESSES,201),
	PROCESSES_CAN_EDIT_PROCESSES(PROCESSES,202),
	
	UNASSIGNED_CAN_VIEW_UNASSIGNEDTASKS(UNASSIGNED,251),
	UNASSIGNED_CAN_REASSIGN_UNASSIGNEDTASKS(UNASSIGNED,252),
	
	DASHBOARDS_CAN_VIEW_DASHBOARDS(DASHBOARDS,301),
	
	DATATABLES_CAN_VIEW_DATATABLES(DATATABLES,401),
	DATATABLES_CAN_EDIT_DATATABLES(DATATABLES,402),
	
	MAILLOG_CAN_VIEW_MAILLOG(MAILLOG,501),
	MAILLOG_CAN_RESEND_MAILS(MAILLOG,502),
	
	DATASOURCES_CAN_VIEW_DATASOURCES(DATASOURCES,601),
	DATASOURCES_CAN_EDIT_DATASOURCES(DATASOURCES,602),
	
	REPORTS_CAN_VIEW_REPORTS(REPORTS,701),
	REPORTS_CAN_EXPORT_REPORTS(REPORTS,702),
	
	CASEREGISTRY_CAN_VIEW_CASES(CASEREGISTRY,801),
	
	FILEVIEWER_CAN_VIEW_FILES(FILEVIEWER,851),
	FILEVIEWER_CAN_DOWNLOAD_FILES(FILEVIEWER,852),
	
	SETTINGS_CAN_VIEW(SETTINGS,901),
	SETTINGS_CAN_EDIT(SETTINGS,902);
	
	private int code;
	private Module module;

	private PermissionName(Module module,int code) {
		this.module = module;
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
	}

	public final String getName() {
		return name();
	}
}
