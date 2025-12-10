package com.serverManagement.server.management.entity.gatePass;

import java.time.LocalDate;
import java.util.List;

import com.serverManagement.server.management.entity.itemRepairDetails.FruEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class InwardGatePassEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private LocalDate createdDate;
	private String partyName;
	private String partyContact;
	private String partyAddress;
	private String createdBy;
	@ManyToOne()
	private RegionEntity regionDetails;
	
	@OneToMany(mappedBy="inwardGatepass", cascade = CascadeType.ALL)
	private List<ItemListViaGatePassInward> itemList;
	@OneToMany( mappedBy= "inGatepassID", cascade = CascadeType.ALL)
	private List<FruEntity> fruList;


	public InwardGatePassEntity(Long id, LocalDate createdDate, String partyName, String partyContact,
			String partyAddress, String createdBy, RegionEntity regionDetails, List<ItemListViaGatePassInward> itemList,
			List<FruEntity> fruList) {
		super();
		this.id = id;
		this.createdDate = createdDate;
		this.partyName = partyName;
		this.partyContact = partyContact;
		this.partyAddress = partyAddress;
		this.createdBy = createdBy;
		this.regionDetails = regionDetails;
		this.itemList = itemList;
		this.fruList = fruList;
	}

	public InwardGatePassEntity() {
		super();
	}
	
	public List<FruEntity> getFruList() {
		return fruList;
	}

	public void setFruList(List<FruEntity> fruList) {
		this.fruList = fruList;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}

	public String getPartyName() {
		return partyName;
	}

	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}

	public String getPartyContact() {
		return partyContact;
	}

	public void setPartyContact(String partyContact) {
		this.partyContact = partyContact;
	}

	public String getPartyAddress() {
		return partyAddress;
	}

	public void setPartyAddress(String partyAddress) {
		this.partyAddress = partyAddress;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public RegionEntity getRegionDetails() {
		return regionDetails;
	}

	public void setRegionDetails(RegionEntity regionDetails) {
		this.regionDetails = regionDetails;
	}

	public List<ItemListViaGatePassInward> getItemList() {
		return itemList;
	}

	public void setItemList(List<ItemListViaGatePassInward> itemList) {
		this.itemList = itemList;
	}
	
	
}
