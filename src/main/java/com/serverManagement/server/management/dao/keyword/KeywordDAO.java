package com.serverManagement.server.management.dao.keyword;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.serverManagement.server.management.entity.keyword.KeywordEntity;

public interface KeywordDAO extends JpaRepository<KeywordEntity, Long>{

	@Query("SELECT um.keywordName FROM KeywordEntity um")
	public List<String> getKeywordList();
	
	@Query("SELECT CASE WHEN COUNT(um) > 0 THEN TRUE ELSE FALSE END FROM KeywordEntity um WHERE LOWER(um.keywordName) = :keyword")
	public boolean existsByKeyword(String keyword);
	
	@Query("SELECT um FROM KeywordEntity um WHERE LOWER(um.keywordName) = :keyword")
	public KeywordEntity getByKeyword(String keyword);
	
	@Query("SELECT um FROM KeywordEntity um WHERE LOWER(um.keywordName) = :keyword")
	public KeywordEntity getSubKeywordList(String keyword);
	
//	@Query("SELECT CASE WHEN COUNT(um) > 0 THEN TRUE ELSE FALSE END FROM KeywordEntity um WHERE LOWER(um.keywordName) = :keyword AND LOWER(um.subKeyword.subKeyword)= :subKeyword")
//	public boolean existsBySubKeyword(String keyword, String subKeyword);
}
