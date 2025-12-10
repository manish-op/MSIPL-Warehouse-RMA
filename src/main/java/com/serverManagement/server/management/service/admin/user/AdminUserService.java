package com.serverManagement.server.management.service.admin.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.region.RegionDAO;
import com.serverManagement.server.management.dao.role.RoleDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;
import com.serverManagement.server.management.entity.role.RoleEntity;
import com.serverManagement.server.management.globalException.DuplicateValueException;
import com.serverManagement.server.management.globalException.NullValueException;
import com.serverManagement.server.management.request.changePassword.ChangeEmployeePasswordRequest;
import com.serverManagement.server.management.request.changePassword.PasswordRequest;
import com.serverManagement.server.management.request.login.LoginRequest;
import com.serverManagement.server.management.request.user.AddUserRequest;
import com.serverManagement.server.management.response.login.LoginResponse;
import com.serverManagement.server.management.service.admin.login.AdminUserLogin;
import com.serverManagement.server.management.shared.utils.CommonUtils;
import com.serverManagement.server.management.utils.jwt.JWTUtils;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AdminUserService {

	private JWTUtils jwtUtil;
	private AdminUserDAO adminUserDAO;
	private RoleDAO roleDAO;
	private AdminUserLogin loginService;
	private PasswordEncoder passwordEncoder;
	private AuthenticationManager authenticationManager;
	private RegionDAO regionDAO;
	private AdminUserEntity adminUserEntity;

	// private JavaMailSender emailSender;
	public AdminUserService(JWTUtils jwtUtil, AdminUserDAO adminUserDAO, RoleDAO roleDAO, AdminUserLogin loginService,
			PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, RegionDAO regionDAO) {
		super();
		this.jwtUtil = jwtUtil;
		this.adminUserDAO = adminUserDAO;
		this.roleDAO = roleDAO;
		this.loginService = loginService;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.regionDAO = regionDAO;
	}

//	@Value("${contact_us_email}")
//	private String sendMailId;

	public ResponseEntity<?> createAdminUser(HttpServletRequest request, AddUserRequest requestUser) throws Exception {

		if (requestUser == null) {
			throw new Exception("Request Body Must not be Empty");
		}
		if (requestUser.getEmail() == null || requestUser.getEmail().isEmpty()) {

			throw new Exception("Email id must Required");
		}
		String loggedInEmail = null;
		try {
			loggedInEmail = request.getUserPrincipal().getName();
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
		}
		try {
			adminUserEntity = adminUserDAO.findByEmail(loggedInEmail);
			if (adminUserEntity == null) {
				throw new Exception("User not Logged In");
			} else {
				String userRole = adminUserEntity.getRoleModel().getRoleName();

				if (userRole.toLowerCase().equals("admin") || userRole.toLowerCase().equals("manager")) {
					requestUser.setEmail(requestUser.getEmail().toLowerCase());
					validateBasicInfo(requestUser);

					String message = "";

					String password = requestUser.getPassword();
					if (password != null && password.length() > 0) {
						password = passwordEncoder.encode(password);
					} else {
//				password = generateRandomPassword();
//				String mailBody = "Your account is created and password is - " + password;
// 				sendMail("Password", requestUser.getEmail(), mailBody);
//				password = passwordEncoder.encode(password);
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Password not provided, it's required");
					}

					requestUser.setPassword(password);
					AdminUserEntity addUserModel = new AdminUserEntity();
					RoleEntity roleModel = roleDAO.findByName("employee");
					if (roleModel != null) {
						addUserModel.setRoleModel(roleModel);
					} else {
						RoleEntity role = new RoleEntity();
						role.setRoleName("EMPLOYEE");
						role = roleDAO.save(role);
						if (role == null) {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									.body("Role cannot create something went wrong. Please try again later");
						} else {
							addUserModel.setRoleModel(role);
						}
					}
					addUserModel.setPassword(password);// set encrypted password

					if (requestUser.getName() == null || requestUser.getName().isEmpty()) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Employee Name is required. Provide Employee name and then try again");
					} else {
						addUserModel.setName(requestUser.getName());
					}
					if (requestUser.getEmail() == null || requestUser.getEmail().isEmpty()) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
								"Email id is missing, It's compulsory for creating account. Please provide employee Email id");
					} else {
						addUserModel.setEmail(requestUser.getEmail().toLowerCase());
					}
					if (requestUser.getMobileNo() == null || requestUser.getMobileNo().isEmpty()) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
								"Phone Nu is missing, It's compulsory for creating account. Please provide employee Phone No");
					} else {
						addUserModel.setMobileNo(requestUser.getMobileNo());
					}
					if (requestUser.getRegionName() == null || requestUser.getRegionName().isEmpty()) {
						addUserModel.setRegionEntity(adminUserEntity.getRegionEntity());
					} else {
						if (adminUserEntity.getRoleModel().getRoleName().toLowerCase().equals("admin")) {
							if (requestUser.getRegionName() != null) {
								RegionEntity region = regionDAO.findByCity(requestUser.getRegionName().toLowerCase());
								if (region == null) {
									return ResponseEntity.status(HttpStatus.BAD_REQUEST)
											.body("This region is not added in our data base, First add "
													+ requestUser.getRegionName().toUpperCase()
													+ " region to DataBase");
								} else {
									addUserModel.setRegionEntity(region);
								}
							}
						} else {
							if (adminUserEntity.getRegionEntity() != null) {
								addUserModel.setRegionEntity(adminUserEntity.getRegionEntity());
							} else {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("your region is not assign, Contact to admin to assign region to you");
							}
						}
					}

					addUserModel = adminUserDAO.save(addUserModel);
					if (addUserModel != null) {
						message = "User Created Successfully";
						return ResponseEntity.status(HttpStatus.CREATED).body(message);
					} else {
						message = "Unable to Create User";
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
					}
				} else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admin Can add new users");
				}

			}
		}catch(NullValueException e) {
			e.printStackTrace();
			throw e;
		}catch(DuplicateValueException e) {
			e.printStackTrace();
			throw e;
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Something Went Wrong");
		}
		

	}

//
//	public ResponseEntity<?> getUserList(HttpServletRequest requestServlet, int pageNo, int noOfRecord,
//			String searchKey, String role) throws Exception {
//
//		String loggedInUserName = requestServlet.getUserPrincipal().getName();
//		if (loggedInUserName != null && loggedInUserName.length() > 0) {
//			AdminUserModel userModel = adminUserDAO.findByEmail(loggedInUserName);
//			if (userModel == null) {
//				throw new Exception("User not logged In");
//			}
//		}
//
//		String message = "";
//		String statusCode = "";
//		List<AdminUserModel> userList = new ArrayList<AdminUserModel>();
//		AdminUserResponse response;
//		try {
//
//			Pageable pageRequest = PageRequest.of(pageNo, noOfRecord);
//
//			Page<AdminUserEntity> userListPage;
//			if (searchKey != null && searchKey.length() > 0) {
//				userListPage = adminUserDAO.getUserList(searchKey, pageRequest);
//				userList.addAll(userListPage.getContent());
//			} else {
//				userListPage = adminUserDAO.getUserList(pageRequest);
//				userList.addAll(userListPage.getContent());
//			}
//
//			statusCode = "200 ok";
//			message = "User Details Fetch Successfully";
//
//			PageDetails pageDetails = new PageDetails(pageNo, noOfRecord, userListPage.getTotalPages(),
//					userListPage.getTotalElements());
//
//			response = new AdminUserResponse(message, statusCode, userList, pageDetails);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
//
//		return response;
//	}

	public ResponseEntity<?> adminLogin(LoginRequest request) throws Exception {

		if (request == null) {
			throw new NullValueException("Request Body Must not be Empty");
		}

		if (request.getEmail() == null) {
			throw new NullValueException("email must not be Empty");
		}

		if (request.getPassword() == null) {
			throw new NullValueException("Password must not be Empty");
		}

		String message = "";
		String authToken = "";
		String name = "";
		String email = "";
		String mobileNo = "";
		String region = "";
		String role = "";

		try {
			try {

				authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword()));
			} catch (BadCredentialsException e) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
			} catch (UsernameNotFoundException e) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username");
			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
			}
			UserDetails userDetails = loginService.loadUserByUsername(request.getEmail());
			AdminUserEntity adminUserModel = adminUserDAO.findByEmail(request.getEmail());
			authToken = jwtUtil.generateToken(userDetails);
			role = adminUserModel.getRoleModel().getRoleName().toLowerCase();
			name = adminUserModel.getName().toUpperCase();
			email = adminUserModel.getEmail().toLowerCase();
			mobileNo = adminUserModel.getMobileNo();
			if (!role.equals("admin")) {
				if (adminUserModel.getRegionEntity() != null) {
					region = adminUserModel.getRegionEntity().getCity().toUpperCase();
				}
			}
			message = "Logged in Successfully";

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		LoginResponse data = new LoginResponse(authToken, message, role, name, email, mobileNo, region);
		return ResponseEntity.ok().body(data);
	}

	private void validateBasicInfo(AddUserRequest request) throws Exception {
		String email = request.getEmail().toLowerCase();

		if (email == null) {
			throw new NullValueException("Email Not Available");
		}

		if (adminUserDAO.findByEmail(email) != null) {
			throw new DuplicateValueException("Email Already Available");
		}

		if (!CommonUtils.validateMobileNumber(request.getMobileNo())) {
			throw new NullValueException("Wrong mobile no format");
		}

		if (!CommonUtils.validateEmailFormat(request.getEmail())) {
			throw new NullValueException("Wrong Email Id Format");
		}
	}

//	private String generateRandomPassword() {
//
//		String lowerCaseText = "abcdefghijklmnopqrstuvwxyz";
//		String upperCaseText = lowerCaseText.toUpperCase();
//		String digit = "0123456789";
//
//		String password = lowerCaseText + upperCaseText + digit;
//		SecureRandom random = new SecureRandom();
//
//		StringBuilder sb = new StringBuilder(15);
//		for (int indexCount = 0; indexCount < 15; indexCount++) {
//			int charPosition = random.nextInt(password.length());
//			char passwordChar = password.charAt(charPosition);
//
//			sb.append(passwordChar);
//		}
//		return sb.toString();
//	}

//	public void sendMail(String subject, String sendTOEmail, String mailBody) throws Exception {
//		MimeMessage message = emailSender.createMimeMessage();
//		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//		helper.setFrom(sendMailId);
//		helper.setTo(sendTOEmail);
//		helper.setSubject(subject);
//		helper.setText(mailBody, true);
//
//		emailSender.send(message);
//	}

	public ResponseEntity<?> changePassword(HttpServletRequest requestServlet, PasswordRequest requestBody)
			throws Exception {

		AdminUserEntity loginUserDetails = null;
		String loggedInUserName = null;
		try {
			loggedInUserName = requestServlet.getUserPrincipal().getName();
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
		}
		try {
			if (loggedInUserName != null && loggedInUserName.length() > 0) {
				loginUserDetails = adminUserDAO.findByEmail(loggedInUserName);
				if (loginUserDetails == null) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged In");
				}
			}

			if (requestBody == null || requestBody.getOldPassword() == null || requestBody.getNewPassword() == null
					|| requestBody.getOldPassword().length() == 0 || requestBody.getNewPassword().length() == 0) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old and New Password both are required ");
			}
			String message = "";

			String oldPassword = requestBody.getOldPassword();
			if (oldPassword != null && oldPassword.length() > 0) {
				String newPassword = requestBody.getNewPassword();
				if (newPassword != null && newPassword.length() > 0) {
					newPassword = passwordEncoder.encode(newPassword);
					if (passwordEncoder.matches(oldPassword, loginUserDetails.getPassword())) {

						loginUserDetails.setPassword(newPassword);
						loginUserDetails = adminUserDAO.save(loginUserDetails);
						if (loginUserDetails != null) {
							message = "User password Changed Successfully";
							return ResponseEntity.status(HttpStatus.OK).body(message);
						} else {
							message = "Unable to Change password";
							return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
						}
					} else {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect password");
					}

				} else {

					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("new password is Required");
				}
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("old password not provided, it's require for change password");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// user Delete Method

	public ResponseEntity<String> deleteUser(HttpServletRequest request, String email) throws Exception {

		AdminUserEntity userModel = null;
		String loggedInUserName = null;
		try {
			loggedInUserName = request.getUserPrincipal().getName();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in or not Authorized");
		}
		if (loggedInUserName != null && loggedInUserName.length() > 0) {
			try {
				userModel = adminUserDAO.findByEmail(loggedInUserName);
				if (userModel == null) {
					ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged In");
				}
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
			}
		}
		String role = userModel.getRoleModel().getRoleName().toLowerCase();
		
		if (role.equals("admin")) {
			if (email == null || (email!=null && email.trim().length()<=0)) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body("Email id required");
			}
			if (email.toLowerCase().equals(loggedInUserName.toLowerCase())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("you can not delete your own id");
			}
			try {
				AdminUserEntity adminModel = adminUserDAO.findByEmail(email);
				if (adminModel == null) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not register with this Email");
				}else {
				if(adminModel.getRoleModel()!=null) {
					if(adminModel.getRoleModel().getRoleName().toLowerCase().equals("admin")) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No one can delete admin user");
					}
				}
				adminUserDAO.delete(adminModel);
				return ResponseEntity.status(HttpStatus.OK).body("delete successfully");
				}

			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
			}
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only ADMIN can delete User Details");
		}
	}

	// change employee password changed by admin
	public ResponseEntity<?> changeUserPassword(HttpServletRequest requestServlet,
			ChangeEmployeePasswordRequest passRequestBody) {

		AdminUserEntity adminModel = null;
		String loggedInUserName = null;
		try {
			loggedInUserName = requestServlet.getUserPrincipal().getName();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in or not Authorized");
		}
		if (loggedInUserName != null && loggedInUserName.trim().length() > 0) {
			try {
				adminModel = adminUserDAO.findByEmail(loggedInUserName);
				if (adminModel == null) {
					ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged In");
				}
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
			}
		}
		if(adminModel.getRoleModel()==null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Role assign to you, contact to admin");
		}
		String role = adminModel.getRoleModel().getRoleName().toLowerCase();

		if (role.equals("admin") || role.equals("manager")){
			if (passRequestBody == null || passRequestBody.getEmpEmail() == null || (passRequestBody.getEmpEmail()!=null && passRequestBody.getEmpEmail().trim().length()<=0)
					|| (passRequestBody.getNewPassword() != null && passRequestBody.getNewPassword().trim().length() <=0)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please provide all field, Like Employee Emial and new Password");
			}
			try {
				AdminUserEntity userModel = adminUserDAO.findByEmail(passRequestBody.getEmpEmail().toLowerCase());
				if (userModel != null) {
					if(userModel.getRoleModel().getRoleName()==null || adminModel.getRoleModel().getRoleName()!=null && adminModel.getRoleModel().getRoleName().trim().length()<=0) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No role assign to this user, First contact to admin to assign a role");
					}
					String employeeRole=userModel.getRoleModel().getRoleName().toLowerCase();
					if (employeeRole.equals("admin")) {
						return ResponseEntity.status(HttpStatus.FORBIDDEN)
								.body("admin password can not changed by this panel");
					}else if(employeeRole.equals(role)){
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You can not change any employee password, Who is equals to your Role");
					}else {
						if(role.equals("manager")) {
							if(adminModel.getRegionEntity()==null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No region Assign to you, contact to admin");
						}else {
							if(userModel.getRegionEntity()==null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No region Assign to this Employee, contact to admin");
							}else if(!adminModel.getRegionEntity().equals(userModel.getRegionEntity())) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This employee not belonging to your region");
							}else {
								//blank not need to do any thing
							}
						}
					}
					String newPassword= passwordEncoder.encode(passRequestBody.getNewPassword());
					userModel.setPassword(newPassword);
					userModel = adminUserDAO.save(userModel);
					if (userModel != null) {
						return ResponseEntity.status(HttpStatus.OK).body("Employee Password Changed SuccessFully");
					} else {
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
					}
					}
				} else {

					return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not register with this Email");
				}
				}catch (Exception e) {
					e.printStackTrace();
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
				}
			
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only ADMIN or Manager can change Employee password");
		}
	
		}

}
