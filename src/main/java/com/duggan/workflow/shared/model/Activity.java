package com.duggan.workflow.shared.model;

import java.io.Serializable;
import java.util.Date;

public abstract class Activity implements Serializable, Comparable<Activity> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public abstract HTUser getCreatedBy();
	public abstract Date getCreated();
	public abstract HTUser getTargetUserId();
	public abstract String getStatement();
	
	@Override
	public int compareTo(Activity o) {
		return this.getCreated().compareTo(o.getCreated());
	}
	
}
