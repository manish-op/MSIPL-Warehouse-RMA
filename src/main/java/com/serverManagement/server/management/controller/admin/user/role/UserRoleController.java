package com.serverManagement.server.management.controller.admin.user.role;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.request.role.ChangeEmployeeRoleRequest;
import com.serverManagement.server.management.request.role.UpdateRole;
import com.serverManagement.server.management.service.admin.user.role.UserRoleService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/admin/role")
public class UserRoleController {
	
	private UserRoleService roleService;
		
	public UserRoleController(UserRoleService roleService) {
		super();
		this.roleService = roleService;
	}

//	@PostMapping
//	private ResponseEntity<String> addRole(HttpServletRequest request, @RequestParam(required=true)String roleName) {
//
//		roleName=roleName.toUpperCase();
//		return roleService.addRole(request, roleName);
//	}
	
	@GetMapping
	private ResponseEntity<?> getAllRoleList(HttpServletRequest request) {
		
		return roleService.getAllRoleList(request);
	}
	
//	@PutMapping
//	private ResponseEntity<?> updateRoleName(HttpServletRequest request, @RequestBody UpdateRole updateRole){
//
//		return roleService.updateRoleName(request, updateRole);
//	}
	
	@PutMapping("/change/employeeRole")
	private ResponseEntity<?> changeEmployeeRole(HttpServletRequest request, @RequestBody ChangeEmployeeRoleRequest changeEmployeeRole){
		
		return roleService.changeEmployeeRole(request, changeEmployeeRole);
	}
}

