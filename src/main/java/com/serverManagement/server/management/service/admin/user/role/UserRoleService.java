package com.serverManagement.server.management.service.admin.user.role;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.role.RoleDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.role.RoleEntity;
import com.serverManagement.server.management.request.role.ChangeEmployeeRoleRequest;
import com.serverManagement.server.management.request.role.UpdateRole;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserRoleService {

    private AdminUserDAO adminUserDAO;
    private RoleDAO roleDAO;

    public UserRoleService(AdminUserDAO adminUserDAO, RoleDAO roleDAO) {
        super();
        this.adminUserDAO = adminUserDAO;
        this.roleDAO = roleDAO;
    }

    private AdminUserEntity adminUserModel;

    // Add Roles only admin can add role
//	public ResponseEntity<String> addRole(HttpServletRequest request, String roleName) {
//
//		String loggedInEmail = null;
//		String message = "";
//		try {
//			loggedInEmail = request.getUserPrincipal().getName();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
//		}
//
//		try {
//			adminUserModel = adminUserDAO.findByEmail(loggedInEmail);
//			if (adminUserModel == null) {
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not logged in");
//			}
//			String role = adminUserModel.getRoleModel().getRoleName().toLowerCase();
//			if (role.equals("admin")) {
//
//				RoleEntity roleModel = roleDAO.findByName(roleName.toLowerCase());
//				if (roleModel == null) {
//					RoleEntity addRole = new RoleEntity();
//					addRole.setRoleName(roleName.toUpperCase());
//
//					addRole = roleDAO.save(addRole);
//
//					if (addRole != null) {
//						message = "Role added successfully";
//						return ResponseEntity.status(HttpStatus.CREATED).body(message);
//					} else {
//						message = "Role not added, something went wrong";
//						return ResponseEntity.status(HttpStatus.CREATED).body(message);
//					}
//				} else {
//					return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("Duplicate Role Name");
//				}
//			} else {
//
//				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only Admin can add Role");
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
//
//		}
//	}

    // Admin or HR Get all Role name list
    public ResponseEntity<?> getAllRoleList(HttpServletRequest request) {
        String loggedInEmail = null;
        try {
            loggedInEmail = request.getUserPrincipal().getName();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        try {
            adminUserModel = adminUserDAO.findByEmail(loggedInEmail);
            if (adminUserModel == null) {
                throw new Exception("User not logged in");
            }
            String role = adminUserModel.getRoleModel().getRoleName().toUpperCase();
            if (role.equals("ADMIN")) {
                List<String> list = roleDAO.findRoleList();
                if (list == null || list.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("not have any role ");
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(list);
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(List.of("Only Admin or HR can access this section"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of("Something went wrong"));

        }
    }

    // Update Role Name only admin can change
//	public ResponseEntity<?> updateRoleName(HttpServletRequest request, UpdateRole updateRole) {
//
//		String loggedInEmail = null;
//		String message = "";
//		try {
//			loggedInEmail = request.getUserPrincipal().getName();
//		} catch (Exception e) {
//			e.printStackTrace();
//			message = "User not authenticated";
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
//		}
//		ResponseEntity<String> response = null;
//		try {
//			adminUserModel = adminUserDAO.findByEmail(loggedInEmail);
//			if (adminUserModel == null) {
//				message = "User not logged in";
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
//			}
//			String role = adminUserModel.getRoleModel().getRoleName().toUpperCase();
//			if (role.equals("ADMIN")) {
//
//				String roleName = updateRole.getOldRoleName().toLowerCase();
//				if (roleName.equals("admin")) {
//					message = "ADMIN role can not Update or Change their name";
//					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
//				}
//				RoleEntity roleModel = roleDAO.findByName(roleName);
//				if (roleModel != null) {
//					roleModel.setRoleName(updateRole.getNewRoleName().toUpperCase());
//					roleModel = roleDAO.save(roleModel);
//					if (roleModel != null) {
//						message = "Role " + roleName + " successfully update with "
//								+ updateRole.getNewRoleName().toUpperCase();
//						response = ResponseEntity.status(HttpStatus.CREATED).body(message);
//					} else {
//						message = "Not Update something went wrong";
//						response = ResponseEntity.status(HttpStatus.CREATED).body(message);
//					}
//				} else {
//					message = "Not have any Role with this name: " + roleName;
//					response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
//				}
//			} else {
//				message = "Only Admin can add Role";
//				response = ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			message = "something went wrong";
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
//
//		}
//		return response;
//	}

    // change employee role by admin only
    // change employee role by admin only
    public ResponseEntity<?> changeEmployeeRole(HttpServletRequest request,
                                                ChangeEmployeeRoleRequest changeEmployeeRole) {

        String loggedInEmail = null;
        String message = "";
        try {
            loggedInEmail = request.getUserPrincipal().getName();
        } catch (Exception e) {
            e.printStackTrace();
            message = "User not authenticated";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
        }

        try {
            adminUserModel = adminUserDAO.findByEmail(loggedInEmail);
            if (adminUserModel == null) {
                message = "User not logged in";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
            }
            String role = adminUserModel.getRoleModel().getRoleName().toLowerCase();

            // SECURITY CHECK 1: Only an admin can perform this.
            if (role.equals("admin")) {
                if (changeEmployeeRole == null || changeEmployeeRole.getEmpEmail() == null
                        || changeEmployeeRole.getRole() == null) {
                    message = "All fields Required";
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
                }
                AdminUserEntity employeeDetails = adminUserDAO
                        .findByEmail(changeEmployeeRole.getEmpEmail().toLowerCase());
                if (employeeDetails == null) {
                    message = "No Employee register with this email";
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);

                    // SECURITY CHECK 2: Prevents changing another admin's role.
                } else if (employeeDetails.getRoleModel().getRoleName().toLowerCase().equals("admin")) {
                    message = "Admin Role Can not be changed";
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);

                    // SECURITY CHECK 3: Prevents an admin from changing their own role.
                } else if (loggedInEmail.toLowerCase().equals(employeeDetails.getEmail().toLowerCase())) {
                    message = "You can not change our own Role";
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
                } else {
                    String assignRole = changeEmployeeRole.getRole().toLowerCase();

                    // --- THIS BLOCK WAS REMOVED ---
                    // if (assignRole.equals("admin")) {
                    //     message = "Can not create admin by this portal";
                    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
                    // }
                    // --- END OF REMOVED BLOCK ---

                    RoleEntity roleModel = roleDAO.findByName(assignRole);
                    if (roleModel == null) {
                        message = "No Role listed with this role name";
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
                    } else if (employeeDetails.getRoleModel().equals(roleModel)) {
                        message = "employee previous role is same";
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
                    } else {

                        employeeDetails.setRoleModel(roleModel);
                        employeeDetails = adminUserDAO.save(employeeDetails);
                        if (employeeDetails != null) {
                            message = "Role changed successfully";
                            return ResponseEntity.status(HttpStatus.OK).body(message);
                        } else {
                            message = "Role not changed, Something went wrong";
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
                        }
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only Admin can add Role");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());

        }
    }

}
