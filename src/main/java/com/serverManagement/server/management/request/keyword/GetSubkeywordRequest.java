package com.serverManagement.server.management.request.keyword;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetSubkeywordRequest {

	private String keywordName;

	public GetSubkeywordRequest(String keywordName) {
		super();
		this.keywordName = keywordName;
	}

	public GetSubkeywordRequest() {
		super();
	}

	public String getKeywordName() {
		return keywordName;
	}

	public void setKeywordName(String keywordName) {
		this.keywordName = keywordName;
	}

	@Override
	public String toString() {
		return "GetSubkeywordRequest [keywordName=" + keywordName + "]";
	}
}
