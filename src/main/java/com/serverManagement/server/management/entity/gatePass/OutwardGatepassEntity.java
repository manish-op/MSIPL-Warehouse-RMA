package com.serverManagement.server.management.entity.gatePass;

import java.time.LocalDate;
import java.util.List;

import com.serverManagement.server.management.entity.region.RegionEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Table(name="OutwardGatePass")
@Entity
public class OutwardGatepassEntity {

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
	@OneToMany(mappedBy="outwardGatepass", cascade = CascadeType.ALL)
	private List<ItemListViaGatePassOutwardEntity> itemList;
	public OutwardGatepassEntity(Long id, LocalDate createdDate, String partyName, String partyContact,
			String partyAddress, String createdBy, RegionEntity regionDetails,
			List<ItemListViaGatePassOutwardEntity> itemList) {
		super();
		this.id = id;
		this.createdDate = createdDate;
		this.partyName = partyName;
		this.partyContact = partyContact;
		this.partyAddress = partyAddress;
		this.createdBy = createdBy;
		this.regionDetails = regionDetails;
		this.itemList = itemList;
	}
	public OutwardGatepassEntity() {
		super();
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
	public List<ItemListViaGatePassOutwardEntity> getItemList() {
		return itemList;
	}
	public void setItemList(List<ItemListViaGatePassOutwardEntity> itemList) {
		this.itemList = itemList;
	}
		
}
