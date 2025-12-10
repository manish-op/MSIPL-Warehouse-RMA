package com.serverManagement.server.management.response.keyword;

public class SubKeywordResponse {

	private String subKeyword;

	public SubKeywordResponse(String subKeyword) {
		super();
		this.subKeyword = subKeyword;
	}

	public SubKeywordResponse() {
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
		return "SubKeywordResponse [subKeyword=" + subKeyword + "]";
	}
	
}
