package com.serverManagement.server.management.request.gatepass;

public class OutwardGatepassItemList {
	private String rmaNo;
	private String serialNo;
	private String remark;
	//private String partNo;
	private String docketOutward;
	private boolean fru;
	public OutwardGatepassItemList(String rmaNo, String serialNo, String remark, String docketOutward, boolean fru) {
		super();
		this.rmaNo = rmaNo;
		this.serialNo = serialNo;
		this.remark = remark;
		this.docketOutward = docketOutward;
		this.fru = fru;
	}
	public OutwardGatepassItemList() {
		super();
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
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getDocketOutward() {
		return docketOutward;
	}
	public void setDocketOutward(String docketOutward) {
		this.docketOutward = docketOutward;
	}
	public boolean isFru() {
		return fru;
	}
	public void setFru(boolean fru) {
		this.fru = fru;
	}
	
}
