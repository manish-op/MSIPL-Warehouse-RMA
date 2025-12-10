package com.serverManagement.server.management.response.itemDetails;

import java.time.ZonedDateTime;

public class ItemDetailsResponse {
	
	private Long itemId;
	private String serialNo;
	private String boxNo;
	private String partNo;
	private String modelNo;
	private String rackNo;
	private String itemStatus;
	private String spareLocation;
	private String system;
	private String systemVersion;
	private String moduleFor;
	private String itemAvailability;
	private String itemDescription;
	private String remark;
	private String empEmail;
	private String addedByEmail;
	private String partyName;
	private ZonedDateTime updateDate;
	private ZonedDateTime addingDate;
	private String region;
	private String keyword;
	private String subKeyword;
	//constructor
	public ItemDetailsResponse(Long itemId, String serialNo, String boxNo, String partNo, String modelNo, String rackNo,
			String itemStatus, String spareLocation, String system, String systemVersion, String moduleFor,
			String itemAvailability, String itemDescription, String remark, String empEmail, String addedByEmail,
			String partyName, ZonedDateTime updateDate, ZonedDateTime addingDate, String region, String keyword,
			String subKeyword) {
		super();
		this.itemId = itemId;
		this.serialNo = serialNo;
		this.boxNo = boxNo;
		this.partNo = partNo;
		this.modelNo = modelNo;
		this.rackNo = rackNo;
		this.itemStatus = itemStatus;
		this.spareLocation = spareLocation;
		this.system = system;
		this.systemVersion = systemVersion;
		this.moduleFor = moduleFor;
		this.itemAvailability = itemAvailability;
		this.itemDescription = itemDescription;
		this.remark = remark;
		this.empEmail = empEmail;
		this.addedByEmail = addedByEmail;
		this.partyName = partyName;
		this.updateDate = updateDate;
		this.addingDate = addingDate;
		this.region = region;
		this.keyword = keyword;
		this.subKeyword = subKeyword;
	}
	public ItemDetailsResponse() {
		super();
	}
	//getter and setter
	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
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
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
	public String getSystemVersion() {
		return systemVersion;
	}
	public void setSystemVersion(String systemVersion) {
		this.systemVersion = systemVersion;
	}
	public String getModuleFor() {
		return moduleFor;
	}
	public void setModuleFor(String moduleFor) {
		this.moduleFor = moduleFor;
	}
	public String getItemAvailability() {
		return itemAvailability;
	}
	public void setItemAvailability(String itemAvailability) {
		this.itemAvailability = itemAvailability;
	}
	public String getItemDescription() {
		return itemDescription;
	}
	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getEmpEmail() {
		return empEmail;
	}
	public void setEmpEmail(String empEmail) {
		this.empEmail = empEmail;
	}
	public String getAddedByEmail() {
		return addedByEmail;
	}
	public void setAddedByEmail(String addedByEmail) {
		this.addedByEmail = addedByEmail;
	}
	public String getPartyName() {
		return partyName;
	}
	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}
	public ZonedDateTime getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(ZonedDateTime updateDate) {
		this.updateDate = updateDate;
	}
	public ZonedDateTime getAddingDate() {
		return addingDate;
	}
	public void setAddingDate(ZonedDateTime addingDate) {
		this.addingDate = addingDate;
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
	//toString
	@Override
	public String toString() {
		return "ItemDetailsResponse [itemId=" + itemId + ", serialNo=" + serialNo + ", boxNo=" + boxNo + ", partNo="
				+ partNo + ", modelNo=" + modelNo + ", rackNo=" + rackNo + ", itemStatus=" + itemStatus
				+ ", spareLocation=" + spareLocation + ", system=" + system + ", systemVersion=" + systemVersion
				+ ", moduleFor=" + moduleFor + ", itemAvailability=" + itemAvailability + ", itemDescription="
				+ itemDescription + ", remark=" + remark + ", empEmail=" + empEmail + ", addedByEmail=" + addedByEmail
				+ ", partyName=" + partyName + ", updateDate=" + updateDate + ", addingDate=" + addingDate + ", region="
				+ region + ", keyword=" + keyword + ", subKeyword=" + subKeyword + "]";
	}
	
}