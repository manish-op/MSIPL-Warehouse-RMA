package com.serverManagement.server.management.entity.itemRepairDetails;

import java.time.LocalDate;

import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.itemRepairOption.RepairingOptionEntity;
import com.serverManagement.server.management.entity.itemRepairOption.TechnicianStatusEntity;
import com.serverManagement.server.management.entity.itemRepairOption.WarrantyOptionEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

public class ItemDetailsDTO {

	private Long id;
	private String detailedExplaination;
	private String technician_Name;
	private RepairingOptionEntity repairStatus; 
	private TechnicianStatusEntity technicianStatus;
	private LocalDate generatedDate;
	private LocalDate lastUpdateDate; 
	private LocalDate tech_Assign_Date; 
	private String inwardGatepass;
	private WarrantyOptionEntity warrantyDetails;
	private String docketIdInward;
	private String ticketGeneratedBy;
	private ItemDetailsEntity itemDetailId; 
	private RegionEntity region;
	private FruEntity fruId;
	public ItemDetailsDTO(Long id, String detailedExplaination, String technician_Name,
			RepairingOptionEntity repairStatus, TechnicianStatusEntity technicianStatus, LocalDate generatedDate,
			LocalDate lastUpdateDate, LocalDate tech_Assign_Date, String inwardGatepass,
			WarrantyOptionEntity warrantyDetails, String docketIdInward, String ticketGeneratedBy,
			ItemDetailsEntity itemDetailId, RegionEntity region, FruEntity fruId) {
		super();
		this.id = id;
		this.detailedExplaination = detailedExplaination;
		this.technician_Name = technician_Name;
		this.repairStatus = repairStatus;
		this.technicianStatus = technicianStatus;
		this.generatedDate = generatedDate;
		this.lastUpdateDate = lastUpdateDate;
		this.tech_Assign_Date = tech_Assign_Date;
		this.inwardGatepass = inwardGatepass;
		this.warrantyDetails = warrantyDetails;
		this.docketIdInward = docketIdInward;
		this.ticketGeneratedBy = ticketGeneratedBy;
		this.itemDetailId = itemDetailId;
		this.region = region;
		this.fruId = fruId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDetailedExplaination() {
		return detailedExplaination;
	}
	public void setDetailedExplaination(String detailedExplaination) {
		this.detailedExplaination = detailedExplaination;
	}
	public String getTechnician_Name() {
		return technician_Name;
	}
	public void setTechnician_Name(String technician_Name) {
		this.technician_Name = technician_Name;
	}
	public RepairingOptionEntity getRepairStatus() {
		return repairStatus;
	}
	public void setRepairStatus(RepairingOptionEntity repairStatus) {
		this.repairStatus = repairStatus;
	}
	public TechnicianStatusEntity getTechnicianStatus() {
		return technicianStatus;
	}
	public void setTechnicianStatus(TechnicianStatusEntity technicianStatus) {
		this.technicianStatus = technicianStatus;
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
	public String getInwardGatepass() {
		return inwardGatepass;
	}
	public void setInwardGatepass(String inwardGatepass) {
		this.inwardGatepass = inwardGatepass;
	}
	public WarrantyOptionEntity getWarrantyDetails() {
		return warrantyDetails;
	}
	public void setWarrantyDetails(WarrantyOptionEntity warrantyDetails) {
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
	public ItemDetailsEntity getItemDetailId() {
		return itemDetailId;
	}
	public void setItemDetailId(ItemDetailsEntity itemDetailId) {
		this.itemDetailId = itemDetailId;
	}
	public RegionEntity getRegion() {
		return region;
	}
	public void setRegion(RegionEntity region) {
		this.region = region;
	}
	public FruEntity getFruId() {
		return fruId;
	}
	public void setFruId(FruEntity fruId) {
		this.fruId = fruId;
	}
	
	

}
