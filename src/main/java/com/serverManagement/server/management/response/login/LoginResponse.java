package com.serverManagement.server.management.response.login;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

	private String authToken;
	private String message;
	private String role;
	private String name ;
	private String email;
	private String mobileNo;
	private String region;
	public LoginResponse(String authToken, String message, String role, String name, String email, String mobileNo,
			String region) {
		super();
		this.authToken = authToken;
		this.message = message;
		this.role = role;
		this.name = name;
		this.email = email;
		this.mobileNo = mobileNo;
		this.region = region;
	}
	public String getAuthToken() {
		return authToken;
	}
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}

}
