package com.serverManagement.server.management.controller.admin.user;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dto.itemRepairDetails.DashboardSummaryDto;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.service.itemDetails.ItemDetailsService;
import io.jsonwebtoken.Jwt;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.serverManagement.server.management.request.changePassword.ChangeEmployeePasswordRequest;
import com.serverManagement.server.management.request.changePassword.PasswordRequest;
import com.serverManagement.server.management.request.login.LoginRequest;
import com.serverManagement.server.management.request.user.AddUserRequest;
import com.serverManagement.server.management.service.admin.user.AdminUserService;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

	private AdminUserService service;
    @Autowired
    private AdminUserDAO adminUserDAO;

    @Autowired
    private ItemDetailsService itemDetailsService;


    public AdminUserController(AdminUserService service) {
		super();
		this.service = service;
	}

	@PostMapping("/addUser")
	public ResponseEntity<?> createUser(HttpServletRequest request, @RequestBody AddUserRequest addUser) throws Exception {
		return service.createAdminUser(request, addUser);
	}

//	@GetMapping
//	public ResponseEntity<?> getUserList(HttpServletRequest requestServlet, @RequestParam int pageNo,
//			@RequestParam int noOfRecord, @RequestParam(required = false) String searchKey,
//			@RequestParam(required = false) String role) throws Exception {
//		return service.getUserList(requestServlet, pageNo, noOfRecord, searchKey, role);
//	}

	@PostMapping("/login")
	public ResponseEntity<?> adminLogin(@RequestBody LoginRequest request) throws Exception {
		return service.adminLogin(request);
	}

	@PutMapping("/change/password")
	public ResponseEntity<?> changePassword(HttpServletRequest requestServlet, @RequestBody PasswordRequest requestBody)
			throws Exception {
		return service.changePassword(requestServlet, requestBody);
	}
	
	@PutMapping("/change/password/admin")
	public ResponseEntity<?> changeUserPassword(HttpServletRequest requestServlet, @RequestBody ChangeEmployeePasswordRequest passRequestBody)
			throws Exception {
		return service.changeUserPassword(requestServlet, passRequestBody);
	}

	@PostMapping("/delete")
	@Hidden
	public ResponseEntity<String> deleteUser(HttpServletRequest request, @RequestParam(required = true) String email) throws Exception {

		return service.deleteUser(request, email);
	}

    @GetMapping("/dashboard-summary")
    public ResponseEntity<?> getDashboardSummary(HttpServletRequest request) {
        try {
            // 1) Try standard approach used throughout your app:
            String loggedInUserName = null;
            try {
                if (request.getUserPrincipal() != null) {
                    loggedInUserName = request.getUserPrincipal().getName();
                }
            } catch (Exception ignored) {
            }

            // 2) If principal not present, try Authorization header fallback (AddItemAPI style)
            if (loggedInUserName == null || loggedInUserName.trim().isEmpty()) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7).trim();
                    if (!token.isEmpty()) {
                        // NOTE: adminUserDAO.findByAuthToken(token) must be implemented (see DAO step)
                        AdminUserEntity userByToken = (AdminUserEntity) adminUserDAO.findByRegionEntityCity(token);
                        if (userByToken != null && userByToken.getEmail() != null) {
                            loggedInUserName = userByToken.getEmail();
                        }
                    }
                }
            }

            // 3) If still not resolved -> unauthorized
            if (loggedInUserName == null || loggedInUserName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
            }

            // 4) Load user entity from DB (your usual pattern)
            AdminUserEntity loggedUser = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
            if (loggedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
            }

            // 5) Determine role and region (same as other endpoints)
            String role = (loggedUser.getRoleModel() != null && loggedUser.getRoleModel().getRoleName() != null)
                    ? loggedUser.getRoleModel().getRoleName().trim().toLowerCase()
                    : "";

            boolean isAdmin = "admin".equalsIgnoreCase(role);

            String regionName = null;
            if (!isAdmin) {
                if (loggedUser.getRegionEntity() == null || loggedUser.getRegionEntity().getCity() == null) {
                    // Non-admin but no region assigned -> follow same pattern you use elsewhere
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No region assigned to your account");
                }
                regionName = loggedUser.getRegionEntity().getCity().toLowerCase();
            }

            // 6) Delegate to existing service method you already added in ItemDetailsService
            DashboardSummaryDto summary = itemDetailsService.getDashboardSummary(isAdmin, regionName);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }

    }

}
