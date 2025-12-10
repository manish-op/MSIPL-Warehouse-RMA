package com.serverManagement.server.management.service.itemRepairingDetails;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDTO;
import com.serverManagement.server.management.dao.itemRepairDetails.FruDAO;
import com.serverManagement.server.management.dao.itemRepairDetails.ItemRepairingDAO;
import com.serverManagement.server.management.dao.itemRepairDetails.ItemRepairingDetilsDynamicQueryForManager;
import com.serverManagement.server.management.dao.itemRepairOption.RepairingOptionDAO;
import com.serverManagement.server.management.dao.itemRepairOption.TechnicianStatusDAO;
import com.serverManagement.server.management.dao.itemRepairOption.WarrantyOptionDAO;
import com.serverManagement.server.management.dao.keyword.KeywordDAO;
import com.serverManagement.server.management.dao.keyword.SubKeywordDAO;
import com.serverManagement.server.management.dao.option.ItemAvailableStatusOptionDAO;
import com.serverManagement.server.management.dao.option.ItemStatusOptionDAO;
import com.serverManagement.server.management.dao.region.RegionDAO;
import com.serverManagement.server.management.dao.role.RoleDAO;
import com.serverManagement.server.management.dto.itemRepairDetails.TicketDetailsViaIdDTO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.adminUser.UserEmailRegionDTO;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.itemDetails.ItemHistoryUpdatedByAdminEntity;
import com.serverManagement.server.management.entity.itemRepairDetails.FruEntity;
import com.serverManagement.server.management.entity.itemRepairDetails.ItemRepairingEntity;
import com.serverManagement.server.management.entity.itemRepairDetails.ItemRepairingEntityDTO;
import com.serverManagement.server.management.entity.itemRepairOption.RepairingOptionEntity;
import com.serverManagement.server.management.entity.itemRepairOption.TechnicianStatusEntity;
import com.serverManagement.server.management.entity.itemRepairOption.WarrantyOptionEntity;
import com.serverManagement.server.management.entity.keyword.KeywordEntity;
import com.serverManagement.server.management.entity.keyword.SubKeywordEntity;
import com.serverManagement.server.management.entity.options.ItemAvailableStatusOptionEntity;
import com.serverManagement.server.management.entity.options.ItemStatusOptionEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;
import com.serverManagement.server.management.entity.role.RoleEntity;
import com.serverManagement.server.management.request.itemRepairingDetails.AssignTechnicianRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.EngineerUpdateTicketStatusRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.FruTicketRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.GetAssignTicketDetailsRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.ItemRepairingRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.ManagerGetTicketRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.UpdateTicketStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class ItemRepairingService {

	@Autowired
	private ItemRepairingDAO itemRepairingDAO;
	@Autowired
	private ItemDetailsDAO itemDetailsDAO;
	@Autowired
	private AdminUserDAO adminUserDAO;
	@Autowired
	private RegionDAO regionDAO;
	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private KeywordDAO keywordDAO;
	@Autowired
	private SubKeywordDAO subKeywordDAO;
	@Autowired
	private ItemAvailableStatusOptionDAO itemAvailableStatusDAO;
	@Autowired
	private ItemStatusOptionDAO itemStatusOptionDAO;
	@Autowired
	private RepairingOptionDAO repairingOptionDAO;
	@Autowired
	private WarrantyOptionDAO warrantyOptionDAO;
	@Autowired
	private FruDAO fruDAO;
	@Autowired
	private TechnicianStatusDAO technicianStatusDAO;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private ItemRepairingDetilsDynamicQueryForManager itemRepairingDetilsDynamicQueryForManager;

	private AdminUserEntity adminUserEntity;
//	private RegionEntity regionEntity;
//	private KeywordEntity keywordEntity;
//	private SubKeywordEntity subKeywordEntity;
//	private ItemDetailsEntity itemDetailsEntity;
	private ItemAvailableStatusOptionEntity itemAvailEntity;
//	private ItemStatusOptionEntity itemStatusOptionEntity;

	// this method is made for generate ticket via FRU number
	// sorted
	// Done
//	public ResponseEntity<?> generateTicketViaFru(HttpServletRequest request,
//			FruTicketRequest repairingRequest) throws Exception {
//
//		String loggedInUserName = null;
//		try {
//			loggedInUserName = request.getUserPrincipal().getName().toLowerCase();
//		} catch (NullPointerException e) {
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
//		}
//		try {
//			if (loggedInUserName != null && loggedInUserName.length() > 0) {
//				adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
//				if (adminUserEntity == null) {
//					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Login");
//				} else {
//					String loginUserRole = adminUserEntity.getRoleModel().getRoleName().toLowerCase();
//					if (repairingRequest == null || repairingRequest.getRmaNo() == null
//							|| (repairingRequest.getRmaNo() != null && repairingRequest.getRmaNo().length() < 1)
//							|| repairingRequest.getItemDetail().getSerialNo() == null) {
//						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("rmaNo and serial no is required");
//					} else {
//
//						FruEntity fruConfirmation = fruDAO.getRmaConfirmation(repairingRequest.getRmaNo(), );
//						if (fruConfirmation != null) {
//							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//									.body("A ticket is already generated with this RMA NO");
//						}
//
//						FruEntity fruTable = new FruEntity();
//						FruEntity fruEntity=new FruEntity();
//						fruEntity=fruDAO.getRmaConfirmation(repairingRequest.getRmaNo().trim().toLowerCase(), repairingRequest.getItemDetail());
//						if(fruEntity != null) {
//							return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Rma No: "+repairingRequest.getRmaNo().trim().toLowerCase()+" is already registered");
//						}
//						fruTable.setRmaNo(repairingRequest.getRmaNo().trim().toLowerCase());
//						fruTable.setCreatedDate(LocalDate.now());
//						fruTable.setRegionDetails(null);-------------------------------
//
//						ItemRepairingRequest itemFromUser = repairingRequest.getItemDetail();
//
//						ItemDetailsEntity itemDetails = itemDetailsDAO
//								.getItemDetailsBySerialNo(itemFromUser.getSerialNo().trim().toLowerCase());
//						if (itemDetails == null) {
//							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//									.body("This serialNo: " + itemFromUser.getSerialNo() + " not in our database");
//						} else {
//							if (itemDetails.getAvailableStatusId().getItemAvailableOption().toLowerCase()
//									.equals("repairing")) {
//								return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//										"This item is already under repairing state we can not create another Ticket for this");
//							}
//							if (itemDetails.getAvailableStatusId().getItemAvailableOption().toLowerCase()
//									.equals("delete")) {
//								return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//										"This item is already under Deleted state we can not create Ticket for this");
//							}
//							if (!loginUserRole.equals("admin")) {
//								if (!itemDetails.getRegion().equals(adminUserEntity.getRegionEntity())) {
//									return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//											.body("This serialNo not belonging to your region");
//								}
//							}
//							// this section is for getting repairing option details from table
//							RepairingOptionEntity repairOption = repairingOptionDAO.getOptionDetails("pending");
//							if (repairOption == null) {
//								RepairingOptionEntity newRepairingOption = new RepairingOptionEntity();
//								newRepairingOption.setStatusOption("pending");
//								repairOption = repairingOptionDAO.save(newRepairingOption);
//								if (repairOption == null) {
//									return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//											.body("Getting error when creating Repairing option");
//								}
//							}
//							ItemRepairingEntity itemInformation = new ItemRepairingEntity();
//							itemInformation.setItemDetailId(itemDetails);
//							itemInformation.setRmaNo(repairingRequest.getRmaNo().trim().toLowerCase());
//							itemInformation.setSerialNo(itemDetails.getSerial_No());
//							itemInformation.setFaultDetails(itemFromUser.getDetailedExplaination());
//							itemInformation.setDocketIdInward(itemFromUser.getInwardGatepass());
//							itemInformation.setFruId(fruTable);
//							itemInformation.setGeneratedDate(LocalDate.now());
//							itemInformation.setLastUpdateDate(LocalDate.now());
//							itemInformation.setRegion(itemDetails.getRegion());
//							itemInformation.setTicketGeneratedBy(loggedInUserName.toLowerCase());
//							itemInformation.setRepairStatus(repairOption);
//
//							// this section is creating technicianStatus pending/Assign
//							TechnicianStatusEntity technicianStatus = technicianStatusDAO
//									.getTechnicianStatus("pending");
//
//							if (technicianStatus == null) {
//								TechnicianStatusEntity newTechnicianStatus = new TechnicianStatusEntity();
//								newTechnicianStatus.setTechnicianAssign("pending");
//								newTechnicianStatus = technicianStatusDAO.save(newTechnicianStatus);
//								if (newTechnicianStatus == null) {
//									return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//											.body("Getting error when creating technician Status");
//								} else {
//									technicianStatus = newTechnicianStatus;
//								}
//							}
//							itemInformation.setTechnicianStatus(technicianStatus);
//
//							// this section is for getting warranty option details from table
//							WarrantyOptionEntity warrantyOption = warrantyOptionDAO
//									.getWarrantyOption(itemFromUser.getWarrantyDetails().toLowerCase());
//							if (warrantyOption == null) {
//								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//										.body("This warranty option is not listed before");
//							}
//							itemInformation.setWarrantyDetails(warrantyOption);
//
//							fruTable.setRepairingIdList(itemInformation);
//
//							// availability status getting from databse to change item status in main item
//							// details table
//							itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption("repairing");
//							if (itemAvailEntity != null) {
//								ItemHistoryUpdatedByAdminEntity historyChangedByAdmin = new ItemHistoryUpdatedByAdminEntity();
//								historyChangedByAdmin.setSerial_No(itemDetails.getSerial_No().toLowerCase());
//
//								itemDetails.setAvailableStatusId(itemAvailEntity);// changing status at main table
//								historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
//
//								itemDetails.setEmpEmail(loggedInUserName);
//								historyChangedByAdmin.setUpdatedByEmail(loggedInUserName);
//
//								itemDetails.setUpdate_Date(ZonedDateTime.now());
//								historyChangedByAdmin.setUpdate_Date(ZonedDateTime.now());
//
//								if (historyChangedByAdmin != null) {
//									// setting table reference for Admin history table
//									historyChangedByAdmin.setItemDetailsEntity(itemDetails);
//									List<ItemHistoryUpdatedByAdminEntity> historyChangedByAdminList = new ArrayList<ItemHistoryUpdatedByAdminEntity>();
//									historyChangedByAdminList.add(historyChangedByAdmin);
//									// add reference for admin history table
//									itemDetails.setItemHistoryUpdatedByAdminEntityList(historyChangedByAdminList);
//								}
//
//							} else {
//								return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//										"repairing status is not listed in available status option table please add it before doing this");
//							}
//
//							fruTable = fruDAO.save(fruTable);
//							if (fruTable != null) {
//
//								// changing main item detail table update their status
//								itemDetails = itemDetailsDAO.save(itemDetails);// save to database
//								if (itemDetails != null) {
//									// if both is success then OK
//									return ResponseEntity.status(HttpStatus.OK)
//											.body("ticket generated for this fru ticketNo:- " + fruTable.getId());
//								} else {
//									// if main table not update then delete this ticket record and give error
//									// message
//									fruDAO.delete(fruTable);
//									return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//											"Something went wrong at the time of changing main item setails table");
//								}
//							} else {
//								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//										.body("Something Went wrong when creating ticket");
//							}
//						}
//					}
//				}
//			} else {
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("your are not loggedIn");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
//		}
//	}

	// this method is made for assign ticket to a person
	// Done
	//need to remove comment for repaired ticket assign technician
	public ResponseEntity<?> assignTechnician(HttpServletRequest request, AssignTechnicianRequest assignTicketRequest)
			throws Exception {

		String loggedInUserName = null;
		try {
			loggedInUserName = request.getUserPrincipal().getName().toLowerCase();
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
		}
		try {
			if (loggedInUserName != null && loggedInUserName.length() > 0) {
				adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
				if (adminUserEntity == null) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Login");
				} else {
					String loginUserRole = adminUserEntity.getRoleModel().getRoleName().toLowerCase();
					if (loginUserRole.equals("admin") || loginUserRole.equals("manager")) {
						if (assignTicketRequest == null || assignTicketRequest.getTicketId() == null
								|| (assignTicketRequest.getTicketId() != null
										&& assignTicketRequest.getTicketId() == null)
								|| assignTicketRequest.getTechnicianEmail() == null
								|| (assignTicketRequest.getTechnicianEmail() != null
										&& assignTicketRequest.getTechnicianEmail().trim().length() < 0)) {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									.body("Technician email and ticket id is required");
						} else {

							ItemRepairingEntity ticketDetails = itemRepairingDAO
									.getRepairingTicketViaTicketId(assignTicketRequest.getTicketId());

							if (ticketDetails == null) {
								return ResponseEntity.status(HttpStatus.NO_CONTENT).body("This Ticket number:- '"
										+ assignTicketRequest.getTicketId() + " ' is not created before");
							}
							if (ticketDetails.getRegion() == null || ticketDetails.getRegion().getId() == null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("This ticket not have any region");
							}
//							if(ticketDetails.getRepairStatus().getStatusOption().trim().toLowerCase().equals("repaired") ||
//									ticketDetails.getRepairStatus().getStatusOption().trim().toLowerCase().equals("replaced")) {
//								return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This ticket is already closed");
//							}
							if (ticketDetails.getTechnicianStatus() != null
									&& ticketDetails.getTechnicianStatus() != null
									&& ticketDetails.getTechnicianStatus().getTechnicianAssign().toLowerCase()
											.equals("assign")) {
								if(ticketDetails.getAssignByManager()!=null) {
									if(!ticketDetails.getAssignByManager().trim().toLowerCase().equals(adminUserEntity.getEmail().trim().toLowerCase())) {
										return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only assign by manager can change");
									}
								}
							}

							// getting employee details via email id
							UserEmailRegionDTO userConfirmation = adminUserDAO
									.confirmUserEmail(assignTicketRequest.getTechnicianEmail().trim().toLowerCase());
							if (userConfirmation == null) {

								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("No technician assign with this email");
							}
							if(userConfirmation.getRoleModelId().getRoleName().trim().toLowerCase().equals("admin")) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("No one can assign ticket to any admin");
							}
							if (userConfirmation.getRegionEntityId() == null
									|| userConfirmation.getRegionEntityId().getId() == null) {

								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("No region Assign to this technician");
							}

							// This section is for check verify and created limitation for manager role
							if (loginUserRole.equals("manager")) {
								if (adminUserEntity.getRegionEntity() == null
										|| adminUserEntity.getRegionEntity().getId() == null) {
									return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
											.body("No region assign to you");
								}

								if (!ticketDetails.getRegion().equals(adminUserEntity.getRegionEntity())) {
									return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
											.body("Your region is not matching with ticket region id");
								}
								if (!userConfirmation.getRegionEntityId().equals(adminUserEntity.getRegionEntity())) {

									return ResponseEntity.status(HttpStatus.BAD_REQUEST)
											.body("This technician not belonging to your region");
								}
							}
							if (userConfirmation.getTechEmail().trim().toLowerCase()
									.equals(adminUserEntity.getEmail().trim().toLowerCase())) {

								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("you can not assign ticket to youself");
							}

							// check if he assign ticket to any another manager other than his or assign to
							// admin
//							if (!adminUserEntity.getEmail().toLowerCase().equals(userConfirmation.getTechEmail())) {
//
//								if (userConfirmation.getRoleModelId().getRoleName().toLowerCase().equals("admin")
//										&& adminUserEntity.getRoleModel().getRoleName().trim().toLowerCase()
//												.equals("manager")) {
//
//									return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//											.body("You can not assign ticket to any admin");
//								}
//								// check for manager and admin section closed
//
//							} // manager section closing

							if (!userConfirmation.getRegionEntityId().equals(ticketDetails.getRegion())) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("This technician not belonging to ticket region");
							}

							RepairingOptionEntity repairOption = repairingOptionDAO.getOptionDetails("pending");
							if (repairOption == null) {
								RepairingOptionEntity newRepairingOption = new RepairingOptionEntity();
								newRepairingOption.setStatusOption("pending");
								repairOption = repairingOptionDAO.save(newRepairingOption);
								if (repairOption == null) {
									return ResponseEntity.status(HttpStatus.BAD_REQUEST)
											.body("Getting error when creating Repairing option");
								}
							}
							ticketDetails.setRepairStatus(repairOption);
							ticketDetails.setTechnician_Name(userConfirmation.getTechEmail().toLowerCase());
							ticketDetails.setTech_Assign_Date(LocalDate.now());
							ticketDetails.setAssignByManager(loggedInUserName);
							TechnicianStatusEntity technicianStatus = technicianStatusDAO.getTechnicianStatus("assign");
							ticketDetails.setTechnicianStatus(technicianStatus);

							ticketDetails = itemRepairingDAO.save(ticketDetails);

							if (ticketDetails != null) {
								return ResponseEntity.status(HttpStatus.OK).body(
										"Engineer assign for ticket id:-" + ticketDetails.getId() + " is Successfully");
							} else {
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
										.body("Some internal error getting");
							}

						}
					} else {
						return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
								.body("Only Admin or Manager have permission");
					}
				}
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User Not logged in");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
		}
	}


	
	// get ticket via date by default show all requests by admin and manager sorted
	// Done
	public ResponseEntity<?> getRepairingTicket(HttpServletRequest request,
			ManagerGetTicketRequest ticketDetailsRequest) throws Exception {

		String loggedInUserName = null;
		try {
			loggedInUserName = request.getUserPrincipal().getName().toLowerCase();
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
		}
		try {
			if (loggedInUserName != null && loggedInUserName.length() > 0) {
				adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
				if (adminUserEntity == null) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Login");
				} else {
					String loginUserRole = adminUserEntity.getRoleModel().getRoleName().toLowerCase();
					if (loginUserRole.equals("admin") || loginUserRole.equals("manager")) {
						RegionEntity regionId = new RegionEntity();
						RepairingOptionEntity repairOption = new RepairingOptionEntity();
						TechnicianStatusEntity technicianStatus = new TechnicianStatusEntity();
						LocalDate startDate = null;
						LocalDate endDate = null;

						// region section
						if (loginUserRole.equals("admin")) {
							// this section is for getting admin region
							if (ticketDetailsRequest.getRegion() == null || (ticketDetailsRequest.getRegion() != null
									&& ticketDetailsRequest.getRegion().trim().length() < 1)) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("Region is required for admin");
							} else {
								regionId = regionDAO.findByCity(ticketDetailsRequest.getRegion().trim().toLowerCase());

								if (regionId == null) {
									return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Region with this city "
											+ ticketDetailsRequest.getRegion().trim() + "  is not listed");
								}
							}
						} else {// this section is for manager region
							regionId = adminUserEntity.getRegionEntity();
							if (regionId == null) {
								return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
										.body("No any region assign to you");
							}
						}

						// this section is for technician assign details
						if (ticketDetailsRequest.getTechnicianStatus() != null
								&& ticketDetailsRequest.getTechnicianStatus().trim().length() > 0) {
							technicianStatus = technicianStatusDAO.getTechnicianStatus(
									ticketDetailsRequest.getTechnicianStatus().trim().toLowerCase());
							if (technicianStatus == null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("Not a valid technician assign option");
							}
						}

						// starting and end date
						LocalDate today = LocalDate.now();
						if (ticketDetailsRequest.getStartingDate() != null) {
							if (ticketDetailsRequest.getStartingDate().isAfter(today)) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("Starting date can not be after Date");
							} else {
								startDate = ticketDetailsRequest.getStartingDate();
							}
						} else {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Starting date is required");
						}

						// end date
						if (ticketDetailsRequest.getEndDate() != null) {
							if (ticketDetailsRequest.getEndDate().isAfter(today)) {
								endDate = today;
							} else {
								endDate = ticketDetailsRequest.getEndDate();
							}
						} else {
							endDate = today;
						}

						// this section is for getting repairing option
						if (ticketDetailsRequest.getRepairStatus() != null
								&& ticketDetailsRequest.getRepairStatus().trim().length() > 0) {
							repairOption = repairingOptionDAO
									.getOptionDetails(ticketDetailsRequest.getRepairStatus().trim().toLowerCase());
							if (repairOption == null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("This repairing option is not listed");
							}
						}

						Specification<ItemRepairingEntity> specification = itemRepairingDetilsDynamicQueryForManager
								.getItemRepairingDetailsQuery(regionId, startDate, endDate, technicianStatus,
										repairOption);

						CriteriaBuilder cb = entityManager.getCriteriaBuilder();
						CriteriaQuery<ItemRepairingEntityDTO> cq = cb.createQuery(ItemRepairingEntityDTO.class); // Use
																													// the
																													// projection
																													// interface
						Root<ItemRepairingEntity> root = cq.from(ItemRepairingEntity.class);

						cq.where(specification.toPredicate(root, cq, cb)); // Apply predicates

						// Select the constructor of the projection interface.
						cq.select(cb.construct(ItemRepairingEntityDTO.class, root.get("id"),
								root.get("faultDetails").alias("faultDetail"),
								root.join("repairStatus").get("statusOption").alias("repairStatus"),
								root.get("serialNo"), root.get("rmaNo"),
								root.join("itemDetailId").get("system").alias("system"),
								root.join("itemDetailId").join("keywordEntity").get("keywordName").alias("keywordName"),
								root.join("itemDetailId").join("subKeyWordEntity").get("subKeyword")
										.alias("subKeywordName"),
								root.join("technicianStatus").get("technicianAssign").alias("technicianStatus"),
								root.get("generatedDate"), root.join("itemDetailId").get("partNo").alias("partNo"),
								root.get("repairingRemark").alias("faultRemark")

						));
						List<ItemRepairingEntityDTO> itemDTOList = new ArrayList<ItemRepairingEntityDTO>();
						itemDTOList = entityManager.createQuery(cq).getResultList();
						if (itemDTOList != null && !itemDTOList.isEmpty()) {
							itemDTOList.sort((e1, e2) -> e2.getGeneratedDate().compareTo(e1.getGeneratedDate()));
							return ResponseEntity.status(HttpStatus.OK).body(itemDTOList);
						} else {
							return ResponseEntity.status(HttpStatus.NOT_FOUND)
									.body("No data listed, with your specific search");
						}
//						
						// end of admin manager check
					} else {
						return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
								.body("only admin and manager can access this section");
					}
				}
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not registered");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
		}

	}

	// get assign ticket to technician on date basis by default show all
	public ResponseEntity<?> getAssignTicket(HttpServletRequest httpRequest, ItemRepairingRequest repairingRequest)
			throws Exception {

		// call service class for generate request
		return null;
	}

	// get list of assign ticket by starting and end date and also ticket status
	// resolve or pending
	// Done
	public ResponseEntity<?> getRepairingRequest(HttpServletRequest request,
			GetAssignTicketDetailsRequest ticketDetailsRequest) throws Exception {
		String loggedInUserName = null;
		try {
			loggedInUserName = request.getUserPrincipal().getName().toLowerCase();
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
		}
		try {
			if (loggedInUserName != null && loggedInUserName.length() > 0) {
				adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
				if (adminUserEntity == null) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Login");
				} else {
					String loginUserRole = adminUserEntity.getRoleModel().getRoleName().toLowerCase();
					if (loginUserRole.equals("employee") || loginUserRole.equals("manager")) {
						RegionEntity regionId = new RegionEntity();
						RepairingOptionEntity repairOption = new RepairingOptionEntity();
						LocalDate startDate = null;
						LocalDate endDate = null;

						regionId = adminUserEntity.getRegionEntity();
						if (regionId == null) {
							return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No any region assign to you");
						}

						// starting and end date
						LocalDate today = LocalDate.now();
						if (ticketDetailsRequest.getStartingDate() != null) {
							if (ticketDetailsRequest.getStartingDate().isAfter(today)) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("Starting date can not be after Date");
							} else {
								startDate = ticketDetailsRequest.getStartingDate();
							}
						} else {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Starting date is required");
						}

						// end date
						if (ticketDetailsRequest.getEndDate() != null) {
							if (ticketDetailsRequest.getEndDate().isAfter(today)) {
								endDate = today;
							} else {
								endDate = ticketDetailsRequest.getEndDate();
							}
						} else {
							endDate = today;
						}

						// this section is for getting repairing option
						if (ticketDetailsRequest.getRepairStatus() != null
								&& ticketDetailsRequest.getRepairStatus().trim().length() > 0) {
							repairOption = repairingOptionDAO
									.getOptionDetails(ticketDetailsRequest.getRepairStatus().trim().toLowerCase());
							if (repairOption == null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("This repairing option is not listed");
							}
						}

						Specification<ItemRepairingEntity> specification = itemRepairingDetilsDynamicQueryForManager
								.getAssignTicketQuery(regionId, startDate, endDate,
										adminUserEntity.getEmail().trim().toLowerCase(), repairOption,
										ticketDetailsRequest.getRmaNo(), ticketDetailsRequest.getTicketId(),
										ticketDetailsRequest.getSerialNo());

						CriteriaBuilder cb = entityManager.getCriteriaBuilder();
						CriteriaQuery<ItemRepairingEntityDTO> cq = cb.createQuery(ItemRepairingEntityDTO.class); // Use
																													// the
																													// projection
																													// interface
						Root<ItemRepairingEntity> root = cq.from(ItemRepairingEntity.class);

						cq.where(specification.toPredicate(root, cq, cb)); // Apply predicates

						// Select the constructor of the projection interface.
						cq.select(cb.construct(ItemRepairingEntityDTO.class, root.get("id"),
								root.get("faultDetails").alias("faultDetail"),
								root.join("repairStatus").get("statusOption").alias("repairStatus"),
								root.get("serialNo"), root.get("rmaNo"),
								root.join("itemDetailId").get("system").alias("system"),
								root.join("itemDetailId").join("keywordEntity").get("keywordName").alias("keywordName"),
								root.join("itemDetailId").join("subKeyWordEntity").get("subKeyword")
										.alias("subKeywordName"),
								root.get("generatedDate"), root.join("itemDetailId").get("partNo").alias("partNo"),
								root.get("repairingRemark").alias("faultRemark"),
								root.get("assignByManager").alias("assignByManager"),
								root.get("tech_Assign_Date").alias("techAssignDate"),
								root.get("lastUpdateDate").alias("lastUpdateDate")

						));
						List<ItemRepairingEntityDTO> itemDTOList = new ArrayList<ItemRepairingEntityDTO>();
						itemDTOList = entityManager.createQuery(cq).getResultList();
						if (itemDTOList != null && !itemDTOList.isEmpty()) {
							itemDTOList.sort((e1, e2) -> e2.getTechAssignDate().compareTo(e1.getTechAssignDate()));
							return ResponseEntity.status(HttpStatus.OK).body(itemDTOList);
						} else {

							return ResponseEntity.status(HttpStatus.NOT_FOUND)
									.body("No ticket assign to you, under this specific filter");
						}
//						
						// end of admin manager check
					} else {
						return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This module is not for admin role");
					}
				}
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not registered");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
		}
	}

	// function to provide item repairing list via serialno
	public ResponseEntity<?> getRepairHistoryViaSerial(HttpServletRequest httpRequest, String serialNo) {

		String loggedInUserName = null;
		try {
			loggedInUserName = httpRequest.getUserPrincipal().getName().toLowerCase();
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
		}
		try {
			if (loggedInUserName != null && loggedInUserName.length() > 0) {
				adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
				if (adminUserEntity == null) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User details not found");
				} else {
					if (serialNo == null || (serialNo != null && serialNo.trim().length() < 0)) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Serial no is required");
					}
					List<ItemRepairingEntity> itemRepairingEntityList = itemRepairingDAO
							.getRepairingTicketViaSerialNo(serialNo.trim().toLowerCase());
					if (itemRepairingEntityList != null && !itemRepairingEntityList.isEmpty()) {
						return ResponseEntity.status(HttpStatus.OK).body(itemRepairingEntityList);
					} else {
						return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No data available");
					}
				}
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not loggedIn");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
		}
	}

	// get Ticket Details via ticket id
	// Done
	public ResponseEntity<?> getTicketDetailsViaId(HttpServletRequest httpRequest, Long ticketId) {

		String loggedInUserName = null;
		try {
			loggedInUserName = httpRequest.getUserPrincipal().getName().toLowerCase();
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
		}
		try {
			if (loggedInUserName != null && loggedInUserName.length() > 0) {
				adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
				if (adminUserEntity == null) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User details not found");
				} else {
					if (ticketId == null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ticket id is required");
					}
					if (adminUserEntity.getRoleModel().getRoleName().trim().toLowerCase().equals("employee")) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("This section only for admin and manager");
					}
					if (!adminUserEntity.getRoleModel().getRoleName().trim().toLowerCase().equals("admin")) {
						// check user region and ticket region id role is other that admin
						boolean isSame = itemRepairingDAO.belongingToSameRegion(adminUserEntity.getRegionEntity());
						if (!isSame) {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									.body("This Ticket not belonging to your region");
						}
					}
					TicketDetailsViaIdDTO ticketDetails = itemRepairingDAO.getRepairingTicketViaTicketIdInDTO(ticketId);
					if (ticketDetails != null) {
						// check for employee and manager region
						if (adminUserEntity.getRoleModel().getRoleName().trim().toLowerCase().equals("manager")) {

							if (ticketDetails.getAssignByManager() != null
									&& ticketDetails.getAssignByManager().trim().length() > 0) {
								if (!ticketDetails.getAssignByManager().trim().toLowerCase()
										.equals(adminUserEntity.getEmail().trim().toLowerCase())) {
									return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This ticket Belong to "
											+ ticketDetails.getAssignByManager() + " this manager");
								}
							}

						}

						// this is for manager and admin region to set employee list with details
						if (adminUserEntity.getRoleModel().getRoleName().trim().toLowerCase().equals("admin")
								|| adminUserEntity.getRoleModel().getRoleName().trim().toLowerCase()
										.equals("manager")) {
							RegionEntity regionId = regionDAO
									.findByCity(ticketDetails.getRegionName().trim().toLowerCase());
							if (regionId == null || regionId.getId() == null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("Region with this name is not listed in our database");
							}
							List<RoleEntity> roleEntityList = new ArrayList<>();
							if (adminUserEntity.getRoleModel().getRoleName().trim().toLowerCase().equals("admin")) {
								RoleEntity managerRole = roleDAO.findByName("manager");
								if (managerRole != null) {
									roleEntityList.add(managerRole);
								}
								RoleEntity employeeRole = roleDAO.findByName("employee");
								if (employeeRole != null) {
									roleEntityList.add(employeeRole);
								}
							} else {
								RoleEntity employeeRole = roleDAO.findByName("employee");
								if (employeeRole != null) {
									roleEntityList.add(employeeRole);
								}
							}
							List<String> employeeList = adminUserDAO.getEmployeeList(regionId, roleEntityList);
							if (employeeList != null && !employeeList.isEmpty()) {
								ticketDetails.setEmployeeList(employeeList);
							}
							return ResponseEntity.status(HttpStatus.OK).body(ticketDetails);
						}

						// sending from their only if user is employee other that manager or admin
						return ResponseEntity.status(HttpStatus.OK).body(ticketDetails);
					} else {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Not find any ticket with this ticket id");
					}
				}

			} else {
				// user not login unathorized access
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not LoggedIn");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
		}
	}

	// get Assign Ticket Details via ticket id
	// Done
	public ResponseEntity<?> getAssignTicketDetailsViaId(HttpServletRequest httpRequest, Long ticketId) {

		String loggedInUserName = null;
		try {
			loggedInUserName = httpRequest.getUserPrincipal().getName().toLowerCase();
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
		}
		try {
			if (loggedInUserName != null && loggedInUserName.length() > 0) {
				adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
				if (adminUserEntity == null) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User details not found");
				} else {
					if (ticketId == null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ticket id is required");
					}
					if (adminUserEntity.getRoleModel().getRoleName().trim().toLowerCase().equals("admin")) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("this is not made for admin ");
					}
					// check user region and ticket region id role is other that admin
					if (adminUserEntity.getRegionEntity() == null
							&& adminUserEntity.getRegionEntity().getId() == null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Do not have assign any region to you");
					}
					boolean isSame = itemRepairingDAO.belongingToSameRegion(adminUserEntity.getRegionEntity());
					if (!isSame) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("This Ticket not belonging to your region");
					}
					TicketDetailsViaIdDTO ticketDetails = itemRepairingDAO.getRepairingTicketViaTicketIdInDTO(ticketId);
					if (ticketDetails != null) {

						if (ticketDetails.getTechnician_Name() != null
								&& ticketDetails.getTechnician_Name().trim().length() > 0) {
							if (!ticketDetails.getTechnician_Name().equals(loggedInUserName.toLowerCase())) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("This ticket not belongs to you");
							}
						}

						// sending from their only if user is employee other that manager or admin
						return ResponseEntity.status(HttpStatus.OK).body(ticketDetails);
					} else {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Not find any ticket with this ticket id");
					}
				}
			} else {
				// user not login unathorized access
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not LoggedIn");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
		}
	}

	// update assign ticket via id
	// Done
	public ResponseEntity<?> updateAssignTicketDetailsViaId(HttpServletRequest httpRequest,
			EngineerUpdateTicketStatusRequest updateDetails) {

		String loggedInUserName = null;
		try {
			loggedInUserName = httpRequest.getUserPrincipal().getName().toLowerCase();
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
		}
		try {
			if (loggedInUserName != null && loggedInUserName.length() > 0) {
				adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
				if (adminUserEntity == null) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User details not found");
				} else {
					if (updateDetails.getId() == null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ticket id is required");
					}
					if (adminUserEntity.getRoleModel().getRoleName().trim().toLowerCase().equals("admin")) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This is not made for admin ");
					}
					// check user region and ticket region id role is other that admin
					if (adminUserEntity.getRegionEntity() == null
							&& adminUserEntity.getRegionEntity().getId() == null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Do not have assign any region to you");
					}
					boolean isSame = itemRepairingDAO.belongingToSameRegion(adminUserEntity.getRegionEntity());
					if (!isSame) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("This Ticket not belonging to your region");
					}
					ItemRepairingEntity ticketDetails = itemRepairingDAO
							.getRepairingTicketViaTicketId(updateDetails.getId());
					if (ticketDetails != null) {

						if (ticketDetails.getTechnician_Name() != null
								&& ticketDetails.getTechnician_Name().trim().length() > 0) {
							if (!ticketDetails.getTechnician_Name().equals(loggedInUserName.toLowerCase())) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("This ticket not belongs to you");
							}
						}
						if (ticketDetails.getRepairStatus().getStatusOption().equals("repaired")
								|| ticketDetails.getRepairStatus().getStatusOption().equals("replaced")) {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									.body("This TicketId:- " + updateDetails.getId() + "  already closed.");
						}

						if (updateDetails.getTicketStatus() == null || (updateDetails.getTicketStatus() != null
								&& updateDetails.getTicketStatus().trim().length() < 1)) {

							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									.body("repairing Status is required");
						} else {
							
							if(updateDetails.getTicketStatus().trim().toLowerCase().equals("pending")) {
								if(!ticketDetails.getRepairStatus().getStatusOption().equals("pending")){
									return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You can not revert back status");
								}
							}
							if(updateDetails.getTicketStatus().trim().toLowerCase().equals("working")) {
								if(ticketDetails.getRepairStatus().getStatusOption().equals("repaired") || ticketDetails.getRepairStatus().getStatusOption().equals("replace")){
									return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You can not revert back status");
								}
							}
							
							RepairingOptionEntity repairOption = repairingOptionDAO
									.getOptionDetails(updateDetails.getTicketStatus().toLowerCase());

							if (repairOption == null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("This repairing status is not listed before");
							}
							if(repairOption.getStatusOption().trim().toLowerCase().equals("repaired")|| repairOption.getStatusOption().trim().toLowerCase().equals("replaced")) {
								ItemDetailsEntity itemDetail=itemDetailsDAO.getItemDetailsBySerialNo(ticketDetails.getSerialNo().trim().toLowerCase());
								if(itemDetail==null) {
									return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Item with this serial number not available in our main table");
								}else {
									ItemHistoryUpdatedByAdminEntity historyChangedByAdmin = new ItemHistoryUpdatedByAdminEntity();
									historyChangedByAdmin.setSerial_No(ticketDetails.getSerialNo().trim().toLowerCase());
									ItemAvailableStatusOptionEntity availableStatusEntity=itemAvailableStatusDAO.getStatusDetailsByOption("available");
									if(availableStatusEntity !=null) {
									itemDetail.setAvailableStatusId(availableStatusEntity);
									historyChangedByAdmin.setAvailableStatusId(availableStatusEntity);
									}else {
										return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("'Available' status is not listed in item available option list");
									}
									
									ItemStatusOptionEntity itemStatusOptionEntity=itemStatusOptionDAO.getItemStatusOptionDetails("repaired");
									if(itemStatusOptionEntity !=null) {
										itemDetail.setItemStatusId(itemStatusOptionEntity);
										historyChangedByAdmin.setItemStatusId(itemStatusOptionEntity);
										}else {
											return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("'Repaired' status is not listed in item status option list");
										}
																		
									historyChangedByAdmin.setUpdatedByEmail(loggedInUserName);

									itemDetail.setEmpEmail(loggedInUserName);// updated by

									itemDetail.setAdding_Date(ZonedDateTime.now());
									historyChangedByAdmin.setUpdate_Date(ZonedDateTime.now());

									itemDetail.setUpdate_Date(ZonedDateTime.now());

									if (historyChangedByAdmin != null) {
										// setting table reference for Admin history table
										historyChangedByAdmin.setItemDetailsEntity(itemDetail);
										List<ItemHistoryUpdatedByAdminEntity> historyChangedByAdminList = new ArrayList<ItemHistoryUpdatedByAdminEntity>();
										historyChangedByAdminList.add(historyChangedByAdmin);
										// add reference for admin history table
										itemDetail.setItemHistoryUpdatedByAdminEntityList(historyChangedByAdminList);
									}
									
									itemDetail=itemDetailsDAO.save(itemDetail);
									if(itemDetail==null) {
										return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong, when update item main table details");
									}
									
								}
							}
							ticketDetails.setRepairStatus(repairOption);

							ticketDetails.setLastUpdateDate(LocalDate.now());
						}

						ticketDetails = itemRepairingDAO.save(ticketDetails);

						if (ticketDetails != null) {
							return ResponseEntity.status(HttpStatus.OK).body("update successfully");
						} else {
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("something went wrong");
						}

						// sending from their only if user is employee other that manager or admin

					} else {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Not find any ticket with this ticket id");
					}
				}
			} else {
				// user not login unathorized access
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not LoggedIn");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
		}
	}

}
