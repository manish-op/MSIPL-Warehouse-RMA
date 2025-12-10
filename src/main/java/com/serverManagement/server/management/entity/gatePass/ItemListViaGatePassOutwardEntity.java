package com.serverManagement.server.management.entity.gatePass;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Table(name="OutwardGatepassItemList")
@Entity
public class ItemListViaGatePassOutwardEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String rmaNo;
	private String serialNo;
	private String partNo;
	private String keywordName;
	private String subkeywordName;
	private String remark;
	@ManyToOne()
	private OutwardGatepassEntity outwardGatepass;
	public ItemListViaGatePassOutwardEntity(Long id, String rmaNo, String serialNo, String partNo, String keywordName,
			String subkeywordName, String remark, OutwardGatepassEntity outwardGatepass) {
		super();
		this.id = id;
		this.rmaNo = rmaNo;
		this.serialNo = serialNo;
		this.partNo = partNo;
		this.keywordName = keywordName;
		this.subkeywordName = subkeywordName;
		this.remark = remark;
		this.outwardGatepass = outwardGatepass;
	}
	public ItemListViaGatePassOutwardEntity() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getPartNo() {
		return partNo;
	}
	public void setPartNo(String partNo) {
		this.partNo = partNo;
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
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public OutwardGatepassEntity getOutwardGatepass() {
		return outwardGatepass;
	}
	public void setOutwardGatepass(OutwardGatepassEntity outwardGatepass) {
		this.outwardGatepass = outwardGatepass;
	}
		
}
