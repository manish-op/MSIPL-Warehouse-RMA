package com.serverManagement.server.management.request.option;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateAvailableStatusRequest {

	private String existingOption;
	private String newOption;
	public UpdateAvailableStatusRequest(String existingOption, String newOption) {
		super();
		this.existingOption = existingOption;
		this.newOption = newOption;
	}
	
	public UpdateAvailableStatusRequest() {
		super();
	}

	public String getExistingOption() {
		return existingOption;
	}
	public void setExistingOption(String existingOption) {
		this.existingOption = existingOption;
	}
	public String getNewOption() {
		return newOption;
	}
	public void setNewOption(String newOption) {
		this.newOption = newOption;
	}
	
	
}
