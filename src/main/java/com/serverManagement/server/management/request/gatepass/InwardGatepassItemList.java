package com.serverManagement.server.management.request.gatepass;



public class InwardGatepassItemList {

	private String rmaNo;
	private String serialNo;
	private String keywordName;
	private String subkeywordName;
	private String faultDescription;
	private String remark;
	private String rackNo;
	private String spareLocation;
	private String partNo;
	private String systemName;
	private String moduleFor;
	private String modeuleVersion;
	private String itemStatus;
	private String faultRemark;
	private String docketInward;
	private String warrantyDetails;
	private boolean fru;
		

	public InwardGatepassItemList(String rmaNo, String serialNo, String keywordName, String subkeywordName,
			String faultDescription, String remark, String rackNo, String spareLocation, String partNo,
			String systemName, String moduleFor, String modeuleVersion, String itemStatus, String faultRemark,
			String docketInward, String warrantyDetails, boolean fru) {
		super();
		this.rmaNo = rmaNo;
		this.serialNo = serialNo;
		this.keywordName = keywordName;
		this.subkeywordName = subkeywordName;
		this.faultDescription = faultDescription;
		this.remark = remark;
		this.rackNo = rackNo;
		this.spareLocation = spareLocation;
		this.partNo = partNo;
		this.systemName = systemName;
		this.moduleFor = moduleFor;
		this.modeuleVersion = modeuleVersion;
		this.itemStatus = itemStatus;
		this.faultRemark = faultRemark;
		this.docketInward = docketInward;
		this.warrantyDetails = warrantyDetails;
		this.fru = fru;
	}
	public InwardGatepassItemList() {
		super();
	}
	public String getRmaNo() {
		return rmaNo;
	}
	public void setRmaNo(String rmaNo) {
		this.rmaNo = rmaNo;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getKeywordName() {
		return keywordName;
	}
	public void setKeywordName(String keywordName) {
		this.keywordName = keywordName;
	}
	public String getSubkeywordName() {
		return subkeywordName;
	}
	public void setSubkeywordName(String subkeywordName) {
		this.subkeywordName = subkeywordName;
	}
	public String getFaultDescription() {
		return faultDescription;
	}
	public void setFaultDescription(String faultDescription) {
		this.faultDescription = faultDescription;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getRackNo() {
		return rackNo;
	}
	public void setRackNo(String rackNo) {
		this.rackNo = rackNo;
	}
	public String getSpareLocation() {
		return spareLocation;
	}
	public void setSpareLocation(String spareLocation) {
		this.spareLocation = spareLocation;
	}
	public String getSystemName() {
		return systemName;
	}
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}
	public String getModuleFor() {
		return moduleFor;
	}
	public void setModuleFor(String moduleFor) {
		this.moduleFor = moduleFor;
	}
	public String getModeuleVersion() {
		return modeuleVersion;
	}
	public void setModeuleVersion(String modeuleVersion) {
		this.modeuleVersion = modeuleVersion;
	}
	public String getItemStatus() {
		return itemStatus;
	}
	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}
	public String getFaultRemark() {
		return faultRemark;
	}
	public void setFaultRemark(String faultRemark) {
		this.faultRemark = faultRemark;
	}
	public String getDocketInward() {
		return docketInward;
	}
	public void setDocketInward(String docketInward) {
		this.docketInward = docketInward;
	}

	public String getWarrantyDetails() {
		return warrantyDetails;
	}
	public void setWarrantyDetails(String warrantyDetails) {
		this.warrantyDetails = warrantyDetails;
	}
	public boolean isFru() {
		return fru;
	}
	public void setFru(boolean fru) {
		this.fru = fru;
	}
	public String getPartNo() {
		return partNo;
	}
	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}
	
	
	
}
