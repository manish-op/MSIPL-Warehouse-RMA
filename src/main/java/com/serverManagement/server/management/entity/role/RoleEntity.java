package com.serverManagement.server.management.entity.role;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Table(name="UserRole")
@Entity
public class RoleEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String roleName;
	
	@OneToMany(mappedBy="roleModel")
    @JsonManagedReference
	private List<AdminUserEntity> adminUserModel;

	public RoleEntity(Long id, String roleName, List<AdminUserEntity> adminUserModel) {
		super();
		this.id = id;
		this.roleName = roleName;
		this.adminUserModel = adminUserModel;
	}

	public RoleEntity() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public List<AdminUserEntity> getAdminUserModel() {
		return adminUserModel;
	}

	public void setAdminUserModel(List<AdminUserEntity> adminUserModel) {
		this.adminUserModel = adminUserModel;
	}

	@Override
	public String toString() {
		return "RoleEntity [id=" + id + ", roleName=" + roleName + ", adminUserModel=" + adminUserModel + "]";
	}
	
}
