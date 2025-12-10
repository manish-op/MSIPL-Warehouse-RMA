package com.serverManagement.server.management.service.region;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.region.RegionDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;
import com.serverManagement.server.management.request.region.UpdateEmployeeRegionRequest;
import com.serverManagement.server.management.request.region.UpdateRegionRequest;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class RegionService {

	@Autowired
	private RegionDAO regionDAO;
	@Autowired
	private AdminUserDAO adminUserDAO;
	private AdminUserEntity adminUserEntity;
	private RegionEntity regionEntity;

	// Method for Getting Region List
	public ResponseEntity<?> getRegionList() {
		List<String> regionList;
		try {
			regionList = regionDAO.findAllCity();
			if (regionList == null || regionList.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
			} else {
					regionList.sort((e1, e2) -> e1.compareTo(e2));
				return ResponseEntity.status(HttpStatus.OK).body(regionList);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
		}
	}

	// Method for Add Region By Admin
	public ResponseEntity<?> addRegion(HttpServletRequest request, String region) {

		String loggedInEmail = null;
		try {
			loggedInEmail = request.getUserPrincipal().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
		}
		try {
			adminUserEntity = adminUserDAO.findByEmail(loggedInEmail);
			if (adminUserEntity == null) {
				throw new Exception("User not Logged In");
			} else {
				if (adminUserEntity.getRoleModel().getRoleName().toLowerCase().equals("admin")) {
					if (region == null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Region value is empty, it must not be Empty");
					} else {
						boolean isAvail = regionDAO.existsByCity(region.toLowerCase());
						if (isAvail) {
							return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
									.body("This Region Already available");
						} else {
							RegionEntity regionEntity = new RegionEntity();
							regionEntity.setCity(region.toUpperCase());
							regionEntity = regionDAO.save(regionEntity);
							if (regionEntity != null) {
								return ResponseEntity.status(HttpStatus.CREATED).body("Region Added successfully");
							} else {
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
										.body("Something Went wrong");
							}
						}
					}
				} else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admin can Add Region");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error");
		}
	}

	// Method for Update Region
	public ResponseEntity<?> updateRegion(HttpServletRequest request, UpdateRegionRequest region) {

		String loggedInEmail = null;
		try {
			loggedInEmail = request.getUserPrincipal().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
		}
		try {
			adminUserEntity = adminUserDAO.findByEmail(loggedInEmail);
			if (adminUserEntity == null) {
				throw new Exception("User not Logged In");
			} else {
				if (adminUserEntity.getRoleModel().getRoleName().toLowerCase().equals("admin")) {
					
					if (region == null || region.getOldRegion() == null || region.getUpdatedRegion() == null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Region value is empty, it must not be Empty");
					} else {
						regionEntity = regionDAO.findByCity(region.getOldRegion().toLowerCase());
						if (regionEntity == null) {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									.body("This region not available, put the right one");
						} else {
							regionEntity.setCity(region.getUpdatedRegion().toUpperCase());
							regionEntity = regionDAO.save(regionEntity);
							if (regionEntity != null) {
								return ResponseEntity.status(HttpStatus.OK).body("Region Update successfully");
							} else {
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
										.body("Something Went wrong");
							}
						}
					}
				} else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admin can Add Region");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error");
		}
	}

	// change employee region by make by admin
	public ResponseEntity<?> updateEmployeeRegion(HttpServletRequest request,
			UpdateEmployeeRegionRequest updateEmployeeRegion) {

		String loggedInEmail = null;
		String message = "";
		try {
			loggedInEmail = request.getUserPrincipal().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
		}

		try {
			adminUserEntity = adminUserDAO.findByEmail(loggedInEmail);
			if (adminUserEntity == null) {

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not logged in");
			}
			String role = adminUserEntity.getRoleModel().getRoleName().toLowerCase();
			if (role.equals("admin")) {
				if (updateEmployeeRegion == null || updateEmployeeRegion.getEmpEmail() == null
						|| updateEmployeeRegion.getRegion() == null) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("All fields Required");
				}
				AdminUserEntity employeeDetails = adminUserDAO
						.findByEmail(updateEmployeeRegion.getEmpEmail().toLowerCase());
				if (employeeDetails == null) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Employee register with this email");
				} else if (employeeDetails.getRoleModel().getRoleName().toLowerCase().equals("admin")) {
					message = "Region for Admin can not be assign";
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
				} else {

					RegionEntity regionDetails = regionDAO.findByCity(updateEmployeeRegion.getRegion().toLowerCase());
					if (regionDetails == null) {

						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not have any region with this name");
					} else {
						if (employeeDetails.getRegionEntity() == null) {

							employeeDetails.setRegionEntity(regionDetails);
							employeeDetails = adminUserDAO.save(employeeDetails);
							if (employeeDetails != null) {

								return ResponseEntity.status(HttpStatus.OK).body("Region changed successfully");
							} else {
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
										.body("Region not changed, Something went wrong");
							}
						} else {
							if (employeeDetails.getRegionEntity().equals(regionDetails)) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("Employee already belonging from this region");
							} else {
								employeeDetails.setRegionEntity(regionDetails);
								employeeDetails = adminUserDAO.save(employeeDetails);
								if (employeeDetails != null) {

									return ResponseEntity.status(HttpStatus.OK).body("Region changed successfully");
								} else {
									return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
											.body("Region not changed, Something went wrong");
								}
							}
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
