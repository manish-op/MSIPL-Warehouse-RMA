package com.serverManagement.server.management.request.region;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateEmployeeRegionRequest {

	private String empEmail;
	private String region;
	public UpdateEmployeeRegionRequest(String empEmail, String region) {
		super();
		this.empEmail = empEmail;
		this.region = region;
	}
	public UpdateEmployeeRegionRequest() {
		super();
	}
	public String getEmpEmail() {
		return empEmail;
	}
	public void setEmpEmail(String empEmail) {
		this.empEmail = empEmail;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	@Override
	public String toString() {
		return "UpdateEmployeeRegionRequest [empEmail=" + empEmail + ", region=" + region + "]";
	}
	
}
