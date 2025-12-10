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
import com.serverManagement.server.management.entity.gatePass.ItemListViaGatePassOutwardEntity;
import com.serverManagement.server.management.entity.gatePass.OutwardGatepassEntity;
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
import com.serverManagement.server.management.request.gatepass.InwardGatepassItemList;
import com.serverManagement.server.management.request.gatepass.InwardGatepassRequest;
import com.serverManagement.server.management.request.gatepass.OutwardGatepassItemList;
import com.serverManagement.server.management.request.gatepass.OutwardGatepassRequest;
import com.serverManagement.server.management.service.gatepasspdf.GatepassPassService;
import com.serverManagement.server.management.service.gatepasspdf.OutwardGatepassPDFService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class InwardGatePassService {

    @Autowired
    private ItemDetailsDAO itemDetailsDAO;
    @Autowired
    private AdminUserDAO adminUserDAO;
    @Autowired
    private RegionDAO regionDAO;
    @Autowired
    private KeywordDAO keywordDAO;
    @Autowired
    private SubKeywordDAO subKeywordDAO;
    @Autowired
    private ItemAvailableStatusOptionDAO itemAvailableStatusDAO;
    @Autowired
    private ItemStatusOptionDAO itemStatusOptionDAO;
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
    private OutwardGatepassDAO outwardGatepassDAO;
    @Autowired
    private GatepassPassService gatepassPassService;
    @Autowired
    private OutwardGatepassPDFService outwardGatepassPDFService;

    private AdminUserEntity adminUserEntity;
    private RegionEntity regionEntity;
    private KeywordEntity keywordEntity;
    private SubKeywordEntity subKeywordEntity;
    private ItemDetailsEntity itemDetailsEntity;
    private ItemStatusOptionEntity itemStatusOptionEntity;

    public ResponseEntity<?> testPdf(Long id) throws Exception {
        try {
            InwardGatePassEntity pass = inwardGatePassDAO.getById(id);
            if (pass != null) {
                if (!pass.getFruList().isEmpty()) {
                    return gatepassPassService.generateInvoicePdf(pass, pass.getFruList());
                } else {
                    return gatepassPassService.generateInvoicePdf(pass, pass.getFruList());
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("wrong");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    // Generate Inward GatePass
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> generateInwardGatePass(HttpServletRequest httpRequest, InwardGatepassRequest passRequest) {

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
                    if (passRequest == null || passRequest.getPartyName() == null || (passRequest.getPartyName() != null && passRequest.getPartyName().trim().length() < 1) || passRequest.getPartyAddress() == null || (passRequest.getPartyAddress() != null && passRequest.getPartyAddress().trim().length() < 1) || passRequest.getItemList() == null || (passRequest.getItemList() != null && passRequest.getItemList().isEmpty())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("party name, Address and Atleast 1 item is required for gatepass");
                    } else {
                        InwardGatePassEntity newGatePassInward = new InwardGatePassEntity();
                        newGatePassInward.setPartyName(passRequest.getPartyName());
                        newGatePassInward.setPartyAddress(passRequest.getPartyAddress());
                        newGatePassInward.setCreatedBy(loggedInUserName);
                        newGatePassInward.setCreatedDate(LocalDate.now());
                        if (passRequest.getPartyContact() != null && passRequest.getPartyContact().trim().length() >= 9) {
                            newGatePassInward.setPartyContact(passRequest.getPartyContact().trim());
                        }

                        if (loginUserRole.equals("admin")) {
                            if (passRequest.getRegion() != null && passRequest.getRegion().trim().length() > 0) {
                                RegionEntity regionDetails = regionDAO.findByCity(passRequest.getRegion().trim().toLowerCase());
                                if (regionDetails != null) {
                                    newGatePassInward.setRegionDetails(regionDetails);
                                } else {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Provided region not listed in our database");
                                }
                            } else {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Region is required for admin role");
                            }
                        } else {
                            // user region assign to gate pass
                            if (adminUserEntity.getRegionEntity() != null && adminUserEntity.getRegionEntity().getId() != null) {

                                newGatePassInward.setRegionDetails(adminUserEntity.getRegionEntity());
                            } else {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No region assign to you");
                            }
                        }
                        List<ItemDetailsEntity> itemList = new ArrayList<ItemDetailsEntity>();
                        List<FruEntity> fruList = new ArrayList<FruEntity>();
                        List<ItemListViaGatePassInward> gatepassItemList = new ArrayList<ItemListViaGatePassInward>();
                        for (InwardGatepassItemList gatepassItem : passRequest.getItemList()) {
                            if (gatepassItem.getSerialNo() == null || (gatepassItem.getSerialNo() != null && gatepassItem.getSerialNo().trim().length() <= 0)) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("serial number for every item is required");
                            }
                            ItemDetailsEntity itemDetail = itemDetailsDAO.getComponentDetailsBySerialNo(gatepassItem.getSerialNo().trim().toLowerCase());
                            ItemHistoryUpdatedByAdminEntity historyChangedByAdmin = new ItemHistoryUpdatedByAdminEntity();
                            if (itemDetail == null) {
                                ItemDetailsEntity item = new ItemDetailsEntity();
                                itemDetail = item;
                                itemDetail.setSerial_No(gatepassItem.getSerialNo().trim().toLowerCase());
                                historyChangedByAdmin.setSerial_No(gatepassItem.getSerialNo().trim().toLowerCase());

                                // partyName
                                itemDetail.setPartyName(newGatePassInward.getPartyName());
                                historyChangedByAdmin.setPartyName(newGatePassInward.getPartyName());

                                if (gatepassItem.getModuleFor() != null && gatepassItem.getModuleFor().trim().length() > 0) {

                                    itemDetail.setModuleFor(loginUserRole);
                                    historyChangedByAdmin.setModuleFor(loginUserRole);
                                }

                                if (gatepassItem.getPartNo() != null && gatepassItem.getPartNo().trim().length() > 0) {

                                    itemDetail.setPartNo(gatepassItem.getPartNo().trim().toLowerCase());
                                    historyChangedByAdmin.setPartNo(gatepassItem.getPartNo().trim().toLowerCase());
                                }

                                if (gatepassItem.getModeuleVersion() != null && gatepassItem.getModuleFor().trim().length() > 0) {

                                    itemDetail.setSystem_Version(loginUserRole);
                                    historyChangedByAdmin.setSystem_Version(loginUserRole);
                                }
                                if (gatepassItem.getSpareLocation() != null && gatepassItem.getSpareLocation().trim().length() > 0) {

                                    itemDetail.setSpare_Location(gatepassItem.getSpareLocation());
                                    historyChangedByAdmin.setSpare_Location(gatepassItem.getSpareLocation());
                                }
                                if (gatepassItem.getSystemName() != null && gatepassItem.getSystemName().trim().length() > 0) {
                                    itemDetail.setSystem(loginUserRole);
                                    historyChangedByAdmin.setSystem(loginUserRole);
                                }
                                if (gatepassItem.getRackNo() != null && gatepassItem.getRackNo().trim().length() > 0) {
                                    itemDetail.setRack_No(gatepassItem.getRackNo());
                                    historyChangedByAdmin.setRack_No(gatepassItem.getRackNo());
                                }
                                if (gatepassItem.getRemark() != null && gatepassItem.getRemark().trim().length() > 0) {
                                    itemDetail.setRemark(gatepassItem.getRemark());
                                    historyChangedByAdmin.setRemark(gatepassItem.getRemark());
                                }

                                // section for item Status new, old or repair
                                if (gatepassItem.getItemStatus() != null && gatepassItem.getItemStatus().trim().length() > 0) {

                                    itemStatusOptionEntity = itemStatusOptionDAO.getItemStatusOptionDetails(gatepassItem.getItemStatus().toLowerCase());
                                    if (itemStatusOptionEntity != null) {
                                        itemDetail.setItemStatusId(itemStatusOptionEntity);
                                        historyChangedByAdmin.setItemStatusId(itemStatusOptionEntity);
                                    } else {
                                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("this item status is not listed add another one");
                                    }

                                } // Item status new,old or repair closed

                                // check keyword and sub keyword section for adding
                                if (gatepassItem.getKeywordName() == null && gatepassItem.getKeywordName().trim().length() < 1) {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Keyword is required for adding item first time :-" + gatepassItem.getSerialNo());
                                } else {
                                    keywordEntity = keywordDAO.getByKeyword(gatepassItem.getKeywordName().toLowerCase());
                                    if (keywordEntity != null) {
                                        itemDetail.setKeywordEntity(keywordEntity);
                                        historyChangedByAdmin.setKeywordEntity(keywordEntity);
                                        // check sub keyword if keyword exist
                                        if (gatepassItem.getSubkeywordName() != null && gatepassItem.getSubkeywordName().trim().length() > 0) {
                                            subKeywordEntity = subKeywordDAO.getSpecificSubKeyword(keywordEntity, gatepassItem.getSubkeywordName().toLowerCase());
                                            if (subKeywordEntity != null) {
                                                itemDetail.setSubKeyWordEntity(subKeywordEntity);
                                                historyChangedByAdmin.setSubKeyWordEntity(subKeywordEntity);
                                            }
                                        }
                                    } else {
                                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("keyword is new, First add this keyword");
                                    }
                                } // Keyword Section closed


                                ItemAvailableStatusOptionEntity itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption("available");
                                if (itemAvailEntity != null) {
                                    itemDetail.setAvailableStatusId(itemAvailEntity);
                                    historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                } else {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("AVAILABLE Status is not listed in our database add this first");
                                }

                                // region adding section open
                                if (newGatePassInward.getRegionDetails() != null) {
                                    itemDetail.setRegion(newGatePassInward.getRegionDetails());
                                    historyChangedByAdmin.setRegion(newGatePassInward.getRegionDetails());
                                } else {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Region not available");
                                } // region adding section closed

                                itemDetail.setAddedByEmail(loggedInUserName);// item added by email

                            } else {// closing for adding new item into table

                                if (!itemDetail.getRegion().equals(newGatePassInward.getRegionDetails())) {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(itemDetail.getSerial_No() + " is not belongs to your region");
                                }


                                historyChangedByAdmin.setSerial_No(gatepassItem.getSerialNo().trim().toLowerCase());

//								

                                if (newGatePassInward.getRegionDetails() != null) {
                                    if (itemDetail.getRegion() != null && (!itemDetail.getRegion().equals(newGatePassInward.getRegionDetails()))) {
                                        itemDetail.setRegion(newGatePassInward.getRegionDetails());
                                        historyChangedByAdmin.setRegion(newGatePassInward.getRegionDetails());
                                    } else if (itemDetail.getRegion() != null) {
                                        itemDetail.setRegion(newGatePassInward.getRegionDetails());
                                        historyChangedByAdmin.setRegion(newGatePassInward.getRegionDetails());
                                    }
                                    itemDetail.setRegion(newGatePassInward.getRegionDetails());
                                    historyChangedByAdmin.setRegion(newGatePassInward.getRegionDetails());
                                } else {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Region not available");
                                }

                                if (gatepassItem.getModuleFor() != null && gatepassItem.getModuleFor().trim().length() > 0) {
                                    if (!itemDetail.getModuleFor().trim().toLowerCase().equals(gatepassItem.getModuleFor().trim().toLowerCase())) {
                                        itemDetail.setModuleFor(loginUserRole);
                                        historyChangedByAdmin.setModuleFor(loginUserRole);
                                    }
                                }
                                if (gatepassItem.getModeuleVersion() != null && gatepassItem.getModuleFor().trim().length() > 0) {
                                    if (!itemDetail.getSystem_Version().trim().toLowerCase().equals(gatepassItem.getModeuleVersion().trim().toLowerCase())) {
                                        itemDetail.setSystem_Version(loginUserRole);
                                        historyChangedByAdmin.setSystem_Version(loginUserRole);
                                    }
                                }

                                // set party name
                                itemDetail.setPartyName(newGatePassInward.getPartyName());
                                historyChangedByAdmin.setPartyName(newGatePassInward.getPartyName());

                                if (gatepassItem.getSpareLocation() != null && gatepassItem.getSpareLocation().trim().length() > 0) {
                                    if (!itemDetail.getSpare_Location().trim().toLowerCase().equals(gatepassItem.getSpareLocation().trim().toLowerCase())) {
                                        itemDetail.setSpare_Location(gatepassItem.getSpareLocation());
                                        historyChangedByAdmin.setSpare_Location(gatepassItem.getSpareLocation());
                                    }
                                }
                                if (gatepassItem.getSystemName() != null && !gatepassItem.getSystemName().trim().isEmpty()) {
                                    if (!itemDetail.getSystem().trim().toLowerCase().equals(gatepassItem.getSystemName().trim().toLowerCase())) {
                                        itemDetail.setSystem(loginUserRole);
                                        historyChangedByAdmin.setSystem(loginUserRole);
                                    }
                                }
                                if (gatepassItem.getRackNo() != null && !gatepassItem.getRackNo().trim().isEmpty()) {
                                    if (!itemDetail.getRack_No().trim().toLowerCase().equals(gatepassItem.getRackNo().toLowerCase().trim())) {
                                        itemDetail.setRack_No(gatepassItem.getRackNo());
                                        historyChangedByAdmin.setRack_No(gatepassItem.getRackNo());
                                    }
                                }
                                if (gatepassItem.getRemark() != null && !gatepassItem.getRemark().trim().isEmpty()) {

                                    itemDetail.setRemark(gatepassItem.getRemark());
                                    historyChangedByAdmin.setRemark(gatepassItem.getRemark());

                                }

                                if (gatepassItem.getPartNo() != null && gatepassItem.getPartNo().trim().length() > 0) {
                                    if (!itemDetail.getPartNo().trim().toLowerCase().equals(gatepassItem.getPartNo().toLowerCase().trim())) {
                                        itemDetail.setPartNo(gatepassItem.getPartNo().trim().toLowerCase());
                                        historyChangedByAdmin.setPartNo(gatepassItem.getPartNo().trim().toLowerCase());
                                    }
                                }

                                // section for item Status new, old or repair, faulty
                                if (gatepassItem.getItemStatus() != null && gatepassItem.getItemStatus().trim().length() > 0) {

                                    itemStatusOptionEntity = itemStatusOptionDAO.getItemStatusOptionDetails(gatepassItem.getItemStatus().toLowerCase());
                                    if (itemStatusOptionEntity != null) {

                                        if (!itemDetail.getItemStatusId().equals(itemStatusOptionEntity)) {
                                            itemDetail.setItemStatusId(itemStatusOptionEntity);
                                            historyChangedByAdmin.setItemStatusId(itemStatusOptionEntity);
                                        }

                                    } else {
                                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("this item status is not listed add another one");
                                    } // Item status new,old or repair closed
                                }

                                // check keyword and sub keyword section for adding

                                if (gatepassItem.getKeywordName() != null && !gatepassItem.getKeywordName().trim().isEmpty()) {
                                    keywordEntity = keywordDAO.getByKeyword(gatepassItem.getKeywordName().toLowerCase());
                                    if (keywordEntity != null) {
                                        if (!itemDetail.getKeywordEntity().equals(keywordEntity)) {
                                            itemDetail.setKeywordEntity(keywordEntity);
                                            historyChangedByAdmin.setKeywordEntity(keywordEntity);
                                            // check sub keyword if keyword exist
                                            if (gatepassItem.getSubkeywordName() != null && !gatepassItem.getSubkeywordName().trim().isEmpty()) {
                                                subKeywordEntity = subKeywordDAO.getSpecificSubKeyword(keywordEntity, gatepassItem.getSubkeywordName().toLowerCase());
                                                if (subKeywordEntity != null) {
                                                    if (!itemDetail.getSubKeyWordEntity().equals(subKeywordEntity)) {
                                                        itemDetail.setSubKeyWordEntity(subKeywordEntity);
                                                        historyChangedByAdmin.setSubKeyWordEntity(subKeywordEntity);
                                                    }
                                                } else {
                                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This is subKeyword: " + gatepassItem.getSubkeywordName() + " not listed under this keyword: " + gatepassItem.getKeywordName());
                                                }
                                            }
                                        } else {
                                            if (gatepassItem.getSubkeywordName() != null && !gatepassItem.getSubkeywordName().trim().isEmpty()) {
                                                subKeywordEntity = subKeywordDAO.getSpecificSubKeyword(keywordEntity, gatepassItem.getSubkeywordName().toLowerCase().trim());
                                                if (subKeywordEntity != null) {
                                                    if (!itemDetail.getSubKeyWordEntity().equals(subKeywordEntity)) {
                                                        itemDetail.setSubKeyWordEntity(subKeywordEntity);
                                                        historyChangedByAdmin.setSubKeyWordEntity(subKeywordEntity);
                                                    }
                                                } else {
                                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This subKeyword: " + gatepassItem.getSubkeywordName() + " not listed under this keyword: " + gatepassItem.getKeywordName());
                                                }
                                            }

                                        }
                                    } else {
                                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("keyword is new, First add this keyword");
                                    }
                                } // Keyword Section closed
                            }

                            // fru process starting-----------
                            if (gatepassItem.isFru()) {
                                if (gatepassItem.getRmaNo() != null && !gatepassItem.getRmaNo().trim().isEmpty()) {

                                    FruEntity fruConfirming = fruDAO.getRmaConfirmation(gatepassItem.getRmaNo().trim().toLowerCase(), newGatePassInward.getRegionDetails());
                                    if (fruConfirming != null) {
                                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Rma No: " + gatepassItem.getRmaNo().trim().toLowerCase() + " is already registered");
                                    }

                                    FruEntity fruTable = new FruEntity();
                                    fruTable.setRmaNo(gatepassItem.getRmaNo().trim().toLowerCase());
                                    fruTable.setInGatepassID(newGatePassInward);
                                    fruTable.setCreatedDate(LocalDate.now());
                                    fruTable.setRegionDetails(newGatePassInward.getRegionDetails());

                                    ItemRepairingEntity itemInformation = new ItemRepairingEntity();
                                    itemInformation.setItemDetailId(itemDetail);
                                    itemInformation.setRmaNo(gatepassItem.getRmaNo().trim().toLowerCase());
                                    itemInformation.setSerialNo(itemDetail.getSerial_No());
                                    itemInformation.setFaultDetails(gatepassItem.getFaultDescription());
                                    itemInformation.setFaultRemark(gatepassItem.getFaultRemark());
                                    itemInformation.setDocketIdInward(gatepassItem.getDocketInward());
                                    itemInformation.setFruId(fruTable);
                                    itemInformation.setGeneratedDate(LocalDate.now());
                                    itemInformation.setInwardGatepass(newGatePassInward);
                                    itemInformation.setLastUpdateDate(LocalDate.now());
                                    itemInformation.setRegion(itemDetail.getRegion());
                                    itemInformation.setPartNo(itemDetail.getPartNo());
                                    itemInformation.setTicketGeneratedBy(loggedInUserName.toLowerCase());

                                    // this section is for getting repairing option details from table
                                    RepairingOptionEntity repairOption = repairingOptionDAO.getOptionDetails("pending");
                                    if (repairOption == null) {
                                        RepairingOptionEntity newRepairingOption = new RepairingOptionEntity();
                                        newRepairingOption.setStatusOption("pending");
                                        repairOption = repairingOptionDAO.save(newRepairingOption);
                                        if (repairOption == null) {
                                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Getting error when creating Repairing option");
                                        }
                                    }
                                    itemInformation.setRepairStatus(repairOption);

                                    // this section is creating technicianStatus pending/Assign
                                    TechnicianStatusEntity technicianStatus = technicianStatusDAO.getTechnicianStatus("pending");

                                    if (technicianStatus == null) {
                                        TechnicianStatusEntity newTechnicianStatus = new TechnicianStatusEntity();
                                        newTechnicianStatus.setTechnicianAssign("pending");
                                        newTechnicianStatus = technicianStatusDAO.save(newTechnicianStatus);
                                        if (newTechnicianStatus == null) {
                                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Getting error when creating technician Status pending");
                                        } else {
                                            technicianStatus = newTechnicianStatus;
                                        }
                                    }
                                    itemInformation.setTechnicianStatus(technicianStatus);

                                    // this section is for getting warranty option details from table
                                    if (gatepassItem.getWarrantyDetails() == null || gatepassItem.getWarrantyDetails() != null && gatepassItem.getWarrantyDetails().trim().length() <= 0) {
                                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Warranty Details is required for item that have Rma no");
                                    }
                                    WarrantyOptionEntity warrantyOption = warrantyOptionDAO.getWarrantyOption(gatepassItem.getWarrantyDetails().toLowerCase());
                                    if (warrantyOption == null) {
                                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This warranty option is not listed before");
                                    }
                                    itemInformation.setWarrantyDetails(warrantyOption);

                                    fruTable.setRepairingIdList(itemInformation);

                                    fruList.add(fruTable); // adding fru into fruList for adding all at a time
                                }
                            } // Fru process closed----------------------------

                            // Gatepass item Entity----------------------------------
                            ItemListViaGatePassInward itemListForGatepass = new ItemListViaGatePassInward();
                            itemListForGatepass.setSerialNo(itemDetail.getSerial_No());
                            itemListForGatepass.setFaultDescription(gatepassItem.getFaultDescription());
                            itemListForGatepass.setFaultRemark(gatepassItem.getFaultRemark());
                            itemListForGatepass.setRmaNo(gatepassItem.getRmaNo());
                            itemListForGatepass.setInwardGatepass(newGatePassInward);
                            itemListForGatepass.setPartNo(itemDetail.getPartNo());
                            itemListForGatepass.setKeywordName(itemDetail.getKeywordEntity().getKeywordName().toLowerCase());
                            itemListForGatepass.setSubkeywordName(itemDetail.getSubKeyWordEntity().getSubKeyword().toLowerCase());
                            itemListForGatepass.setRemark(gatepassItem.getRemark());

                            gatepassItemList.add(itemListForGatepass);// adding item into gatepass for adding all at a
                            // time
                            /// Gate pass item Entity closed ---------------------------------

                            if (gatepassItem.isFru() && gatepassItem.getRmaNo() != null && !gatepassItem.getRmaNo().trim().isEmpty()) {

                                ItemAvailableStatusOptionEntity itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption("repairing");
                                if (itemAvailEntity != null) {
                                    itemDetail.setAvailableStatusId(itemAvailEntity);
                                    historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                } else {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("REPAIRING Status is not listed in our database add this first");
                                }
                            } else {
                                if (itemDetail.getAvailableStatusId().getItemAvailableOption().trim().toLowerCase().equals("repairing")) {
//									return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//											.body("This item is already under Repairing State");
                                } else {
                                    // section for available status if Rma no not available
                                    ItemAvailableStatusOptionEntity itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption("available");
                                    if (itemAvailEntity != null) {
                                        itemDetail.setAvailableStatusId(itemAvailEntity);
                                        historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                    } else {
                                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("AVAILABLE Status is not listed in our database add this first");
                                    }
                                }
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

                            itemList.add(itemDetail); // adding itemList into main table list for add all at a time

                        }
                        if (!gatepassItemList.isEmpty()) {
                            newGatePassInward.setItemList(gatepassItemList);
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No item list is available for this gate pass");
                        }
                        if (fruList != null && !fruList.isEmpty()) {
                            newGatePassInward.setFruList(fruList);
                            System.out.println("1");
                        }

                        if (!itemList.isEmpty() && !gatepassItemList.isEmpty()) {
                            System.out.println("2");
                            itemList = itemDetailsDAO.saveAll(itemList);// save item details table
                            newGatePassInward = inwardGatePassDAO.save(newGatePassInward);// save gatepass table
                            if (!itemList.isEmpty() && !newGatePassInward.getItemList().isEmpty()) {
//								if (!fruList.isEmpty()) {
//									fruList = fruDAO.saveAll(fruList);// save repairing request table
//								}
                                if (newGatePassInward.getFruList() != null && !newGatePassInward.getFruList().isEmpty() && newGatePassInward.getFruList().size() > 1) {
                                    return gatepassPassService.generateInvoicePdf(newGatePassInward, newGatePassInward.getFruList());
                                } else {
                                    return gatepassPassService.generateInvoicePdf(newGatePassInward, fruList);
                                }

                                // return ResponseEntity.status(HttpStatus.OK).body("gatepass generated
                                // successfully");
                            } else {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No item list is available for this gate pass");
                            }

                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No data, Item List is Empty");
                        }

                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not LoggedIn");
            }
        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }

    }

    // Generate Outward GatePass
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> generatedOutwardGatepass(HttpServletRequest httpRequest, OutwardGatepassRequest outPassRequest) {
        String loggedInUserName = null;

        try {
            loggedInUserName = httpRequest.getUserPrincipal().getName();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
        }
        try {
            if (loggedInUserName != null && !loggedInUserName.isEmpty()) {
                adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
                if (adminUserEntity == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Login");
                } else {
                    String loginUserRole = adminUserEntity.getRoleModel().getRoleName().toLowerCase();
                    if (outPassRequest == null || outPassRequest.getPartyName() == null || (outPassRequest.getPartyName() != null && outPassRequest.getPartyName().trim().isEmpty()) || outPassRequest.getPartyAddress() == null || (outPassRequest.getPartyAddress() != null && outPassRequest.getPartyAddress().trim().isEmpty()) || outPassRequest.getItemList() == null || (outPassRequest.getItemList() != null && outPassRequest.getItemList().isEmpty())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("party name, Address and Atleast 1 item is required for gatepass");
                    } else {
                        OutwardGatepassEntity newGatePassOutward = new OutwardGatepassEntity();
                        newGatePassOutward.setPartyName(outPassRequest.getPartyName());
                        newGatePassOutward.setPartyAddress(outPassRequest.getPartyAddress());
                        newGatePassOutward.setCreatedBy(loggedInUserName);
                        newGatePassOutward.setCreatedDate(LocalDate.now());
                        if (outPassRequest.getPartyContact() != null && outPassRequest.getPartyContact().trim().length() >= 9) {
                            newGatePassOutward.setPartyContact(outPassRequest.getPartyContact().trim());
                        }

                        if (loginUserRole.equals("admin")) {
                            if (outPassRequest.getRegion() != null && !outPassRequest.getRegion().trim().isEmpty()) {
                                RegionEntity regionDetails = regionDAO.findByCity(outPassRequest.getRegion().trim().toLowerCase());
                                if (regionDetails != null) {
                                    newGatePassOutward.setRegionDetails(regionDetails);
                                } else {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Provided region not listed in our database");
                                }
                            } else {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Region is required for admin role");
                            }
                        } else {
                            // user region assign to gate pass
                            if (adminUserEntity.getRegionEntity() != null && adminUserEntity.getRegionEntity().getId() != null) {

                                newGatePassOutward.setRegionDetails(adminUserEntity.getRegionEntity());
                            } else {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No region assign to you");
                            }
                        }
                        newGatePassOutward = outwardGatepassDAO.save(newGatePassOutward);

                        List<ItemDetailsEntity> itemList = new ArrayList<ItemDetailsEntity>();
                        List<FruEntity> fruList = new ArrayList<FruEntity>();
                        List<ItemListViaGatePassOutwardEntity> gatepassItemList = new ArrayList<ItemListViaGatePassOutwardEntity>();
                        for (OutwardGatepassItemList gatepassItem : outPassRequest.getItemList()) {
                            if (gatepassItem.getSerialNo() == null || (gatepassItem.getSerialNo() != null && gatepassItem.getSerialNo().trim().length() <= 0)) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("serial number for every item is required");
                            }
                            ItemDetailsEntity itemDetail = itemDetailsDAO.getComponentDetailsBySerialNo(gatepassItem.getSerialNo().trim().toLowerCase());
                            ItemHistoryUpdatedByAdminEntity historyChangedByAdmin = new ItemHistoryUpdatedByAdminEntity();
                            if (itemDetail == null) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This item is not listed before with this serial no:-" + gatepassItem.getSerialNo().trim().toLowerCase());
                            } else {

                                if (!itemDetail.getRegion().equals(newGatePassOutward.getRegionDetails())) {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(itemDetail.getSerial_No() + " is not belongs to your region");
                                }

                                historyChangedByAdmin.setSerial_No(gatepassItem.getSerialNo().trim().toLowerCase());
                                if (!gatepassItem.isFru()) {
                                    if (!itemDetail.getAvailableStatusId().getItemAvailableOption().trim().toLowerCase().equals("repairing")) {
                                        if (itemDetail.getAvailableStatusId().getItemAvailableOption().trim().toLowerCase().equals("issue")) {
                                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This item is not available");
                                        }
                                    }
                                }

                                if (gatepassItem.getRemark() != null && !gatepassItem.getRemark().trim().isEmpty()) {

                                    itemDetail.setRemark(gatepassItem.getRemark());
                                    historyChangedByAdmin.setRemark(gatepassItem.getRemark());

                                }

                            }

                            // fru process starting-----------
                            if (gatepassItem.isFru()) {
                                if (gatepassItem.getRmaNo() != null && !gatepassItem.getRmaNo().trim().isEmpty()) {

                                    FruEntity fruTable = fruDAO.getRmaConfirmation(gatepassItem.getRmaNo().trim().toLowerCase(), newGatePassOutward.getRegionDetails());
                                    if (fruTable == null) {
                                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Rma No: " + gatepassItem.getRmaNo().trim().toLowerCase() + " is not registered");
                                    }
                                    fruTable.setClosingDate(LocalDate.now());
                                    fruTable.setOutGatepassID(newGatePassOutward);

                                    ItemRepairingEntity itemInformation = fruTable.getRepairingIdList();
                                    if (itemInformation == null) {
                                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No item registered under this " + fruTable.getRmaNo() + " RMA no");
                                    }
                                    if (!itemDetail.getSerial_No().toLowerCase().equals(itemInformation.getSerialNo().trim().toLowerCase())) {
                                        // starting for available option null or not--------------

                                        // update for add remark on main item field under this rma
                                        ItemDetailsEntity itemForAddRemark = itemDetailsDAO.getItemDetailsBySerialNo(itemInformation.getSerialNo().trim().toLowerCase());
//										if(itemForAddRemark==null){
//											return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This item is not available");
//
//										}else {
                                        ItemHistoryUpdatedByAdminEntity historyRemark = new ItemHistoryUpdatedByAdminEntity();
                                        historyRemark.setSerial_No(itemInformation.getSerialNo().trim().toLowerCase());
                                        historyRemark.setItemDetailsEntity(itemForAddRemark);
                                        List<ItemHistoryUpdatedByAdminEntity> historyList = new ArrayList<>();

                                        itemForAddRemark.setRemark("this is Replaced with :-" + itemDetail.getSerial_No().toLowerCase() + " for this Rma no:-" + gatepassItem.getRmaNo().toLowerCase());
                                        historyRemark.setRemark("this is Replaced with :-" + itemDetail.getSerial_No().toLowerCase() + " for this Rma no:-" + gatepassItem.getRmaNo().toLowerCase());

                                        itemForAddRemark.setUpdate_Date(ZonedDateTime.now());
                                        historyRemark.setUpdate_Date(ZonedDateTime.now());
                                        itemForAddRemark.setEmpEmail(loggedInUserName);
                                        historyRemark.setUpdatedByEmail(loggedInUserName);

                                        historyList.add(historyRemark);
                                        itemForAddRemark.setItemHistoryUpdatedByAdminEntityList(historyList);
                                        itemList.add(itemForAddRemark);

                                        /// update main item remark closed

                                        if (itemDetail.getAvailableStatusId().getItemAvailableOption() != null && !itemDetail.getAvailableStatusId().getItemAvailableOption().trim().isEmpty()) {
                                            if (itemDetail.getAvailableStatusId().getItemAvailableOption().trim().toLowerCase().equals("available")) {

                                                ItemAvailableStatusOptionEntity itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption("issue");
                                                if (itemAvailEntity != null) {
                                                    itemDetail.setAvailableStatusId(itemAvailEntity);
                                                    historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                                } else {
                                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("issue Status is not listed in our database, contact to Admin");
                                                }

                                                String userRemark = itemDetail.getRemark();
                                                String addExtraRemark = "This item is replacement of Item Serial: " + itemInformation.getSerialNo();
                                                String contcatinationOFUserAndExtra = userRemark + addExtraRemark;
                                                itemDetail.setRemark(contcatinationOFUserAndExtra);
                                                historyChangedByAdmin.setRemark(contcatinationOFUserAndExtra);

                                                itemDetail.setUpdate_Date(ZonedDateTime.now());
                                                historyChangedByAdmin.setUpdate_Date(ZonedDateTime.now());

                                                itemDetail.setEmpEmail(loggedInUserName);
                                                historyChangedByAdmin.setUpdatedByEmail(loggedInUserName);


                                                itemInformation.setReplaceItemSerial(itemDetail.getSerial_No());


                                            } else {

                                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("item with serial number: " + itemDetail.getSerial_No() + " is already under" + itemDetail.getAvailableStatusId().getItemAvailableOption());
                                            }

                                        } else {// check for available option if null then closing-----
                                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Avilable status is null for this serial: " + itemDetail.getSerial_No());
                                        }

                                        // replacement section closed
                                    } else {

                                        if (itemDetail.getAvailableStatusId().getItemAvailableOption() != null && !itemDetail.getAvailableStatusId().getItemAvailableOption().trim().isEmpty()) {
                                            if (itemDetail.getAvailableStatusId().getItemAvailableOption().trim().toLowerCase().equals("available")) {

                                                ItemAvailableStatusOptionEntity itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption("issue");
                                                if (itemAvailEntity != null) {
                                                    itemDetail.setAvailableStatusId(itemAvailEntity);
                                                    historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                                } else {
                                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("issue Status is not listed in our database, contact to Admin");
                                                }

                                                if (gatepassItem.getRemark() != null && !gatepassItem.getRemark().trim().isEmpty()) {
                                                    itemDetail.setRemark(gatepassItem.getRemark());
                                                    historyChangedByAdmin.setRemark(gatepassItem.getRemark());
                                                }

                                            }
                                        }
                                    }

                                    if (gatepassItem.getDocketOutward() != null && !gatepassItem.getDocketOutward().trim().isEmpty()) {
                                        itemInformation.setDocketIdOutward(gatepassItem.getDocketOutward());
                                    }
                                    itemInformation.setLastUpdateDate(LocalDate.now());

                                    fruList.add(fruTable); // adding fru into fruList for adding all at a time
                                }
                            }

                            // Gatepass item Entity----------------------------------
                            ItemListViaGatePassOutwardEntity itemListForGatepass = new ItemListViaGatePassOutwardEntity();
                            itemListForGatepass.setSerialNo(itemDetail.getSerial_No());
                            itemListForGatepass.setRmaNo(gatepassItem.getRmaNo());
                            itemListForGatepass.setPartNo(itemDetail.getPartNo());
                            itemListForGatepass.setKeywordName(itemDetail.getKeywordEntity().getKeywordName().toLowerCase());



                            SubKeywordEntity subKeyword = itemDetail.getSubKeyWordEntity();
                            if (subKeyword != null && subKeyword.getSubKeyword() != null) {
                                itemListForGatepass.setSubkeywordName(subKeyword.getSubKeyword().toLowerCase());
                            } else {
                                // Set a default value so it doesn't crash
                                itemListForGatepass.setSubkeywordName("N/A");
                            }
                            // --- END: NULL CHECK FIX ---
                            itemListForGatepass.setRemark(gatepassItem.getRemark());

                            gatepassItemList.add(itemListForGatepass);// adding item into gatepass for adding all at a
                            // time
                            /// Gate pass item Entity closed ---------------------------------

                            if (!gatepassItem.isFru()) {

                                ItemAvailableStatusOptionEntity itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption("issue");
                                if (itemAvailEntity != null) {
                                    itemDetail.setAvailableStatusId(itemAvailEntity);
                                    historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                } else {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Issue Status is not listed in our database add this first");
                                }
                            }

                            historyChangedByAdmin.setUpdatedByEmail(loggedInUserName);

                            itemDetail.setPartyName(newGatePassOutward.getPartyName());
                            historyChangedByAdmin.setPartyName(newGatePassOutward.getPartyName());

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

                            itemList.add(itemDetail); // adding itemList into main table list for add all at a time

                        }
                        if (!gatepassItemList.isEmpty()) {
                            newGatePassOutward.setItemList(gatepassItemList);
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No item list is available for this gate pass");
                        }

                        if (!itemList.isEmpty() && !gatepassItemList.isEmpty()) {

                            newGatePassOutward = outwardGatepassDAO.save(newGatePassOutward);// save gatepass table
                            itemList = itemDetailsDAO.saveAll(itemList);// save item details table

                            if (!itemList.isEmpty() && !newGatePassOutward.getItemList().isEmpty()) {
                                if (!fruList.isEmpty()) {
                                    fruList = fruDAO.saveAll(fruList);// save repairing request table
                                }
                                return outwardGatepassPDFService.generateInvoicePdf(newGatePassOutward);
                            } else {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No item list is available for this gate pass");
                            }

                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No data, Item List is Empty");
                        }

                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not Logged In");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

}
