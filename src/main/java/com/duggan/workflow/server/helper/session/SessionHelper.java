package com.duggan.workflow.server.helper.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import xtension.workitems.SendMailWorkItemHandler;

import com.duggan.workflow.server.ServerConstants;
import com.duggan.workflow.shared.model.HTUser;

/**
 * A utility class for retrieval and use of 
 * session variables & the HTTPSession
 * 
 * @author duggan
 *
 */
public class SessionHelper{

	static ThreadLocal<HttpServletRequest> request = new ThreadLocal<>();
	private static Logger log = Logger.getLogger(SessionHelper.class);
	
	/**
	 * 
	 * @return User This is the currently logged in user
	 */
	public static HTUser getCurrentUser(){
				
		HttpSession session = request.get()==null? null: request.get().getSession(false);
		if(session==null){
			return new HTUser("testuser", "mdkimani@gmail.com");
			//return null;
		}
		
		if(session.getAttribute(ServerConstants.USER)==null){
			//return new HTUser("calcacuervo");
			return null;
		}
		
		return (HTUser)session.getAttribute(ServerConstants.USER);
	}
	
	public static String getRemoteIP(){
		return getHttpRequest()==null? "Not Defined" :getHttpRequest().getRemoteAddr();
	}
	
	public static String generateDocUrl(String docRefId){
		HttpServletRequest request = getHttpRequest();
		if(request!=null){
			String requestURL = request.getRequestURL().toString();
			String servletPath = request.getServletPath();
			String pathInfo = request.getPathInfo();
			
			log.debug("# RequestURL = "+requestURL);
			log.debug("# ServletPath = "+servletPath);
			log.debug("# Path Info = "+pathInfo);
			if(pathInfo!=null){
				requestURL = requestURL.replace(pathInfo, "");
			}
			log.debug("# Remove Path Info = "+requestURL);				
			requestURL = requestURL.replace(servletPath, "/#search;docRefId="+docRefId);
			log.debug("# Replace ServletPath = "+requestURL);
			
			return requestURL;
		}
		return "#";
	}
	
	/**
	 * This is a utility method to enable retrieval of request from 
	 * any part of the application
	 * 
	 * @param request
	 */
	public static void setHttpRequest(HttpServletRequest httprequest){
		request.set(httprequest);
	}
	
	public static void afterRequest(){
		request.set(null);
	}
	
	public static HttpServletRequest getHttpRequest(){
		return request.get();
	}
}
