package com.serverManagement.server.management.request.option;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateItemStatusOtionRequest {

	private String oldStatus;
	private String newStatus;
	public UpdateItemStatusOtionRequest(String oldStatus, String newStatus) {
		super();
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}
	public UpdateItemStatusOtionRequest() {
		super();
	}
	public String getOldStatus() {
		return oldStatus;
	}
	public void setOldStatus(String oldStatus) {
		this.oldStatus = oldStatus;
	}
	public String getNewStatus() {
		return newStatus;
	}
	public void setNewStatus(String newStatus) {
		this.newStatus = newStatus;
	}
	@Override
	public String toString() {
		return "UpdateItemStatusOtionRequest [oldStatus=" + oldStatus + ", newStatus=" + newStatus + "]";
	}
	
}
