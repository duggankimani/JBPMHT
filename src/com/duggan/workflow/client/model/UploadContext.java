package com.duggan.workflow.client.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author duggan
 *
 */
public class UploadContext {

	private String url=null;
	private static final String ACTION="ACTION"; 
	
	private Map<String,String> context = new HashMap<String, String>();
	
	public enum UPLOADACTION{
		ATTACHDOCUMENT,
		UPLOADBPMNPROCESS
	}
	
	public UploadContext(){
	}
	
	public UploadContext(String url){
		this.url=url;
	}
	
	public String getUrl(){
		
		if(url==null || url.trim().length()<1 ){
			return null;
		}
		
		if(url.startsWith("/")){
			//ignore forward slash
			return url.substring(1, url.length());
		}
		
		return url.trim();
	}
	
	public void setContext(String key, String value){
		context.put(key, value);
	}
	
	public void setAction(UPLOADACTION action){
		context.put(ACTION, action.name());
	}
	
	public UPLOADACTION getAction(){
		return context.get(ACTION)==null? null : UPLOADACTION.valueOf(context.get(ACTION));
	}
	
	public String toUrl(){
		return url+"?"+getContextValuesAsURLParams();
	}
	/**
	 * This method converts all values in context into
	 * url parameters format i.e key=value&key2=value&key3=value etc
	 *  
	 * @return
	 */
	public String getContextValuesAsURLParams(){
		Set<String> keys = context.keySet();
		StringBuffer params = new StringBuffer();
		for(String key: keys){
			params.append(key+"="+context.get(key));
			params.append("&");
		}
		
		if(params.length()>0){
			params.replace(params.length()-1, params.length(), "");
		}
		return params.toString();
	}	
}
