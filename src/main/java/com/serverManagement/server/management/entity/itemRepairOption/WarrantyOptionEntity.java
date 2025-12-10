package com.serverManagement.server.management.entity.itemRepairOption;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class WarrantyOptionEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(unique=true, nullable=false)
	private String warrantyOption;
	public WarrantyOptionEntity(Long id, String warrantyOption) {
		super();
		this.id = id;
		this.warrantyOption = warrantyOption;
	}
	public WarrantyOptionEntity() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getWarrantyOption() {
		return warrantyOption;
	}
	public void setWarrantyOption(String warrantyOption) {
		this.warrantyOption = warrantyOption;
	}
	

}
