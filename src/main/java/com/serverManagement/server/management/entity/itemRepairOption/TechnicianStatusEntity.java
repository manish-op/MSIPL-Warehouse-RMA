package com.serverManagement.server.management.entity.itemRepairOption;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TechnicianStatusEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(unique=true, nullable=false)
	private String technicianAssign;
	public TechnicianStatusEntity(Long id, String technicianAssign) {
		super();
		this.id = id;
		this.technicianAssign = technicianAssign;
	}
	public TechnicianStatusEntity() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTechnicianAssign() {
		return technicianAssign;
	}
	public void setTechnicianAssign(String technicianAssign) {
		this.technicianAssign = technicianAssign;
	}
	
}
