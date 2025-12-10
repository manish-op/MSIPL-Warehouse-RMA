package com.serverManagement.server.management.entity.itemDetails;

import java.time.ZonedDateTime;

public class ItemDetailsDTOForCSV {

	private String serial_No;
	private String boxNo;
	private String partNo;
	private String rack_No;
	private String spare_Location;
	private String system;
	private String modelNo;
	private String moduleFor;
	private String system_Version;
	private String itemDescription;
	private String remark;
	private String empEmail;
	private String addedByEmail;
	private String partyName;
	private ZonedDateTime update_Date;
	private ZonedDateTime adding_Date;
	private String itemStatus;
	private String itemAvailableOption;
	private String city;
	private String keywordName;
	private String subKeyword;
	public ItemDetailsDTOForCSV(String serial_No, String boxNo, String partNo, String rack_No, String spare_Location,
			String system, String modelNo, String moduleFor, String system_Version, String itemDescription,
			String remark, String empEmail, String addedByEmail, String partyName, ZonedDateTime update_Date,
			ZonedDateTime adding_Date, String itemStatus, String itemAvailableOption, String city, String keywordName,
			String subKeyword) {
		super();
		this.serial_No = serial_No;
		this.boxNo = boxNo;
		this.partNo = partNo;
		this.rack_No = rack_No;
		this.spare_Location = spare_Location;
		this.system = system;
		this.modelNo = modelNo;
		this.moduleFor = moduleFor;
		this.system_Version = system_Version;
		this.itemDescription = itemDescription;
		this.remark = remark;
		this.empEmail = empEmail;
		this.addedByEmail = addedByEmail;
		this.partyName = partyName;
		this.update_Date = update_Date;
		this.adding_Date = adding_Date;
		this.itemStatus = itemStatus;
		this.itemAvailableOption = itemAvailableOption;
		this.city = city;
		this.keywordName = keywordName;
		this.subKeyword = subKeyword;
	}
	public ItemDetailsDTOForCSV() {
		super();
	}
	public String getSerial_No() {
		return serial_No;
	}
	public void setSerial_No(String serial_No) {
		this.serial_No = serial_No;
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
	public String getRack_No() {
		return rack_No;
	}
	public void setRack_No(String rack_No) {
		this.rack_No = rack_No;
	}
	public String getSpare_Location() {
		return spare_Location;
	}
	public void setSpare_Location(String spare_Location) {
		this.spare_Location = spare_Location;
	}
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
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
	public String getSystem_Version() {
		return system_Version;
	}
	public void setSystem_Version(String system_Version) {
		this.system_Version = system_Version;
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
	public ZonedDateTime getUpdate_Date() {
		return update_Date;
	}
	public void setUpdate_Date(ZonedDateTime update_Date) {
		this.update_Date = update_Date;
	}
	public ZonedDateTime getAdding_Date() {
		return adding_Date;
	}
	public void setAdding_Date(ZonedDateTime adding_Date) {
		this.adding_Date = adding_Date;
	}
	public String getItemStatus() {
		return itemStatus;
	}
	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}
	public String getItemAvailableOption() {
		return itemAvailableOption;
	}
	public void setItemAvailableOption(String itemAvailableOption) {
		this.itemAvailableOption = itemAvailableOption;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getKeywordName() {
		return keywordName;
	}
	public void setKeywordName(String keywordName) {
		this.keywordName = keywordName;
	}
	public String getSubKeyword() {
		return subKeyword;
	}
	public void setSubKeyword(String subKeyword) {
		this.subKeyword = subKeyword;
	}	
	
}
