package com.serverManagement.server.management.request.keyword;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateKeywordName {

	private String oldKeyword;
	private String newKeyword;
	public UpdateKeywordName(String oldKeyword, String newKeyword) {
		super();
		this.oldKeyword = oldKeyword;
		this.newKeyword = newKeyword;
	}
	public UpdateKeywordName() {
		super();
	}
	public String getOldKeyword() {
		return oldKeyword;
	}
	public void setOldKeyword(String oldKeyword) {
		this.oldKeyword = oldKeyword;
	}
	public String getNewKeyword() {
		return newKeyword;
	}
	public void setNewKeyword(String newKeyword) {
		this.newKeyword = newKeyword;
	}
	@Override
	public String toString() {
		return "UpdateKeywordName [oldKeyword=" + oldKeyword + ", newKeyword=" + newKeyword + "]";
	}
}
