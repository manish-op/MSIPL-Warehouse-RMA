package com.serverManagement.server.management.request.itemDetails;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetItemByKeywordRequest {
	
	private String region;
	private String keyword;
	private String subKeyword;
	private String systemName;
	private String itemAvailability;
	private String itemStatus;
	private String partNo;
	public GetItemByKeywordRequest(String region, String keyword, String subKeyword, String systemName,
			String itemAvailability, String itemStatus, String partNo) {
		super();
		this.region = region;
		this.keyword = keyword;
		this.subKeyword = subKeyword;
		this.systemName = systemName;
		this.itemAvailability = itemAvailability;
		this.itemStatus = itemStatus;
		this.partNo = partNo;
	}
	public GetItemByKeywordRequest() {
		super();
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getSubKeyword() {
		return subKeyword;
	}
	public void setSubKeyword(String subKeyword) {
		this.subKeyword = subKeyword;
	}
	public String getSystemName() {
		return systemName;
	}
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}
	public String getItemAvailability() {
		return itemAvailability;
	}
	public void setItemAvailability(String itemAvailability) {
		this.itemAvailability = itemAvailability;
	}
	public String getItemStatus() {
		return itemStatus;
	}
	public void setItemStatus(String itemStatus) {
		this.itemStatus = itemStatus;
	}
	public String getPartNo() {
		return partNo;
	}
	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}
	
}
