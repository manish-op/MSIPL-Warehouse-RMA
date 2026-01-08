package com.serverManagement.server.management.dao.admin.user;

import java.util.List;

//import java.util.List;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.adminUser.UserEmailRegionDTO;
import com.serverManagement.server.management.entity.region.RegionEntity;
import com.serverManagement.server.management.entity.role.RoleEntity;

public interface AdminUserDAO extends JpaRepository<AdminUserEntity, Long> {

    @Query("select um from AdminUserEntity um where LOWER(um.email) in (:email)")
    public AdminUserEntity findByEmail(@Param("email") String email);

    @Query("select um.email from AdminUserEntity um where um.regionEntity = (:userId) AND um.roleModel in (:roleEntity)")
    public List<String> getEmployeeList(@Param("userId") RegionEntity userId,
            @Param("roleEntity") List<RoleEntity> roleEntity);

    @Query("select new com.serverManagement.server.management.entity.adminUser.UserEmailRegionDTO(um.email, um.regionEntity, um.roleModel) from AdminUserEntity um where LOWER(um.email) in (:email)")
    public UserEmailRegionDTO confirmUserEmail(@Param("email") String email);

    // In com/serverManagement/server/management/dao/admin/user/AdminUserDAO.java

    List<AdminUserEntity> findByRegionEntityCity(String city);

    List<AdminUserEntity> findAllByRegionEntity(RegionEntity regionEntity);

    // --- FIX 3 ---
    @Query("SELECT u FROM AdminUserEntity u WHERE LOWER(u.roleModel.roleName) = 'admin'")
    List<AdminUserEntity> findAllAdmins();

    // Count online users (active within last 5 minutes)
    @Query("SELECT COUNT(u) FROM AdminUserEntity u WHERE u.lastActiveAt IS NOT NULL AND u.lastActiveAt > :cutoffTime")
    Long countOnlineUsers(@Param("cutoffTime") java.time.ZonedDateTime cutoffTime);

    // Batch fetch users by email list (case-insensitive handling recommended in
    // service layer or query)
    @Query("SELECT u FROM AdminUserEntity u WHERE LOWER(u.email) IN :emails")
    List<AdminUserEntity> findByEmailIn(@Param("emails") List<String> emails);
}