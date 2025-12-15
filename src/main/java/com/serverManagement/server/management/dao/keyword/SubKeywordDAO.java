package com.serverManagement.server.management.dao.keyword;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serverManagement.server.management.entity.keyword.KeywordEntity;
import com.serverManagement.server.management.entity.keyword.SubKeywordEntity;

public interface SubKeywordDAO extends JpaRepository<SubKeywordEntity, Long> {

	@Query("SELECT CASE WHEN COUNT(um) > 0 THEN TRUE ELSE FALSE END FROM SubKeywordEntity um WHERE um.keywordRef = (:keywordEntity) AND LOWER(um.subKeyword)= (:subKeyword)")
	public boolean existsBySubKeyword(@Param("keywordEntity") KeywordEntity keywordEntity,
			@Param("subKeyword") String subKeyword);

	@Query("SELECT um FROM SubKeywordEntity um WHERE um.keywordRef = :keywordEntity AND LOWER(um.subKeyword)= LOWER(:subKeyword)")
	public SubKeywordEntity getSpecificSubKeyword(@Param("keywordEntity") KeywordEntity keywordEntity,
			@Param("subKeyword") String subKeyword);
}
