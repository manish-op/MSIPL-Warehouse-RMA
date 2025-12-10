package com.serverManagement.server.management.request.region;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateRegionRequest {

	private String oldRegion;
	private String updatedRegion;
	public UpdateRegionRequest(String oldRegion, String updatedRegion) {
		super();
		this.oldRegion = oldRegion;
		this.updatedRegion = updatedRegion;
	}
	public UpdateRegionRequest() {
		super();
	}
	public String getOldRegion() {
		return oldRegion;
	}
	public void setOldRegion(String oldRegion) {
		this.oldRegion = oldRegion;
	}
	public String getUpdatedRegion() {
		return updatedRegion;
	}
	public void setUpdatedRegion(String updatedRegion) {
		this.updatedRegion = updatedRegion;
	}
	@Override
	public String toString() {
		return "UpdateRegionRequest [oldRegion=" + oldRegion + ", updatedRegion=" + updatedRegion + "]";
	}
	
}
