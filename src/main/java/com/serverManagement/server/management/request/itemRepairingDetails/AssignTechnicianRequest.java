package com.serverManagement.server.management.request.itemRepairingDetails;

public class AssignTechnicianRequest {

	private Long ticketId;
	private String technicianEmail;
	public AssignTechnicianRequest(Long ticketId, String technicianEmail) {
		super();
		this.ticketId = ticketId;
		this.technicianEmail = technicianEmail;
	}
	public AssignTechnicianRequest() {
		super();
	}
	public Long getTicketId() {
		return ticketId;
	}
	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}
	public String getTechnicianEmail() {
		return technicianEmail;
	}
	public void setTechnicianEmail(String technicianEmail) {
		this.technicianEmail = technicianEmail;
	}
	
	
}
