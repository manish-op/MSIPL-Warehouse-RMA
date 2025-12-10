package com.serverManagement.server.management.request.itemRepairingDetails;


public class UpdateTicketStatus {

	
	private Long ticketId;	
	private String repairStatusName;	
	private String outwardGatePass;	
	private String warrantyDetailsOption;
	private String docketIdOutward;
	private String replaceWithItemSerialNo;	
	private String comment;
	public UpdateTicketStatus(Long ticketId, String repairStatusName, String outwardGatePass,
			String warrantyDetailsOption, String docketIdOutward, String replaceWithItemSerialNo, String comment) {
		super();
		this.ticketId = ticketId;
		this.repairStatusName = repairStatusName;
		this.outwardGatePass = outwardGatePass;
		this.warrantyDetailsOption = warrantyDetailsOption;
		this.docketIdOutward = docketIdOutward;
		this.replaceWithItemSerialNo = replaceWithItemSerialNo;
		this.comment = comment;
	}
	public UpdateTicketStatus() {
		super();
	}
	public Long getTicketId() {
		return ticketId;
	}
	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}
	public String getRepairStatusName() {
		return repairStatusName;
	}
	public void setRepairStatusName(String repairStatusName) {
		this.repairStatusName = repairStatusName;
	}
	public String getOutwardGatePass() {
		return outwardGatePass;
	}
	public void setOutwardGatePass(String outwardGatePass) {
		this.outwardGatePass = outwardGatePass;
	}
	public String getWarrantyDetailsOption() {
		return warrantyDetailsOption;
	}
	public void setWarrantyDetailsOption(String warrantyDetailsOption) {
		this.warrantyDetailsOption = warrantyDetailsOption;
	}
	public String getDocketIdOutward() {
		return docketIdOutward;
	}
	public void setDocketIdOutward(String docketIdOutward) {
		this.docketIdOutward = docketIdOutward;
	}
	public String getReplaceWithItemSerialNo() {
		return replaceWithItemSerialNo;
	}
	public void setReplaceWithItemSerialNo(String replaceWithItemSerialNo) {
		this.replaceWithItemSerialNo = replaceWithItemSerialNo;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
}
