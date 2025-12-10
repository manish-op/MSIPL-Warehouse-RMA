package com.serverManagement.server.management.entity.itemDetails;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serverManagement.server.management.entity.keyword.KeywordEntity;
import com.serverManagement.server.management.entity.keyword.SubKeywordEntity;
import com.serverManagement.server.management.entity.options.ItemAvailableStatusOptionEntity;
import com.serverManagement.server.management.entity.options.ItemStatusOptionEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Table(name="item-history-by-admin")
@Entity
public class ItemHistoryUpdatedByAdminEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String serial_No;//serial number of item
	private String boxNo;//location box number of item
	private String partNo;//partNo of item
	private String modelNo;//model number of item
	private String rack_No;//rack no or item
	@ManyToOne
	private ItemStatusOptionEntity itemStatusId;//for item new,old,repaired or other status
	private String spare_Location;//location of item
	private String system;//module type
	private String moduleFor;//module required for 
	private String system_Version;//module version
	@ManyToOne
	private ItemAvailableStatusOptionEntity availableStatusId;//automatically generate status issued and available at the time of add and assign
	private String itemDescription;//description can added at the time of adding items
	private String remark;//it can filled at the time of allotting item
	private String updatedByEmail;//it automatically taken the email id of user who adding component
	private String partyName;//party name required at the time of issue items
	private ZonedDateTime update_Date;
	@ManyToOne
	private RegionEntity region;
	@ManyToOne
	private KeywordEntity keywordEntity;
	@ManyToOne
	private SubKeywordEntity subKeyWordEntity;
	@ManyToOne
	@JsonIgnore
	private ItemDetailsEntity itemDetailsEntity;
	//constructor
	public ItemHistoryUpdatedByAdminEntity(Long id, String serial_No, String boxNo, String partNo, String modelNo,
			String rack_No, ItemStatusOptionEntity itemStatusId, String spare_Location, String system, String moduleFor,
			String system_Version, ItemAvailableStatusOptionEntity availableStatusId, String itemDescription,
			String remark, String updatedByEmail, String partyName, ZonedDateTime update_Date, RegionEntity region,
			KeywordEntity keywordEntity, SubKeywordEntity subKeyWordEntity, ItemDetailsEntity itemDetailsEntity) {
		super();
		this.id = id;
		this.serial_No = serial_No;
		this.boxNo = boxNo;
		this.partNo = partNo;
		this.modelNo = modelNo;
		this.rack_No = rack_No;
		this.itemStatusId = itemStatusId;
		this.spare_Location = spare_Location;
		this.system = system;
		this.moduleFor = moduleFor;
		this.system_Version = system_Version;
		this.availableStatusId = availableStatusId;
		this.itemDescription = itemDescription;
		this.remark = remark;
		this.updatedByEmail = updatedByEmail;
		this.partyName = partyName;
		this.update_Date = update_Date;
		this.region = region;
		this.keywordEntity = keywordEntity;
		this.subKeyWordEntity = subKeyWordEntity;
		this.itemDetailsEntity = itemDetailsEntity;
	}
	public ItemHistoryUpdatedByAdminEntity() {
		super();
	}
	//getter and setter
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getModelNo() {
		return modelNo;
	}
	public void setModelNo(String modelNo) {
		this.modelNo = modelNo;
	}
	public String getRack_No() {
		return rack_No;
	}
	public void setRack_No(String rack_No) {
		this.rack_No = rack_No;
	}
	public ItemStatusOptionEntity getItemStatusId() {
		return itemStatusId;
	}
	public void setItemStatusId(ItemStatusOptionEntity itemStatusId) {
		this.itemStatusId = itemStatusId;
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
	public ItemAvailableStatusOptionEntity getAvailableStatusId() {
		return availableStatusId;
	}
	public void setAvailableStatusId(ItemAvailableStatusOptionEntity availableStatusId) {
		this.availableStatusId = availableStatusId;
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
	public String getUpdatedByEmail() {
		return updatedByEmail;
	}
	public void setUpdatedByEmail(String updatedByEmail) {
		this.updatedByEmail = updatedByEmail;
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
	public RegionEntity getRegion() {
		return region;
	}
	public void setRegion(RegionEntity region) {
		this.region = region;
	}
	public KeywordEntity getKeywordEntity() {
		return keywordEntity;
	}
	public void setKeywordEntity(KeywordEntity keywordEntity) {
		this.keywordEntity = keywordEntity;
	}
	public SubKeywordEntity getSubKeyWordEntity() {
		return subKeyWordEntity;
	}
	public void setSubKeyWordEntity(SubKeywordEntity subKeyWordEntity) {
		this.subKeyWordEntity = subKeyWordEntity;
	}
	public ItemDetailsEntity getItemDetailsEntity() {
		return itemDetailsEntity;
	}
	public void setItemDetailsEntity(ItemDetailsEntity itemDetailsEntity) {
		this.itemDetailsEntity = itemDetailsEntity;
	}
	//toString
	@Override
	public String toString() {
		return "ItemHistoryUpdatedByAdminEntity [id=" + id + ", serial_No=" + serial_No + ", boxNo=" + boxNo
				+ ", partNo=" + partNo + ", modelNo=" + modelNo + ", rack_No=" + rack_No + ", itemStatusId="
				+ itemStatusId + ", spare_Location=" + spare_Location + ", system=" + system + ", moduleFor="
				+ moduleFor + ", system_Version=" + system_Version + ", availableStatusId=" + availableStatusId
				+ ", itemDescription=" + itemDescription + ", remark=" + remark + ", updatedByEmail=" + updatedByEmail
				+ ", partyName=" + partyName + ", update_Date=" + update_Date + ", region=" + region
				+ ", keywordEntity=" + keywordEntity + ", subKeyWordEntity=" + subKeyWordEntity + ", itemDetailsEntity="
				+ itemDetailsEntity + "]";
	}
	
}
