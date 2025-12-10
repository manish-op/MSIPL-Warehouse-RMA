package com.serverManagement.server.management.request.changePassword;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangeEmployeePasswordRequest {

	private String empEmail;
	private String newPassword;
	public ChangeEmployeePasswordRequest(String empEmail, String newPassword) {
		super();
		this.empEmail = empEmail;
		this.newPassword = newPassword;
	}
	public String getEmpEmail() {
		return empEmail;
	}
	public void setEmpEmail(String empEmail) {
		this.empEmail = empEmail;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
}
