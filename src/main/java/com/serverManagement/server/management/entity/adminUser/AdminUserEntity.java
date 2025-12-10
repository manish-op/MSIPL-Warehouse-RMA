package com.serverManagement.server.management.entity.adminUser;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serverManagement.server.management.entity.region.RegionEntity;
import com.serverManagement.server.management.entity.role.RoleEntity;

import jakarta.persistence.*;

@Table(name = "Login_Table")
@Entity
public class AdminUserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;
	private String name;
	private String email;
	private String mobileNo;
	@JsonIgnore
	private String password;
	@ManyToOne
	@JoinColumn(name = "role_id")
	@JsonBackReference
	private RoleEntity roleModel;
	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "region_id")
	private RegionEntity regionEntity;

	// Track user's last activity for online status
	private java.time.ZonedDateTime lastActiveAt;

	public AdminUserEntity(Long userId, String name, String email, String mobileNo, String password,
			RoleEntity roleModel, RegionEntity regionEntity) {
		super();
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.mobileNo = mobileNo;
		this.password = password;
		this.roleModel = roleModel;
		this.regionEntity = regionEntity;
	}

	public AdminUserEntity() {
		super();
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public RoleEntity getRoleModel() {
		return roleModel;
	}

	public void setRoleModel(RoleEntity roleModel) {
		this.roleModel = roleModel;
	}

	public RegionEntity getRegionEntity() {
		return regionEntity;
	}

	public void setRegionEntity(RegionEntity regionEntity) {
		this.regionEntity = regionEntity;
	}

	public java.time.ZonedDateTime getLastActiveAt() {
		return lastActiveAt;
	}

	public void setLastActiveAt(java.time.ZonedDateTime lastActiveAt) {
		this.lastActiveAt = lastActiveAt;
	}

	@Override
	public String toString() {
		return "AdminUserEntity [userId=" + userId + ", name=" + name + ", email=" + email + ", mobileNo=" + mobileNo
				+ ", password=" + password + ", roleModel=" + roleModel + ", regionEntity=" + regionEntity + "]";
	}

}
