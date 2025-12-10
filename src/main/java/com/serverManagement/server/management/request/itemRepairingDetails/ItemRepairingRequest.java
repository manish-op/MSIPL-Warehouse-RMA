package com.serverManagement.server.management.request.itemRepairingDetails;

public class ItemRepairingRequest {

	private String serialNo;
	private String detailedExplaination;
	private String inwardGatepass;
	private String warrantyDetails;
	public ItemRepairingRequest(String serialNo, String detailedExplaination, String inwardGatepass,
			String warrantyDetails) {
		super();
		this.serialNo = serialNo;
		this.detailedExplaination = detailedExplaination;
		this.inwardGatepass = inwardGatepass;
		this.warrantyDetails = warrantyDetails;
	}
	public ItemRepairingRequest() {
		super();
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getDetailedExplaination() {
		return detailedExplaination;
	}
	public void setDetailedExplaination(String detailedExplaination) {
		this.detailedExplaination = detailedExplaination;
	}
	public String getInwardGatepass() {
		return inwardGatepass;
	}
	public void setInwardGatepass(String inwardGatepass) {
		this.inwardGatepass = inwardGatepass;
	}
	public String getWarrantyDetails() {
		return warrantyDetails;
	}
	public void setWarrantyDetails(String warrantyDetails) {
		this.warrantyDetails = warrantyDetails;
	}
	
}
