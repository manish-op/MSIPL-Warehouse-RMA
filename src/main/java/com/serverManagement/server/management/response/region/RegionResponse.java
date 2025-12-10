package com.serverManagement.server.management.response.region;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegionResponse {

	private String city;

	public RegionResponse(String city) {
		super();
		this.city = city;
	}

	public RegionResponse() {
		super();
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String toString() {
		return "RegionResponse [city=" + city + "]";
	}
	
}
