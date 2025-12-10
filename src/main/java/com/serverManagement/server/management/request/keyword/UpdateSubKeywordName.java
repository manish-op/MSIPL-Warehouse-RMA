package com.serverManagement.server.management.request.keyword;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateSubKeywordName {

	private String KeywordName;
	private String oldSubKeyword;
	private String UpdateSubKeyword;
	public UpdateSubKeywordName(String keywordName, String oldSubKeyword, String updateSubKeyword) {
		super();
		KeywordName = keywordName;
		this.oldSubKeyword = oldSubKeyword;
		UpdateSubKeyword = updateSubKeyword;
	}
	public UpdateSubKeywordName() {
		super();
	}
	public String getKeywordName() {
		return KeywordName;
	}
	public void setKeywordName(String keywordName) {
		KeywordName = keywordName;
	}
	public String getOldSubKeyword() {
		return oldSubKeyword;
	}
	public void setOldSubKeyword(String oldSubKeyword) {
		this.oldSubKeyword = oldSubKeyword;
	}
	public String getUpdateSubKeyword() {
		return UpdateSubKeyword;
	}
	public void setUpdateSubKeyword(String updateSubKeyword) {
		UpdateSubKeyword = updateSubKeyword;
	}
	@Override
	public String toString() {
		return "UpdateSubKeywordName [KeywordName=" + KeywordName + ", oldSubKeyword=" + oldSubKeyword
				+ ", UpdateSubKeyword=" + UpdateSubKeyword + "]";
	}
}
