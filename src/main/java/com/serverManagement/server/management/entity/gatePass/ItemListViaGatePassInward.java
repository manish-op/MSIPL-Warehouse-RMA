package com.serverManagement.server.management.entity.gatePass;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ItemListViaGatePassInward {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private InwardGatePassEntity inwardGatepass;
	private String rmaNo;
	private String serialNo;
	private String partNo;
	private String keywordName;
	private String subkeywordName;
	private String faultDescription;
	private String faultRemark;
	private String remark;

	
	
	public ItemListViaGatePassInward(Long id, InwardGatePassEntity inwardGatepass, String rmaNo, String serialNo,
			String partNo, String keywordName, String subkeywordName, String faultDescription, String faultRemark,
			String remark) {
		super();
		this.id = id;
		this.inwardGatepass = inwardGatepass;
		this.rmaNo = rmaNo;
		this.serialNo = serialNo;
		this.partNo = partNo;
		this.keywordName = keywordName;
		this.subkeywordName = subkeywordName;
		this.faultDescription = faultDescription;
		this.faultRemark = faultRemark;
		this.remark = remark;
	}
	
	public ItemListViaGatePassInward() {
		super();
	}
	
	public String getPartNo() {
		return partNo;
	}
	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public InwardGatePassEntity getInwardGatepass() {
		return inwardGatepass;
	}
	public void setInwardGatepass(InwardGatePassEntity inwardGatepass) {
		this.inwardGatepass = inwardGatepass;
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
	public String getKeywordName() {
		return keywordName;
	}
	public void setKeywordName(String keywordName) {
		this.keywordName = keywordName;
	}
	public String getSubkeywordName() {
		return subkeywordName;
	}
	public void setSubkeywordName(String subkeywordName) {
		this.subkeywordName = subkeywordName;
	}
	public String getFaultDescription() {
		return faultDescription;
	}
	public void setFaultDescription(String faultDescription) {
		this.faultDescription = faultDescription;
	}
	public String getFaultRemark() {
		return faultRemark;
	}
	public void setFaultRemark(String faultRemark) {
		this.faultRemark = faultRemark;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

	
}
