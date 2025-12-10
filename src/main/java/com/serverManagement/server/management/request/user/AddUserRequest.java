package com.serverManagement.server.management.request.user;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddUserRequest {

	private String name;
	private String email;
	private String mobileNo;
	private String password;
	private String regionName;
	public AddUserRequest(String name, String email, String mobileNo, String password, String regionName) {
		super();
		this.name = name;
		this.email = email;
		this.mobileNo = mobileNo;
		this.password = password;
		this.regionName = regionName;
	}
	public AddUserRequest() {
		super();
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	@Override
	public String toString() {
		return "AddUserRequest [name=" + name + ", email=" + email + ", mobileNo=" + mobileNo + ", password=" + password
				+ ", regionName=" + regionName + "]";
	}
	
}
