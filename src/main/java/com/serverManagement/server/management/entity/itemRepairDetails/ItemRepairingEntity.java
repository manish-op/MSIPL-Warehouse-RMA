package com.serverManagement.server.management.entity.itemRepairDetails;

import java.time.LocalDate;
import java.util.List;

import com.serverManagement.server.management.entity.gatePass.InwardGatePassEntity;
import com.serverManagement.server.management.entity.gatePass.OutwardGatepassEntity;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.itemRepairOption.RepairingOptionEntity;
import com.serverManagement.server.management.entity.itemRepairOption.TechnicianStatusEntity;
import com.serverManagement.server.management.entity.itemRepairOption.WarrantyOptionEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class ItemRepairingEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String faultDetails;
	private String faultRemark;
	private String rmaNo;
	private String serialNo;
	private String partNo;
	private String technician_Name;
	@ManyToOne()
	private RepairingOptionEntity repairStatus;
	@ManyToOne()
	private TechnicianStatusEntity technicianStatus;
	private LocalDate generatedDate;
	private LocalDate lastUpdateDate;
	private LocalDate tech_Assign_Date;
	@ManyToOne()
	private InwardGatePassEntity inwardGatepass;
	@ManyToOne()
	private OutwardGatepassEntity outwardGatePass;
	@ManyToOne()
	private WarrantyOptionEntity warrantyDetails;
	private String docketIdInward;
	private String docketIdOutward;
	private String replaceItemSerial;
	private String ticketGeneratedBy;
	private String assignByManager;
	private String repairingRemark;
	@ManyToOne
	private ItemDetailsEntity itemDetailId;
	@ManyToOne
	private RegionEntity region;
	@OneToMany 
	@JoinColumn(name = "comment_id")
	private List<RepairingCommentsEntity> commentList;
	@OneToOne()
	private FruEntity fruId;
	
	
	public ItemRepairingEntity(Long id, String faultDetails, String faultRemark, String rmaNo, String serialNo,
			String partNo, String technician_Name, RepairingOptionEntity repairStatus,
			TechnicianStatusEntity technicianStatus, LocalDate generatedDate, LocalDate lastUpdateDate,
			LocalDate tech_Assign_Date, InwardGatePassEntity inwardGatepass, OutwardGatepassEntity outwardGatePass,
			WarrantyOptionEntity warrantyDetails, String docketIdInward, String docketIdOutward,
			String replaceItemSerial, String ticketGeneratedBy, String assignByManager, String repairingRemark,
			ItemDetailsEntity itemDetailId, RegionEntity region, List<RepairingCommentsEntity> commentList,
			FruEntity fruId) {
		super();
		this.id = id;
		this.faultDetails = faultDetails;
		this.faultRemark = faultRemark;
		this.rmaNo = rmaNo;
		this.serialNo = serialNo;
		this.partNo = partNo;
		this.technician_Name = technician_Name;
		this.repairStatus = repairStatus;
		this.technicianStatus = technicianStatus;
		this.generatedDate = generatedDate;
		this.lastUpdateDate = lastUpdateDate;
		this.tech_Assign_Date = tech_Assign_Date;
		this.inwardGatepass = inwardGatepass;
		this.outwardGatePass = outwardGatePass;
		this.warrantyDetails = warrantyDetails;
		this.docketIdInward = docketIdInward;
		this.docketIdOutward = docketIdOutward;
		this.replaceItemSerial = replaceItemSerial;
		this.ticketGeneratedBy = ticketGeneratedBy;
		this.assignByManager = assignByManager;
		this.repairingRemark = repairingRemark;
		this.itemDetailId = itemDetailId;
		this.region = region;
		this.commentList = commentList;
		this.fruId = fruId;
	}
	
	public ItemRepairingEntity() {
		super();
	}
	
	public String getPartNo() {
		return partNo;
	}
	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public void setTech_Assign_Date(LocalDate tech_Assign_Date) {
		this.tech_Assign_Date = tech_Assign_Date;
	}
	public InwardGatePassEntity getInwardGatepass() {
		return inwardGatepass;
	}
	public void setInwardGatepass(InwardGatePassEntity inwardGatepass) {
		this.inwardGatepass = inwardGatepass;
	}
	
	public OutwardGatepassEntity getOutwardGatePass() {
		return outwardGatePass;
	}

	public void setOutwardGatePass(OutwardGatepassEntity outwardGatePass) {
		this.outwardGatePass = outwardGatePass;
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
	public String getDocketIdOutward() {
		return docketIdOutward;
	}
	public void setDocketIdOutward(String docketIdOutward) {
		this.docketIdOutward = docketIdOutward;
	}
	public String getReplaceItemSerial() {
		return replaceItemSerial;
	}
	public void setReplaceItemSerial(String replaceItemSerial) {
		this.replaceItemSerial = replaceItemSerial;
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
	public String getRepairingRemark() {
		return repairingRemark;
	}
	public void setRepairingRemark(String repairingRemark) {
		this.repairingRemark = repairingRemark;
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
	public List<RepairingCommentsEntity> getCommentList() {
		return commentList;
	}
	public void setCommentList(List<RepairingCommentsEntity> commentList) {
		this.commentList = commentList;
	}
	public FruEntity getFruId() {
		return fruId;
	}
	public void setFruId(FruEntity fruId) {
		this.fruId = fruId;
	}
	
}
