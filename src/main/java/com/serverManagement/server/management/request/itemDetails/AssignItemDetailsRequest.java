package com.serverManagement.server.management.request.itemDetails;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignItemDetailsRequest {

	private String serialNo;
	private String boxNo;
	private String rackNo;
	private String itemStatus;
	private String spareLocation;
	private String itemDescription;
	private String partyName;
	private String remark;
	private String itemAvailability;
	// for admin or manager
	private String modelNo;
	private String moduleFor;
	private String systemName;
	private String systemVersion;
	private String keywordName;
	private String subKeywordName;
	// for admin only
	private String region;
	private Integer returnDuration;

	public AssignItemDetailsRequest(String serialNo, String boxNo, String rackNo, String itemStatus,
			String spareLocation, String itemDescription, String partyName, String remark, String itemAvailability,
			String modelNo, String moduleFor, String systemName, String systemVersion, String keywordName,
			String subKeywordName, String region, Integer returnDuration) {
		super();
		this.serialNo = serialNo;
		this.boxNo = boxNo;
		this.rackNo = rackNo;
		this.itemStatus = itemStatus;
		this.spareLocation = spareLocation;
		this.itemDescription = itemDescription;
		this.partyName = partyName;
		this.remark = remark;
		this.itemAvailability = itemAvailability;
		this.modelNo = modelNo;
		this.moduleFor = moduleFor;
		this.systemName = systemName;
		this.systemVersion = systemVersion;
		this.keywordName = keywordName;
		this.subKeywordName = subKeywordName;
		this.region = region;
		this.returnDuration = returnDuration;
	}

	public AssignItemDetailsRequest() {
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

	public String getRackNo() {
		return rackNo;
	}

	public void setRackNo(String rackNo) {
		this.rackNo = rackNo;
	}

	public String getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}

	public String getSpareLocation() {
		return spareLocation;
	}

	public void setSpareLocation(String spareLocation) {
		this.spareLocation = spareLocation;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
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

	public String getItemAvailability() {
		return itemAvailability;
	}

	public void setItemAvailability(String itemAvailability) {
		this.itemAvailability = itemAvailability;
	}

	public String getModelNo() {
		return modelNo;
	}

	public void setModelNo(String modelNo) {
		this.modelNo = modelNo;
	}

	public String getModuleFor() {
		return moduleFor;
	}

	public void setModuleFor(String moduleFor) {
		this.moduleFor = moduleFor;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getSystemVersion() {
		return systemVersion;
	}

	public void setSystemVersion(String systemVersion) {
		this.systemVersion = systemVersion;
	}

	public String getKeywordName() {
		return keywordName;
	}

	public void setKeywordName(String keywordName) {
		this.keywordName = keywordName;
	}

	public String getSubKeywordName() {
		return subKeywordName;
	}

	public void setSubKeywordName(String subKeywordName) {
		this.subKeywordName = subKeywordName;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public Integer getReturnDuration() {
		return returnDuration;
	}

	public void setReturnDuration(Integer returnDuration) {
		this.returnDuration = returnDuration;
	}

	@Override
	public String toString() {
		return "AssignItemDetailsRequest [serialNo=" + serialNo + ", boxNo=" + boxNo + ", rackNo=" + rackNo
				+ ", itemStatus=" + itemStatus + ", spareLocation=" + spareLocation + ", itemDescription="
				+ itemDescription + ", partyName=" + partyName + ", remark=" + remark + ", itemAvailability="
				+ itemAvailability + ", modelNo=" + modelNo + ", moduleFor=" + moduleFor + ", systemName=" + systemName
				+ ", systemVersion=" + systemVersion + ", keywordName=" + keywordName + ", subKeywordName="
				+ subKeywordName + ", region=" + region + ", returnDuration=" + returnDuration + "]";
	}

}
