package com.serverManagement.server.management.request.itemDetails;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddItemRequest {

	private String serialNo;
	private String boxNo;
	private String partNo;
	private String modelNo;
	private String itemStatus;
	private String rackNo;
	private String itemDescription;
	private String spareLocation;
	private String system;
	private String moduleFor;
	private String systemVersion;
	private String region;
	private String keyword;
	private String subKeyword;
	private String availableStatus;
	private String partyName;
	private String remark;
	public AddItemRequest(String serialNo, String boxNo, String partNo, String modelNo, String itemStatus,
			String rackNo, String itemDescription, String spareLocation, String system, String moduleFor,
			String systemVersion, String region, String keyword, String subKeyword, String availableStatus,
			String partyName, String remark) {
		super();
		this.serialNo = serialNo;
		this.boxNo = boxNo;
		this.partNo = partNo;
		this.modelNo = modelNo;
		this.itemStatus = itemStatus;
		this.rackNo = rackNo;
		this.itemDescription = itemDescription;
		this.spareLocation = spareLocation;
		this.system = system;
		this.moduleFor = moduleFor;
		this.systemVersion = systemVersion;
		this.region = region;
		this.keyword = keyword;
		this.subKeyword = subKeyword;
		this.availableStatus = availableStatus;
		this.partyName = partyName;
		this.remark = remark;
	}
	public AddItemRequest() {
		super();
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getBoxNo() {
		return boxNo;
	}
	public void setBoxNo(String boxNo) {
		this.boxNo = boxNo;
	}
	public String getPartNo() {
		return partNo;
	}
	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}
	public String getModelNo() {
		return modelNo;
	}
	public void setModelNo(String modelNo) {
		this.modelNo = modelNo;
	}
	public String getItemStatus() {
		return itemStatus;
	}
	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}
	public String getRackNo() {
		return rackNo;
	}
	public void setRackNo(String rackNo) {
		this.rackNo = rackNo;
	}
	public String getItemDescription() {
		return itemDescription;
	}
	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}
	public String getSpareLocation() {
		return spareLocation;
	}
	public void setSpareLocation(String spareLocation) {
		this.spareLocation = spareLocation;
	}
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
	public String getModuleFor() {
		return moduleFor;
	}
	public void setModuleFor(String moduleFor) {
		this.moduleFor = moduleFor;
	}
	public String getSystemVersion() {
		return systemVersion;
	}
	public void setSystemVersion(String systemVersion) {
		this.systemVersion = systemVersion;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getSubKeyword() {
		return subKeyword;
	}
	public void setSubKeyword(String subKeyword) {
		this.subKeyword = subKeyword;
	}
	public String getAvailableStatus() {
		return availableStatus;
	}
	public void setAvailableStatus(String availableStatus) {
		this.availableStatus = availableStatus;
	}
	public String getPartyName() {
		return partyName;
	}
	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	@Override
	public String toString() {
		return "AddItemRequest [serialNo=" + serialNo + ", boxNo=" + boxNo + ", partNo=" + partNo + ", modelNo="
				+ modelNo + ", itemStatus=" + itemStatus + ", rackNo=" + rackNo + ", itemDescription=" + itemDescription
				+ ", spareLocation=" + spareLocation + ", system=" + system + ", moduleFor=" + moduleFor
				+ ", systemVersion=" + systemVersion + ", region=" + region + ", keyword=" + keyword + ", subKeyword="
				+ subKeyword + ", availableStatus=" + availableStatus + ", partyName=" + partyName + ", remark="
				+ remark + "]";
	}
	
}
