package com.serverManagement.server.management.request.keyword;

public class SubKeywordRequest {

	private String subKeyword;

	public SubKeywordRequest(String subKeyword) {
		super();
		this.subKeyword = subKeyword;
	}

	public SubKeywordRequest() {
		super();
	}

	public String getSubKeyword() {
		return subKeyword;
	}

	public void setSubKeyword(String subKeyword) {
		this.subKeyword = subKeyword;
	}

	@Override
	public String toString() {
		return "SubKeywordRequest [subKeyword=" + subKeyword + "]";
	}
	
}
