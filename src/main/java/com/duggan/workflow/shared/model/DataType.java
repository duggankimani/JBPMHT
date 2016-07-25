package com.duggan.workflow.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum DataType implements IsSerializable{
	STRING,
	STRINGLONG,
	BOOLEAN,
	INTEGER,
	DOUBLE,
	DATE,
	CHECKBOX,
	MULTIBUTTON,
	SELECTBASIC,
	SELECTMULTIPLE,
	LABEL,
	BUTTON,
	LAYOUTHR,
	GRID,
	COLUMNPROPERTY, 
	FILEUPLOAD, FORM, LINK, IFRAME,
	JS,CHECKBOXGROUP;
	
	public boolean isDropdown(){
		return this.equals(SELECTBASIC);
	}
	//GRID;
	
	public boolean isLookup(){
		return this.equals(SELECTBASIC) || this.equals(SELECTMULTIPLE) || this.equals(BOOLEAN) || this.equals(CHECKBOXGROUP);
	}
	
	public DBType toDBType(){
		DBType type = null;
		switch (this) {
		case CHECKBOXGROUP:
		case STRING:
		case STRINGLONG:
		case SELECTBASIC:
		case LINK:
		case LABEL:
			type = DBType.VARCHAR;
			break;
		case INTEGER:
		case FILEUPLOAD:
			type = DBType.INTEGER;
			break;
		case DOUBLE:
			type = DBType.DECIMAL;
			break;
		case DATE:
			type = DBType.DATE;
			break;
		case CHECKBOX:
			type = DBType.BOOLEAN;
			break;
		default:
			type = DBType.VARCHAR;
			break;
		}
		
		return type;
	}

	public boolean isDate() {
		
		return this.equals(DATE);
	}
}
