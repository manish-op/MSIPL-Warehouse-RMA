package com.serverManagement.server.management.request.itemRepairingDetails;

import java.time.LocalDate;

public class ManagerGetTicketRequest {
	
	private LocalDate startingDate;
	private LocalDate endDate;
	private String technicianStatus;
	private String repairStatus;
	private String region;
	public ManagerGetTicketRequest(LocalDate startingDate, LocalDate endDate, String technicianStatus,
			String repairStatus, String region) {
		super();
		this.startingDate = startingDate;
		this.endDate = endDate;
		this.technicianStatus = technicianStatus;
		this.repairStatus = repairStatus;
		this.region = region;
	}
	public ManagerGetTicketRequest() {
		super();
	}
	public LocalDate getStartingDate() {
		return startingDate;
	}
	public void setStartingDate(LocalDate startingDate) {
		this.startingDate = startingDate;
	}
	public LocalDate getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
	public String getTechnicianStatus() {
		return technicianStatus;
	}
	public void setTechnicianStatus(String technicianStatus) {
		this.technicianStatus = technicianStatus;
	}
	public String getRepairStatus() {
		return repairStatus;
	}
	public void setRepairStatus(String repairStatus) {
		this.repairStatus = repairStatus;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	
}
