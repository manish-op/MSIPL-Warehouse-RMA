package com.serverManagement.server.management.request.gatepass;

public class AfterGatePassFruMakingRequest {

	String fruSerialNo;
	String rmaNo;
	String warrantyDetails;
	String faultDescription;
	String faultRemark;
	public AfterGatePassFruMakingRequest(String fruSerialNo, String rmaNo, String warrantyDetails,
			String faultDescription, String faultRemark) {
		super();
		this.fruSerialNo = fruSerialNo;
		this.rmaNo = rmaNo;
		this.warrantyDetails = warrantyDetails;
		this.faultDescription = faultDescription;
		this.faultRemark = faultRemark;
	}
	public AfterGatePassFruMakingRequest() {
		super();
	}
	public String getFruSerialNo() {
		return fruSerialNo;
	}
	public void setFruSerialNo(String fruSerialNo) {
		this.fruSerialNo = fruSerialNo;
	}
	public String getRmaNo() {
		return rmaNo;
	}
	public void setRmaNo(String rmaNo) {
		this.rmaNo = rmaNo;
	}
	public String getWarrantyDetails() {
		return warrantyDetails;
	}
	public void setWarrantyDetails(String warrantyDetails) {
		this.warrantyDetails = warrantyDetails;
	}
	public String getFaultDescription() {
		return faultDescription;
	}
	public void setFaultDescription(String faultDescription) {
		this.faultDescription = faultDescription;
	}
	public String getFaultRemark() {
		return faultRemark;
	}
	public void setFaultRemark(String faultRemark) {
		this.faultRemark = faultRemark;
	}
	
}
