package com.serverManagement.server.management.request.itemRepairingDetails;

public class EngineerUpdateTicketStatusRequest {

	private Long id;
	private String ticketStatus;
	public EngineerUpdateTicketStatusRequest(Long id, String ticketStatus) {
		super();
		this.id = id;
		this.ticketStatus = ticketStatus;
	}
	public EngineerUpdateTicketStatusRequest() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTicketStatus() {
		return ticketStatus;
	}
	public void setTicketStatus(String ticketStatus) {
		this.ticketStatus = ticketStatus;
	}
	
}
