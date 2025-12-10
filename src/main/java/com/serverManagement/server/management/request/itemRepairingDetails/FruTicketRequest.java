package com.serverManagement.server.management.request.itemRepairingDetails;


public class FruTicketRequest {
	
	private String rmaNo;
	private ItemRepairingRequest itemDetail;
	public FruTicketRequest(String rmaNo, ItemRepairingRequest itemDetail) {
		super();
		this.rmaNo = rmaNo;
		this.itemDetail = itemDetail;
	}
	public FruTicketRequest() {
		super();
	}
	public String getRmaNo() {
		return rmaNo;
	}
	public void setRmaNo(String rmaNo) {
		this.rmaNo = rmaNo;
	}
	public ItemRepairingRequest getItemDetail() {
		return itemDetail;
	}
	public void setItemDetail(ItemRepairingRequest itemDetail) {
		this.itemDetail = itemDetail;
	}
	
}
