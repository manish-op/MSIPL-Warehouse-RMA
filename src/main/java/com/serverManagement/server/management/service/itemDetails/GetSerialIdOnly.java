package com.serverManagement.server.management.service.itemDetails;

//this class is used for get data from component table only serial number and id extract from table by Serial nu
public class GetSerialIdOnly {

	private Long id;
	private String serialNo;
	public GetSerialIdOnly(Long id, String serialNo) {
		super();
		this.id = id;
		this.serialNo = serialNo;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
}
