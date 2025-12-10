package com.serverManagement.server.management.service.option;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.option.ItemStatusOptionDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.options.ItemStatusOptionEntity;
import com.serverManagement.server.management.request.option.UpdateItemStatusOtionRequest;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ItemStatusOptionService {

	@Autowired
	private ItemStatusOptionDAO itemStatusOptionDAO;
	@Autowired 
	private AdminUserDAO adminUserDAO;
	private AdminUserEntity adminUserEntity;
	//private ItemStatusOptionEntity itemStatusOptionEntity;
	
	//get all option list
	public ResponseEntity<?> getItemStatusOptions() {
		try {
			List<String> allOptionsClass=itemStatusOptionDAO.getItemStatusOptionList();
			if(allOptionsClass !=null) {
				return ResponseEntity.status(HttpStatus.OK).body(allOptionsClass);
			}else {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
			}
			}catch(Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
			}
	}

	//add item Status option
	public ResponseEntity<?> addItemStatusOptions(HttpServletRequest request, String optionName) {
		
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
					
					if(optionName==null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("option name is null");
					}else {
						boolean optionIsAvailable=itemStatusOptionDAO.existsByItemStatusOptionValue(optionName.toLowerCase());
						if(optionIsAvailable) {
							return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("this option is already available");
						}else {
							ItemStatusOptionEntity newOptionAdding=new ItemStatusOptionEntity();
							newOptionAdding.setItemStatus(optionName.toUpperCase());
							newOptionAdding=itemStatusOptionDAO.save(newOptionAdding);
							if(newOptionAdding !=null) {
								return ResponseEntity.status(HttpStatus.OK).body("added successfully");
							}else {
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
							}
						}
					}
				}else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("only Admin can add option");
				}
			}
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	
	
	//update item status option 
	public ResponseEntity<?> updateItemStatusOptions(HttpServletRequest request,
			UpdateItemStatusOtionRequest updateStatusOption) {
		// TODO Auto-generated method stub
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
					
					if(updateStatusOption==null || updateStatusOption.getNewStatus()==null || updateStatusOption.getOldStatus()==null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("all field required");
					}else {
						ItemStatusOptionEntity updateOptionData=itemStatusOptionDAO.getItemStatusOptionDetails(updateStatusOption.getOldStatus().toLowerCase());
						if(updateOptionData != null) {
						boolean optionIsAvailable=itemStatusOptionDAO.existsByItemStatusOptionValue(updateStatusOption.getNewStatus().toLowerCase());
						if(optionIsAvailable) {
							return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("new option is already available");
						}
							
						updateOptionData.setItemStatus(updateStatusOption.getNewStatus().toUpperCase());
						updateOptionData=itemStatusOptionDAO.save(updateOptionData);
							if(updateOptionData !=null) {
								return ResponseEntity.status(HttpStatus.OK).body("added successfully");
							}else {
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
							}
						}else {
							return ResponseEntity.status(HttpStatus.NOT_FOUND).body("this option is not listed before");
						}
					}
				}else {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("only Admin can add option");
				}
			}
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

}
