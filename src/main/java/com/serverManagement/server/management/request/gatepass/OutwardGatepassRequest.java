package com.serverManagement.server.management.request.gatepass;

import java.util.List;

public class OutwardGatepassRequest {
	private String partyName;
	private String partyContact;
	private String partyAddress;
	private String region;
	private List<OutwardGatepassItemList> itemList;	
	
	public OutwardGatepassRequest(String partyName, String partyContact, String partyAddress, String region,
			List<OutwardGatepassItemList> itemList) {
		super();
		this.partyName = partyName;
		this.partyContact = partyContact;
		this.partyAddress = partyAddress;
		this.region = region;
		this.itemList = itemList;
	}
	public OutwardGatepassRequest() {
		super();
	}
	public String getPartyName() {
		return partyName;
	}
	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}
	public String getPartyContact() {
		return partyContact;
	}
	public void setPartyContact(String partyContact) {
		this.partyContact = partyContact;
	}
	public String getPartyAddress() {
		return partyAddress;
	}
	public void setPartyAddress(String partyAddress) {
		this.partyAddress = partyAddress;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public List<OutwardGatepassItemList> getItemList() {
		return itemList;
	}
	public void setItemList(List<OutwardGatepassItemList> itemList) {
		this.itemList = itemList;
	}
	
}
