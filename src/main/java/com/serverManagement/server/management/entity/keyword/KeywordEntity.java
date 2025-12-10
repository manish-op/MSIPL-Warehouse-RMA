package com.serverManagement.server.management.entity.keyword;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Table(name="keyword-table")
@Entity
public class KeywordEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(unique=true, nullable=false)
	private String keywordName;
	@OneToMany(mappedBy="keywordRef", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<SubKeywordEntity> subKeyword;
	public KeywordEntity(Long id, String keywordName, List<SubKeywordEntity> subKeyword) {
		super();
		this.id = id;
		this.keywordName = keywordName;
		this.subKeyword = subKeyword;
	}
	public KeywordEntity() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getKeywordName() {
		return keywordName;
	}
	public void setKeywordName(String keywordName) {
		this.keywordName = keywordName;
	}
	public List<SubKeywordEntity> getSubKeyword() {
		return subKeyword;
	}
	public void setSubKeyword(List<SubKeywordEntity> subKeyword) {
		this.subKeyword = subKeyword;
	}
	@Override
	public String toString() {
		return "KeywordEntity [id=" + id + ", keywordName=" + keywordName + ", subKeyword=" + subKeyword + "]";
	}
	
}
