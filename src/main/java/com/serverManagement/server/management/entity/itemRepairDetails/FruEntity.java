package com.serverManagement.server.management.entity.itemRepairDetails;

import java.time.LocalDate;

import org.hibernate.annotations.Fetch;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.serverManagement.server.management.entity.gatePass.InwardGatePassEntity;
import com.serverManagement.server.management.entity.gatePass.OutwardGatepassEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class FruEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(unique=true)
	private String rmaNo;
	private LocalDate createdDate;
	@OneToOne(mappedBy = "fruId", cascade = CascadeType.ALL)
	@JsonBackReference
	private ItemRepairingEntity repairingIdList;
	private LocalDate closingDate;
	@ManyToOne()
	@JoinColumn()
	private InwardGatePassEntity inGatepassID;
	@ManyToOne()
	@JoinColumn()
	private OutwardGatepassEntity outGatepassID;
	@ManyToOne()
	@JoinColumn()
	private RegionEntity regionDetails;
	
	

	public FruEntity(Long id, String rmaNo, LocalDate createdDate, ItemRepairingEntity repairingIdList,
			LocalDate closingDate, InwardGatePassEntity inGatepassID, OutwardGatepassEntity outGatepassID,
			RegionEntity regionDetails) {
		super();
		this.id = id;
		this.rmaNo = rmaNo;
		this.createdDate = createdDate;
		this.repairingIdList = repairingIdList;
		this.closingDate = closingDate;
		this.inGatepassID = inGatepassID;
		this.outGatepassID = outGatepassID;
		this.regionDetails = regionDetails;
	}
	public FruEntity() {
		super();
	}
	public LocalDate getClosingDate() {
		return closingDate;
	}
	public void setClosingDate(LocalDate closingDate) {
		this.closingDate = closingDate;
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
	public LocalDate getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}
	public InwardGatePassEntity getInGatepassID() {
		return inGatepassID;
	}
	public RegionEntity getRegionDetails() {
		return regionDetails;
	}
	public void setRegionDetails(RegionEntity regionDetails) {
		this.regionDetails = regionDetails;
	}
	public void setInGatepassID(InwardGatePassEntity inGatepassID) {
		this.inGatepassID = inGatepassID;
	}
	public OutwardGatepassEntity getOutGatepassID() {
		return outGatepassID;
	}
	public void setOutGatepassID(OutwardGatepassEntity outGatepassID) {
		this.outGatepassID = outGatepassID;
	}
	public ItemRepairingEntity getRepairingIdList() {
		return repairingIdList;
	}
	public void setRepairingIdList(ItemRepairingEntity repairingIdList) {
		this.repairingIdList = repairingIdList;
	}
	
}
