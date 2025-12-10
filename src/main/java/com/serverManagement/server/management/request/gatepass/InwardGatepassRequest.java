package com.serverManagement.server.management.request.gatepass;

import java.util.List;



public class InwardGatepassRequest {

	private String partyName;
	private String partyContact;
	private String partyAddress;
	private String region;
	private List<InwardGatepassItemList> itemList;
	public InwardGatepassRequest(String partyName, String partyContact, String partyAddress, String region,
			List<InwardGatepassItemList> itemList) {
		super();
		this.partyName = partyName;
		this.partyContact = partyContact;
		this.partyAddress = partyAddress;
		this.region = region;
		this.itemList = itemList;
	}
	public InwardGatepassRequest() {
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
	public List<InwardGatepassItemList> getItemList() {
		return itemList;
	}
	public void setItemList(List<InwardGatepassItemList> itemList) {
		this.itemList = itemList;
	}
	
}
