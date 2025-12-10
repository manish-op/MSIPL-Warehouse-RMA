package com.serverManagement.server.management.entity.itemRepairDetails;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.serverManagement.server.management.entity.region.RegionEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemRepairingEntityDTO {

	 	private Long id;
	    private String faultDetail;
	    private String repairStatus;
	    private String serial_No;
	    private String rmaNo;
	    private String system;
	    private String technicianName;
	    private String keywordName;
	    private String subKeywordName;
	    private String statusOption;
	    private String technicianStatus;
	    private LocalDate generatedDate;
	    private LocalDate lastUpdateDate;
	    private LocalDate techAssignDate;
	    private String assignByManager;
	    private String inwardGatepass;
	    private String warrantyDetails;
	    private String docketIdInward;
	    private String ticketGeneratedBy;
	    private String partNo;
	    private String regionName;  //  Keep as entity if you need the full object
	    private String faultRemark;
	    
		public ItemRepairingEntityDTO() {
			super();
		}
		
		//constructor for show details to manager for get details
		public ItemRepairingEntityDTO(Long id, String faultDetail, String repairStatus, String serial_No, String rmaNo,
				String system, String keywordName, String subKeywordName, String technicianStatus,
				LocalDate generatedDate, String partNo, String faultRemark) {
			super();
			this.id = id;
			this.faultDetail = faultDetail;
			this.repairStatus = repairStatus;
			this.serial_No = serial_No;
			this.rmaNo = rmaNo;
			this.system = system;
			this.keywordName = keywordName;
			this.subKeywordName = subKeywordName;
			this.technicianStatus = technicianStatus;
			this.generatedDate = generatedDate;
			this.partNo = partNo;
			this.faultRemark = faultRemark;
		}
		
		//constructor for show employee assign ticket details
		public ItemRepairingEntityDTO(Long id, String faultDetail, String repairStatus, String serial_No, String rmaNo,
				String system, String keywordName, String subKeywordName, LocalDate generatedDate, String partNo,
				String faultRemark, String assignByManager, LocalDate techAssignDate, LocalDate lastUpdateDate) {
			super();
			this.id = id;
			this.faultDetail = faultDetail;
			this.repairStatus = repairStatus;
			this.serial_No = serial_No;
			this.rmaNo = rmaNo;
			this.system = system;
			this.keywordName = keywordName;
			this.subKeywordName = subKeywordName;
			this.generatedDate = generatedDate;
			this.partNo = partNo;
			this.faultRemark = faultRemark;
			this.assignByManager = assignByManager;
			this.techAssignDate = techAssignDate;
			this.lastUpdateDate = lastUpdateDate;			
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getFaultDetail() {
			return faultDetail;
		}

		public void setFaultDetail(String faultDetail) {
			this.faultDetail = faultDetail;
		}

		public String getRepairStatus() {
			return repairStatus;
		}

		public void setRepairStatus(String repairStatus) {
			this.repairStatus = repairStatus;
		}

		public String getSerial_No() {
			return serial_No;
		}

		public void setSerial_No(String serial_No) {
			this.serial_No = serial_No;
		}

		public String getRmaNo() {
			return rmaNo;
		}

		public void setRmaNo(String rmaNo) {
			this.rmaNo = rmaNo;
		}

		public String getSystem() {
			return system;
		}

		public void setSystem(String system) {
			this.system = system;
		}

		public String getTechnicianName() {
			return technicianName;
		}

		public void setTechnicianName(String technicianName) {
			this.technicianName = technicianName;
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

		public String getStatusOption() {
			return statusOption;
		}

		public void setStatusOption(String statusOption) {
			this.statusOption = statusOption;
		}

		public String getTechnicianStatus() {
			return technicianStatus;
		}

		public void setTechnicianStatus(String technicianStatus) {
			this.technicianStatus = technicianStatus;
		}

		public LocalDate getGeneratedDate() {
			return generatedDate;
		}

		public String getAssignByManager() {
			return assignByManager;
		}

		public void setAssignByManager(String assignByManager) {
			this.assignByManager = assignByManager;
		}

		public void setGeneratedDate(LocalDate generatedDate) {
			this.generatedDate = generatedDate;
		}

		public LocalDate getLastUpdateDate() {
			return lastUpdateDate;
		}

		public void setLastUpdateDate(LocalDate lastUpdateDate) {
			this.lastUpdateDate = lastUpdateDate;
		}

		public LocalDate getTechAssignDate() {
			return techAssignDate;
		}

		public void setTechAssignDate(LocalDate techAssignDate) {
			this.techAssignDate = techAssignDate;
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

		public String getDocketIdInward() {
			return docketIdInward;
		}

		public void setDocketIdInward(String docketIdInward) {
			this.docketIdInward = docketIdInward;
		}

		public String getTicketGeneratedBy() {
			return ticketGeneratedBy;
		}

		public void setTicketGeneratedBy(String ticketGeneratedBy) {
			this.ticketGeneratedBy = ticketGeneratedBy;
		}

		public String getPartNo() {
			return partNo;
		}

		public void setPartNo(String partNo) {
			this.partNo = partNo;
		}

		public String getRegionName() {
			return regionName;
		}

		public void setRegionName(String regionName) {
			this.regionName = regionName;
		}

		public String getFaultRemark() {
			return faultRemark;
		}

		public void setFaultRemark(String faultRemark) {
			this.faultRemark = faultRemark;
		}
		
	        
}
