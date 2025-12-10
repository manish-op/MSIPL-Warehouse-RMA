package com.serverManagement.server.management.dto.itemRepairDetails;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDetailsViaIdDTO {

 	private Long id;
    private String faultDetails;
    private String faultRemark;
    private String rmaNo;
    private String serialNo;
    private String technician_Name;
    private LocalDate generatedDate;
    private LocalDate lastUpdateDate;
    private LocalDate tech_Assign_Date;
    private Long outwardGatepass;
    private String docketIdInward;
    private String docketIdOutward;
    private String partNo;
    private String ticketGeneratedBy;
    private String assignByManager;
    private String statusOption;
    private String technicianAssign;
    private Long inwardGatepass;
    private String warrantyDetails;
    private String regionName; 
    private String system;    
    private String keywordName;
    private String subKeywordName;
    private List<String> employeeList;
	public TicketDetailsViaIdDTO(Long id, String faultDetails, String faultRemark, String rmaNo, String serialNo,
			String technician_Name, LocalDate generatedDate, LocalDate lastUpdateDate, LocalDate tech_Assign_Date,
			Long outwardGatepass, String docketIdInward, String docketIdOutward, String partNo,
			String ticketGeneratedBy, String assignByManager, String statusOption, String technicianAssign,
			Long inwardGatepass, String warrantyDetails, String regionName, String system, String keywordName,
			String subKeywordName) {
		super();
		this.id = id;
		this.faultDetails = faultDetails;
		this.faultRemark = faultRemark;
		this.rmaNo = rmaNo;
		this.serialNo = serialNo;
		this.technician_Name = technician_Name;
		this.generatedDate = generatedDate;
		this.lastUpdateDate = lastUpdateDate;
		this.tech_Assign_Date = tech_Assign_Date;
		this.outwardGatepass = outwardGatepass;
		this.docketIdInward = docketIdInward;
		this.docketIdOutward = docketIdOutward;
		this.partNo = partNo;
		this.ticketGeneratedBy = ticketGeneratedBy;
		this.assignByManager = assignByManager;
		this.statusOption = statusOption;
		this.technicianAssign = technicianAssign;
		this.inwardGatepass = inwardGatepass;
		this.warrantyDetails = warrantyDetails;
		this.regionName = regionName;
		this.system = system;
		this.keywordName = keywordName;
		this.subKeywordName = subKeywordName;
	}
	public TicketDetailsViaIdDTO() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFaultDetails() {
		return faultDetails;
	}
	public void setFaultDetails(String faultDetails) {
		this.faultDetails = faultDetails;
	}
	public String getFaultRemark() {
		return faultRemark;
	}
	public void setFaultRemark(String faultRemark) {
		this.faultRemark = faultRemark;
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
	public String getTechnician_Name() {
		return technician_Name;
	}
	public void setTechnician_Name(String technician_Name) {
		this.technician_Name = technician_Name;
	}
	public LocalDate getGeneratedDate() {
		return generatedDate;
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
	public LocalDate getTech_Assign_Date() {
		return tech_Assign_Date;
	}
	public void setTech_Assign_Date(LocalDate tech_Assign_Date) {
		this.tech_Assign_Date = tech_Assign_Date;
	}
	public Long getOutwardGatepass() {
		return outwardGatepass;
	}
	public void setOutwardGatepass(Long outwardGatepass) {
		this.outwardGatepass = outwardGatepass;
	}
	public String getDocketIdInward() {
		return docketIdInward;
	}
	public void setDocketIdInward(String docketIdInward) {
		this.docketIdInward = docketIdInward;
	}
	public String getDocketIdOutward() {
		return docketIdOutward;
	}
	public void setDocketIdOutward(String docketIdOutward) {
		this.docketIdOutward = docketIdOutward;
	}
	public String getPartNo() {
		return partNo;
	}
	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}
	public String getTicketGeneratedBy() {
		return ticketGeneratedBy;
	}
	public void setTicketGeneratedBy(String ticketGeneratedBy) {
		this.ticketGeneratedBy = ticketGeneratedBy;
	}
	public String getAssignByManager() {
		return assignByManager;
	}
	public void setAssignByManager(String assignByManager) {
		this.assignByManager = assignByManager;
	}
	public String getStatusOption() {
		return statusOption;
	}
	public void setStatusOption(String statusOption) {
		this.statusOption = statusOption;
	}
	public String getTechnicianAssign() {
		return technicianAssign;
	}
	public void setTechnicianAssign(String technicianAssign) {
		this.technicianAssign = technicianAssign;
	}
	public Long getInwardGatepass() {
		return inwardGatepass;
	}
	public void setInwardGatepass(Long inwardGatepass) {
		this.inwardGatepass = inwardGatepass;
	}
	public String getWarrantyDetails() {
		return warrantyDetails;
	}
	public void setWarrantyDetails(String warrantyDetails) {
		this.warrantyDetails = warrantyDetails;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
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
	public List<String> getEmployeeList() {
		return employeeList;
	}
	public void setEmployeeList(List<String> employeeList) {
		this.employeeList = employeeList;
	}
	
	
}
