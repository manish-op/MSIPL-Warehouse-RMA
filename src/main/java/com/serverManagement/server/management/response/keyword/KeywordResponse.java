package com.serverManagement.server.management.response.keyword;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeywordResponse {

	private String keyword;
	private List<SubKeywordResponse> subKeywordList;
	public KeywordResponse(String keyword, List<SubKeywordResponse> subKeywordList) {
		super();
		this.keyword = keyword;
		this.subKeywordList = subKeywordList;
	}
	public KeywordResponse() {
		super();
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public List<SubKeywordResponse> getSubKeywordList() {
		return subKeywordList;
	}
	public void setSubKeywordList(List<SubKeywordResponse> subKeywordList) {
		this.subKeywordList = subKeywordList;
	}
	@Override
	public String toString() {
		return "KeywordResponse [keyword=" + keyword + ", subKeywordList=" + subKeywordList + "]";
	}
	
}
