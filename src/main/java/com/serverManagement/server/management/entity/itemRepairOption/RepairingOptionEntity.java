package com.serverManagement.server.management.entity.itemRepairOption;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class RepairingOptionEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(unique=true, nullable=false)
	private String statusOption;
	
	public RepairingOptionEntity(Long id, String statusOption) {
		super();
		this.id = id;
		this.statusOption = statusOption;
	}
	public RepairingOptionEntity() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getStatusOption() {
		return statusOption;
	}
	public void setStatusOption(String statusOption) {
		this.statusOption = statusOption;
	}
	
}
