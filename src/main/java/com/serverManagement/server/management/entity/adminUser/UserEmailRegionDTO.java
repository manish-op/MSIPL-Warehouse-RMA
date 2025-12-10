package com.serverManagement.server.management.entity.adminUser;

import com.serverManagement.server.management.entity.region.RegionEntity;
import com.serverManagement.server.management.entity.role.RoleEntity;

public class UserEmailRegionDTO {

	private String techEmail;
	private RegionEntity regionEntityId;
	private RoleEntity roleModelId;
	public UserEmailRegionDTO(String techEmail, RegionEntity regionEntityId, RoleEntity roleModelId) {
		super();
		this.techEmail = techEmail;
		this.regionEntityId = regionEntityId;
		this.roleModelId = roleModelId;
	}
	public String getTechEmail() {
		return techEmail;
	}
	public void setTechEmail(String techEmail) {
		this.techEmail = techEmail;
	}
	public RegionEntity getRegionEntityId() {
		return regionEntityId;
	}
	public void setRegionEntityId(RegionEntity regionEntityId) {
		this.regionEntityId = regionEntityId;
	}
	public RoleEntity getRoleModelId() {
		return roleModelId;
	}
	public void setRoleModelId(RoleEntity roleModelId) {
		this.roleModelId = roleModelId;
	}
	
}
