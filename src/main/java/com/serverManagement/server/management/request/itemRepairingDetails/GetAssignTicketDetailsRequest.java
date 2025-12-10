package com.serverManagement.server.management.request.itemRepairingDetails;

import java.time.LocalDate;

public class GetAssignTicketDetailsRequest {

	private LocalDate startingDate;
	private LocalDate endDate;
	private String repairStatus;
	private String ticketId;
	private String rmaNo;
	private String serialNo;
	
	public GetAssignTicketDetailsRequest(LocalDate startingDate, LocalDate endDate, String repairStatus,
			String ticketId, String rmaNo, String serialNo) {
		super();
		this.startingDate = startingDate;
		this.endDate = endDate;
		this.repairStatus = repairStatus;
		this.ticketId = ticketId;
		this.rmaNo = rmaNo;
		this.serialNo = serialNo;
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
	public String getRepairStatus() {
		return repairStatus;
	}
	public String getTicketId() {
		return ticketId;
	}
	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
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
	public void setRepairStatus(String repairStatus) {
		this.repairStatus = repairStatus;
	}
	
}
