package com.duggan.workflow.shared.requests;

import com.duggan.workflow.shared.responses.BaseResponse;
import com.duggan.workflow.shared.responses.LoginRequestResult;

public class LoginRequest extends BaseRequest<LoginRequestResult> {

	private String username;
	private String password;

	@SuppressWarnings("unused")
	private LoginRequest() {
		// For serialization only
	}

	@Override
	public boolean isSecured() {
		return false;
	}
	
	public LoginRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	@Override
	public BaseResponse createDefaultActionResponse() {

		return new LoginRequestResult();
	}
}
