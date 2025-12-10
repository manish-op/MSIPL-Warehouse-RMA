package com.serverManagement.server.management.service.option;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.option.ItemAvailableStatusOptionDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.options.ItemAvailableStatusOptionEntity;
import com.serverManagement.server.management.request.option.UpdateAvailableStatusRequest;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ItemAvailableStatusOptionService {

	@Autowired
	private ItemAvailableStatusOptionDAO itemAvailableStatusOptionDAO;
	@Autowired
	private AdminUserDAO adminUserDAO;
	private AdminUserEntity adminUserEntity;

	// method for get option
	public ResponseEntity<?> getAvailableOptions() {
		try {
			List<String> allOptionsClass = itemAvailableStatusOptionDAO.getStatusOptionList();
			if (allOptionsClass != null) {
				return ResponseEntity.status(HttpStatus.OK).body(allOptionsClass);
			} else {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	// method for add option
	public ResponseEntity<?> addAvailableOptions(HttpServletRequest request, String optionName) {

		String loggedInEmail = null;
		try {
			loggedInEmail = request.getUserPrincipal().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
		}
		try {
			adminUserEntity = adminUserDAO.findByEmail(loggedInEmail);
			if (adminUserEntity == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not authenticated");
			} else {
				if (adminUserEntity.getRoleModel().getRoleName().toLowerCase().equals("admin")) {

					if (optionName == null || optionName.trim().length()<=0) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("option name is null");
					} else {
						boolean optionIsAvailable = itemAvailableStatusOptionDAO
								.existsByOptionValue(optionName.toLowerCase());
						if (optionIsAvailable) {
							return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
									.body("this option is already available");
						} else {
							ItemAvailableStatusOptionEntity newOptionAdding = new ItemAvailableStatusOptionEntity();
							newOptionAdding.setItemAvailableOption(optionName.toUpperCase());
							newOptionAdding = itemAvailableStatusOptionDAO.save(newOptionAdding);
							if (newOptionAdding != null) {
								return ResponseEntity.status(HttpStatus.OK).body("added successfully");
							} else {
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
										.body("Something went wrong");
							}
						}
					}
				} else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("only Admin can add option");
				}
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	// update Available Option
	public ResponseEntity<?> updateAvailableOptions(HttpServletRequest request,
			UpdateAvailableStatusRequest updateOptionRequest) {

		String loggedInEmail = null;
		try {
			loggedInEmail = request.getUserPrincipal().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
		}
		try {
			adminUserEntity = adminUserDAO.findByEmail(loggedInEmail);
			if (adminUserEntity == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
			} else {
				if (adminUserEntity.getRoleModel().getRoleName().toLowerCase().equals("admin")) {

					if (updateOptionRequest == null || updateOptionRequest.getExistingOption() == null || (updateOptionRequest.getExistingOption()!=null && updateOptionRequest.getExistingOption().trim().length()<=0)
							|| updateOptionRequest.getNewOption() == null ||(updateOptionRequest.getNewOption()!=null && updateOptionRequest.getNewOption().trim().length()<=0)) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("both field required");
					} else {
						ItemAvailableStatusOptionEntity getDetailsForUpdate = itemAvailableStatusOptionDAO.getStatusDetailsByOption(updateOptionRequest.getExistingOption().toLowerCase());
						if (getDetailsForUpdate != null) {
							boolean isExist=itemAvailableStatusOptionDAO.existsByOptionValue(updateOptionRequest.getNewOption().toLowerCase());
							if(isExist) {
								return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("new option is already available");
							}
							getDetailsForUpdate
									.setItemAvailableOption(updateOptionRequest.getNewOption().toUpperCase());
							getDetailsForUpdate = itemAvailableStatusOptionDAO.save(getDetailsForUpdate);
							if (getDetailsForUpdate != null) {
								return ResponseEntity.status(HttpStatus.OK).body("updated successfully");
							} else {
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
										.body("Something went wrong");
							}
						} else {
							return ResponseEntity.status(HttpStatus.NOT_FOUND)
									.body("this option is not listed before");
						}
					}
				} else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("only Admin can add option");
				}
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

}
