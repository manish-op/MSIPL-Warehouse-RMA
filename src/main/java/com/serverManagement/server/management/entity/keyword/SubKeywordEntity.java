package com.serverManagement.server.management.entity.keyword;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Table(name="sub-keyword-table")
@Entity
public class SubKeywordEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(nullable=false)
	private String subKeyword;
	@ManyToOne
	@JsonBackReference
	private KeywordEntity keywordRef;
	public SubKeywordEntity(Long id, String subKeyword, KeywordEntity keywordRef) {
		super();
		this.id = id;
		this.subKeyword = subKeyword;
		this.keywordRef = keywordRef;
	}
	public SubKeywordEntity() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSubKeyword() {
		return subKeyword;
	}
	public void setSubKeyword(String subKeyword) {
		this.subKeyword = subKeyword;
	}
	public KeywordEntity getKeywordRef() {
		return keywordRef;
	}
	public void setKeywordRef(KeywordEntity keywordRef) {
		this.keywordRef = keywordRef;
	}
	@Override
	public String toString() {
		return "SubKeywordEntity [id=" + id + ", subKeyword=" + subKeyword + ", keywordRef=" + keywordRef + "]";
	}
	
}