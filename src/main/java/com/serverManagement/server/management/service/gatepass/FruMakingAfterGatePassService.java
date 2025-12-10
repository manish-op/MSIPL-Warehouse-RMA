package com.serverManagement.server.management.service.gatepass;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.gatepass.InwardGatePassDAO;
import com.serverManagement.server.management.dao.gatepass.OutwardGatepassDAO;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
import com.serverManagement.server.management.dao.itemRepairDetails.FruDAO;
import com.serverManagement.server.management.dao.itemRepairOption.RepairingOptionDAO;
import com.serverManagement.server.management.dao.itemRepairOption.TechnicianStatusDAO;
import com.serverManagement.server.management.dao.itemRepairOption.WarrantyOptionDAO;
import com.serverManagement.server.management.dao.keyword.KeywordDAO;
import com.serverManagement.server.management.dao.keyword.SubKeywordDAO;
import com.serverManagement.server.management.dao.option.ItemAvailableStatusOptionDAO;
import com.serverManagement.server.management.dao.option.ItemStatusOptionDAO;
import com.serverManagement.server.management.dao.region.RegionDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.gatePass.InwardGatePassEntity;
import com.serverManagement.server.management.entity.gatePass.ItemListViaGatePassInward;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.itemDetails.ItemHistoryUpdatedByAdminEntity;
import com.serverManagement.server.management.entity.itemRepairDetails.FruEntity;
import com.serverManagement.server.management.entity.itemRepairDetails.ItemRepairingEntity;
import com.serverManagement.server.management.entity.itemRepairOption.RepairingOptionEntity;
import com.serverManagement.server.management.entity.itemRepairOption.TechnicianStatusEntity;
import com.serverManagement.server.management.entity.itemRepairOption.WarrantyOptionEntity;
import com.serverManagement.server.management.entity.keyword.KeywordEntity;
import com.serverManagement.server.management.entity.keyword.SubKeywordEntity;
import com.serverManagement.server.management.entity.options.ItemAvailableStatusOptionEntity;
import com.serverManagement.server.management.entity.options.ItemStatusOptionEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;
import com.serverManagement.server.management.request.gatepass.AfterGatePassFruMakingRequest;
import com.serverManagement.server.management.request.gatepass.InwardGatepassItemList;
import com.serverManagement.server.management.service.gatepasspdf.AfterGatepassFruTicketPringtPDF;
import com.serverManagement.server.management.service.gatepasspdf.GatepassPassService;
import com.serverManagement.server.management.service.gatepasspdf.OutwardGatepassPDFService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class FruMakingAfterGatePassService {

	@Autowired
	private ItemDetailsDAO itemDetailsDAO;
	@Autowired
	private AdminUserDAO adminUserDAO;
	@Autowired
	private ItemAvailableStatusOptionDAO itemAvailableStatusDAO;
	@Autowired
	private FruDAO fruDAO;
	@Autowired
	private RepairingOptionDAO repairingOptionDAO;
	@Autowired
	private TechnicianStatusDAO technicianStatusDAO;
	@Autowired
	private WarrantyOptionDAO warrantyOptionDAO;
	@Autowired
	private InwardGatePassDAO inwardGatePassDAO;
	@Autowired
	private AfterGatepassFruTicketPringtPDF afterGatepassFruTicketPringtPDF;

	private AdminUserEntity adminUserEntity;

	
	@Transactional(rollbackFor = Exception.class)
	public ResponseEntity<?> createTicketAfterGatepass(HttpServletRequest httpRequest, AfterGatePassFruMakingRequest fruRequest){
		String loggedInUserName = null;

		try {
			loggedInUserName = httpRequest.getUserPrincipal().getName();
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
					
					if(fruRequest==null || fruRequest.getFruSerialNo()==null ||
							(fruRequest.getFruSerialNo()!=null && fruRequest.getFruSerialNo().trim().length()<1) ||
							fruRequest.getRmaNo()==null ||
							(fruRequest.getRmaNo()!=null && fruRequest.getRmaNo().trim().length()<1)) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("SerialNo and RmaNo both required");
					}
					
					ItemDetailsEntity itemDetails=itemDetailsDAO.getItemDetailsBySerialNo(fruRequest.getFruSerialNo().trim().toLowerCase());
					
					if(itemDetails==null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No item available with this SerialNo");
					}
					//check manager and employee region with item region
					if(!loginUserRole.equals("admin")) {
						if(adminUserEntity.getRegionEntity() ==null || adminUserEntity.getRegionEntity().getId()== null) {
							return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No region assign to you");
						}
						if(!adminUserEntity.getRegionEntity().equals(itemDetails.getRegion())){
							return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This item not belongs to your region");
						}
					}
					
					if(itemDetails.getAvailableStatusId().getItemAvailableOption().trim().toLowerCase().equals("repairing")){
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This item already under repairing state ");
					}
					
					if(!itemDetails.getAvailableStatusId().getItemAvailableOption().trim().toLowerCase().equals("available")){
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This item is not in available state");
					}
					
					
							FruEntity fruConfirming = fruDAO.getRmaConfirmation(
									fruRequest.getRmaNo().trim().toLowerCase(),
									itemDetails.getRegion());
							if (fruConfirming != null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("This Rma No: " + fruRequest.getRmaNo().trim().toLowerCase()
												+ " is already registered");
							}

							FruEntity fruTable = new FruEntity();
							fruTable.setRmaNo(fruRequest.getRmaNo().trim().toLowerCase());
							InwardGatePassEntity inwardGatePass=inwardGatePassDAO.getInwardPassBySerial(itemDetails.getSerial_No().trim().toLowerCase());
							if(inwardGatePass==null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No inward gate pass available for this serialNo");
							}else {
								fruTable.setInGatepassID(inwardGatePass);
							}
							
							fruTable.setCreatedDate(LocalDate.now());
							fruTable.setRegionDetails(itemDetails.getRegion());

							ItemRepairingEntity itemInformation = new ItemRepairingEntity();
							itemInformation.setItemDetailId(itemDetails);
							itemInformation.setRmaNo(fruRequest.getRmaNo().trim().toLowerCase());
							itemInformation.setSerialNo(itemDetails.getSerial_No());
							itemInformation.setFaultDetails(fruRequest.getFaultDescription());
							itemInformation.setFaultRemark(fruRequest.getFaultRemark());
							//itemInformation.setDocketIdInward(inwardGatePass.getItemList());
							itemInformation.setFruId(fruTable);
							itemInformation.setGeneratedDate(LocalDate.now());
							itemInformation.setInwardGatepass(inwardGatePass);
							itemInformation.setLastUpdateDate(LocalDate.now());
							itemInformation.setRegion(itemDetails.getRegion());
							itemInformation.setPartNo(itemDetails.getPartNo());
							itemInformation.setTicketGeneratedBy(loggedInUserName.toLowerCase());

							// this section is for getting repairing option details from table
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
							itemInformation.setRepairStatus(repairOption);

							// this section is creating technicianStatus pending/Assign
							TechnicianStatusEntity technicianStatus = technicianStatusDAO
									.getTechnicianStatus("pending");

							if (technicianStatus == null) {
								TechnicianStatusEntity newTechnicianStatus = new TechnicianStatusEntity();
								newTechnicianStatus.setTechnicianAssign("pending");
								newTechnicianStatus = technicianStatusDAO.save(newTechnicianStatus);
								if (newTechnicianStatus == null) {
									return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
											.body("Getting error when creating technician Status pending");
								} else {
									technicianStatus = newTechnicianStatus;
								}
							}
							itemInformation.setTechnicianStatus(technicianStatus);

							// this section is for getting warranty option details from table
							if (fruRequest.getWarrantyDetails() == null
									|| fruRequest.getWarrantyDetails() != null
											&& fruRequest.getWarrantyDetails().trim().length() <= 0) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("Warranty Details is required for item that have Rma no");
							}
							WarrantyOptionEntity warrantyOption = warrantyOptionDAO
									.getWarrantyOption(fruRequest.getWarrantyDetails().toLowerCase());
							if (warrantyOption == null) {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("This warranty option is not listed before");
							}
							itemInformation.setWarrantyDetails(warrantyOption);

							fruTable.setRepairingIdList(itemInformation);							
							// adding fru into fruList for adding all at a time
							
							ItemHistoryUpdatedByAdminEntity historyChangedByAdmin = new ItemHistoryUpdatedByAdminEntity();
							
							ItemAvailableStatusOptionEntity itemAvailEntity = itemAvailableStatusDAO
									.getStatusDetailsByOption("repairing");
							if (itemAvailEntity != null) {
								itemDetails.setAvailableStatusId(itemAvailEntity);
								historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
							} else {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("REPAIRING Status is not listed in our database add this first");
							}
							
							historyChangedByAdmin.setUpdatedByEmail(loggedInUserName);

							itemDetails.setEmpEmail(loggedInUserName);// updated by

							//itemDetails.setAdding_Date(ZonedDateTime.now());
							historyChangedByAdmin.setUpdate_Date(ZonedDateTime.now());

							itemDetails.setUpdate_Date(ZonedDateTime.now());
							
							historyChangedByAdmin.setSerial_No(itemDetails.getSerial_No());

							if (historyChangedByAdmin != null) {
								// setting table reference for Admin history table
								historyChangedByAdmin.setItemDetailsEntity(itemDetails);
								List<ItemHistoryUpdatedByAdminEntity> historyChangedByAdminList = new ArrayList<ItemHistoryUpdatedByAdminEntity>();
								historyChangedByAdminList.add(historyChangedByAdmin);
								// add reference for admin history table
								itemDetails.setItemHistoryUpdatedByAdminEntityList(historyChangedByAdminList);
							}
							
							itemDetails=itemDetailsDAO.save(itemDetails);
							fruTable = fruDAO.save(fruTable);// save repairing request table
							if(itemDetails!=null && fruTable==null) {
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
										.body("Something went wrong");
							}else {
								
								return afterGatepassFruTicketPringtPDF.generatePdf(fruTable);
							}
				}
				
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not LoggedIn");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Erro");
		}
	}
}
