package com.serverManagement.server.management.request.keyword;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeywordRequest {

	private String keyword;
	private List<SubKeywordRequest> subKeywordList;
	public KeywordRequest(String keyword, List<SubKeywordRequest> subKeywordList) {
		super();
		this.keyword = keyword;
		this.subKeywordList = subKeywordList;
	}
	public KeywordRequest() {
		super();
	}
	public String getKeyword() {
		return keyword;
		
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public List<SubKeywordRequest> getSubKeywordList() {
		return subKeywordList;
	}
	public void setSubKeywordList(List<SubKeywordRequest> subKeywordList) {
		this.subKeywordList = subKeywordList;
	}
	@Override
	public String toString() {
		return "KeywordRequest [keyword=" + keyword + ", subKeywordList=" + subKeywordList + "]";
	}
	
}
