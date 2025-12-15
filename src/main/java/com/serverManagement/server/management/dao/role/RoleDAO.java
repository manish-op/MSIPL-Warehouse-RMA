package com.serverManagement.server.management.dao.role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serverManagement.server.management.entity.role.RoleEntity;

public interface RoleDAO extends JpaRepository<RoleEntity, Long> {

	@Query("select um from RoleEntity um where LOWER(um.roleName) in (:roleName)")
	public RoleEntity findByName(@Param("roleName") String roleName);

	@Query("select um.roleName from RoleEntity um")
	public List<String> findRoleList();

	public RoleEntity findByroleName(String roleName);
}
