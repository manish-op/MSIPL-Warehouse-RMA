package com.serverManagement.server.management.request.region;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddRegionRequest {

	private String regionName;

	public AddRegionRequest(String regionName) {
		super();
		this.regionName = regionName;
	}

	public AddRegionRequest() {
		super();
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	@Override
	public String toString() {
		return "AddRegionRequest [regionName=" + regionName + "]";
	}

	
}
