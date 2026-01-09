package com.serverManagement.server.management.service.itemDetails;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.serverManagement.server.management.dto.itemRepairDetails.DashboardSummaryDto;
import com.serverManagement.server.management.dto.itemRepairDetails.RegionCountDto;
import com.serverManagement.server.management.entity.itemDetails.RegionUpdateDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDynamicQueryBuilder;
import com.serverManagement.server.management.dao.keyword.KeywordDAO;
import com.serverManagement.server.management.dao.keyword.SubKeywordDAO;
import com.serverManagement.server.management.dao.option.ItemAvailableStatusOptionDAO;
import com.serverManagement.server.management.dao.option.ItemStatusOptionDAO;
import com.serverManagement.server.management.dao.region.RegionDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.itemDetails.ItemHistoryUpdatedByAdminEntity;
import com.serverManagement.server.management.entity.keyword.KeywordEntity;
import com.serverManagement.server.management.entity.keyword.SubKeywordEntity;
import com.serverManagement.server.management.entity.options.ItemAvailableStatusOptionEntity;
import com.serverManagement.server.management.entity.options.ItemStatusOptionEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;
import com.serverManagement.server.management.request.itemDetails.AddItemRequest;
import com.serverManagement.server.management.request.itemDetails.AssignItemDetailsRequest;
import com.serverManagement.server.management.request.itemDetails.GetItemByKeywordRequest;
import com.serverManagement.server.management.response.itemDetails.ItemDetailsResponse;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ItemDetailsService {

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

    private AdminUserEntity adminUserEntity;
    private RegionEntity regionEntity;
    private KeywordEntity keywordEntity;
    private SubKeywordEntity subKeywordEntity;
    private ItemDetailsEntity itemDetailsEntity;
    private ItemAvailableStatusOptionEntity itemAvailEntity;
    private ItemStatusOptionEntity itemStatusOptionEntity;

    // Admin or manager add component
    public ResponseEntity<?> addComponent(HttpServletRequest request, AddItemRequest addComponent) {

        String loggedInUserName = null;
        String message = "";
        try {
            loggedInUserName = request.getUserPrincipal().getName();
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
                    if (loginUserRole.equals("admin") || loginUserRole.equals("manager")
                            || loginUserRole.equals("employee")) {

                        // Debug log incoming payload
                        System.out.println("DEBUG addComponent payload: " + addComponent);

                        // VALIDATION (serial NOT required) - ensure other required fields exist
                        if (addComponent == null
                                || isBlank(addComponent.getKeyword())
                                || isBlank(addComponent.getRackNo())
                                || isBlank(addComponent.getAvailableStatus())
                                || isBlank(addComponent.getItemStatus())) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("Keyword or Region or RackNo or AvailableStatus or ItemStatus must not be null");
                        }

                        // If serial empty -> generate; else check uniqueness
                        String serialFromRequest = (addComponent.getSerialNo() == null ? ""
                                : addComponent.getSerialNo().trim());
                        boolean serialWasGenerated = false;
                        if (serialFromRequest.isEmpty()) {
                            String generated = generateSerialForKeyword(addComponent.getKeyword());
                            addComponent.setSerialNo(generated);
                            serialFromRequest = generated;
                            serialWasGenerated = true;
                            // debug
                            System.out.println("Generated serial for request: " + generated);
                        } else {
                            boolean serialExists = itemDetailsDAO.isSerialExist(serialFromRequest.toLowerCase());
                            if (serialExists) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("Serial number already exists");
                            }
                        }

                        // At this point, serialFromRequest contains final serial (provided or
                        // generated)

                        // Build entity and map values (mostly your original logic)
                        ItemDetailsEntity componentDetails = new ItemDetailsEntity();
                        ItemHistoryUpdatedByAdminEntity historyChangedByAdmin = new ItemHistoryUpdatedByAdminEntity();

                        // set serial (always set since we either have from request or generated)
                        if (!isBlank(addComponent.getSerialNo())) {
                            componentDetails.setSerial_No(addComponent.getSerialNo().trim().toLowerCase());
                            historyChangedByAdmin.setSerial_No(addComponent.getSerialNo().trim().toLowerCase());
                        }

                        // region handling
                        if (addComponent.getRegion() != null && addComponent.getRegion().trim().length() > 0) {
                            if (loginUserRole.equals("admin")) {
                                regionEntity = regionDAO.findByCity(addComponent.getRegion().toLowerCase());
                                if (regionEntity != null) {
                                    componentDetails.setRegion(regionEntity);
                                    historyChangedByAdmin.setRegion(regionEntity);
                                } else {
                                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                            .body("This region not Listed, First add this region");
                                }
                            } else {
                                if (adminUserEntity.getRegionEntity() != null) {
                                    componentDetails.setRegion(adminUserEntity.getRegionEntity());
                                    historyChangedByAdmin.setRegion(adminUserEntity.getRegionEntity());
                                } else {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                            .body("Your region is not assign, contact to admin to assign a region");
                                }
                            }
                        } else {
                            if (loginUserRole.equals("manager") || loginUserRole.equals("employee")) {
                                if (adminUserEntity.getRegionEntity() != null) {
                                    componentDetails.setRegion(adminUserEntity.getRegionEntity());
                                    historyChangedByAdmin.setRegion(adminUserEntity.getRegionEntity());
                                } else {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                            .body("No region assign to you, contact to admin to assign a region");
                                }
                            } else {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("Region is required for Admin Role");
                            }
                        }

                        // keyword & subkeyword mapping (unchanged)
                        if (addComponent.getKeyword() != null && addComponent.getKeyword().trim().length() > 0) {
                            keywordEntity = keywordDAO.getByKeyword(addComponent.getKeyword().toLowerCase());
                            if (keywordEntity != null) {
                                componentDetails.setKeywordEntity(keywordEntity);
                                historyChangedByAdmin.setKeywordEntity(keywordEntity);
                                if (addComponent.getSubKeyword() != null
                                        && addComponent.getSubKeyword().trim().length() > 0) {
                                    subKeywordEntity = subKeywordDAO.getSpecificSubKeyword(keywordEntity,
                                            addComponent.getSubKeyword().toLowerCase());
                                    if (subKeywordEntity != null) {
                                        componentDetails.setSubKeyWordEntity(subKeywordEntity);
                                        historyChangedByAdmin.setSubKeyWordEntity(subKeywordEntity);
                                    } else {
                                        message = ", sub keyword not listed under this keyword, item saved under keyword only";
                                    }
                                }
                            } else {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body("keyword is new, First add this keyword");
                            }
                        }

                        // other optional fields mapping (unchanged)
                        if (addComponent.getBoxNo() != null && addComponent.getBoxNo().trim().length() > 0) {
                            componentDetails.setBoxNo(addComponent.getBoxNo());
                            historyChangedByAdmin.setBoxNo(addComponent.getBoxNo());
                        }
                        if (addComponent.getItemDescription() != null
                                && addComponent.getItemDescription().trim().length() > 0) {
                            componentDetails.setItemDescription(addComponent.getItemDescription());
                            historyChangedByAdmin.setItemDescription(addComponent.getItemDescription());
                        }
                        if (addComponent.getModuleFor() != null && addComponent.getModuleFor().trim().length() > 0) {
                            componentDetails.setModuleFor(addComponent.getModuleFor());
                            historyChangedByAdmin.setModuleFor(addComponent.getModuleFor());
                        }
                        if (addComponent.getPartNo() != null && addComponent.getPartNo().trim().length() > 0) {
                            componentDetails.setPartNo(addComponent.getPartNo());
                            historyChangedByAdmin.setPartNo(addComponent.getPartNo());
                        }
                        if (addComponent.getModelNo() != null && addComponent.getModelNo().trim().length() > 0) {
                            componentDetails.setModelNo(addComponent.getModelNo());
                            historyChangedByAdmin.setModelNo(addComponent.getModelNo());
                        }
                        if (addComponent.getItemStatus() != null && addComponent.getItemStatus().trim().length() > 0) {
                            itemStatusOptionEntity = itemStatusOptionDAO
                                    .getItemStatusOptionDetails(addComponent.getItemStatus().toLowerCase());
                            if (itemStatusOptionEntity != null) {
                                componentDetails.setItemStatusId(itemStatusOptionEntity);
                                historyChangedByAdmin.setItemStatusId(itemStatusOptionEntity);
                            } else {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body("this item status is not listed add another one");
                            }
                        }
                        if (addComponent.getRackNo() != null && addComponent.getRackNo().trim().length() > 0) {
                            componentDetails.setRack_No(addComponent.getRackNo());
                            historyChangedByAdmin.setRack_No(addComponent.getRackNo());
                        }
                        if (addComponent.getSpareLocation() != null
                                && addComponent.getSpareLocation().trim().length() > 0) {
                            componentDetails.setSpare_Location(addComponent.getSpareLocation().trim());
                            historyChangedByAdmin.setSpare_Location(addComponent.getSpareLocation());
                        }
                        if (addComponent.getSystem() != null && addComponent.getSystem().trim().length() > 0) {
                            componentDetails.setSystem(addComponent.getSystem());
                            historyChangedByAdmin.setSystem(addComponent.getSystem());
                        }
                        if (addComponent.getPartyName() != null && addComponent.getPartyName().trim().length() > 0) {
                            componentDetails.setPartyName(addComponent.getPartyName());
                            historyChangedByAdmin.setPartyName(addComponent.getPartyName());
                        }
                        if (addComponent.getRemark() != null && addComponent.getRemark().trim().length() > 0) {
                            componentDetails.setRemark(addComponent.getRemark());
                            historyChangedByAdmin.setRemark(addComponent.getRemark());
                        }
                        if (addComponent.getSystemVersion() != null
                                && addComponent.getSystemVersion().trim().length() > 0) {
                            componentDetails.setSystem_Version(addComponent.getSystemVersion());
                            historyChangedByAdmin.setSystem_Version(addComponent.getSystemVersion());
                        }

                        // availability status mapping
                        if (addComponent.getAvailableStatus() != null
                                && addComponent.getAvailableStatus().trim().length() > 0) {
                            String availLower = addComponent.getAvailableStatus().toLowerCase();
                            if (availLower.equals("issue") || availLower.equals("available")) {
                                itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption(availLower);
                                if (itemAvailEntity != null) {
                                    componentDetails.setAvailableStatusId(itemAvailEntity);
                                    historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                } else {
                                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                            .body("available status is not not listed try another");
                                }
                            } else {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body("At the time of adding you only set status available or issue");
                            }
                        }

                        // set audit fields
                        componentDetails.setAddedByEmail(loggedInUserName);
                        historyChangedByAdmin.setUpdatedByEmail(loggedInUserName);
                        componentDetails.setEmpEmail(loggedInUserName);
                        componentDetails.setAdding_Date(ZonedDateTime.now());
                        historyChangedByAdmin.setUpdate_Date(ZonedDateTime.now());
                        componentDetails.setUpdate_Date(ZonedDateTime.now());

                        if (historyChangedByAdmin != null) {
                            historyChangedByAdmin.setItemDetailsEntity(componentDetails);
                            List<ItemHistoryUpdatedByAdminEntity> historyChangedByAdminList = new ArrayList<>();
                            historyChangedByAdminList.add(historyChangedByAdmin);
                            componentDetails.setItemHistoryUpdatedByAdminEntityList(historyChangedByAdminList);
                        }

                        // SAVE with retry if serial was generated (to handle collisions)
                        final int MAX_RETRIES = 5;
                        int attempt = 0;
                        while (true) {
                            try {
                                attempt++;
                                componentDetails = itemDetailsDAO.save(componentDetails);
                                if (componentDetails != null) {
                                    String createdSerial = componentDetails.getSerial_No();
                                    return ResponseEntity.status(HttpStatus.CREATED)
                                            .body("Item added successfully. Serial: " + createdSerial
                                                    + (message != null ? message : ""));
                                } else {
                                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .body("Something went wrong");
                                }
                            } catch (DataIntegrityViolationException dive) {
                                // likely unique constraint violation on serial_No
                                System.out.println(
                                        "DataIntegrityViolation on save attempt " + attempt + ": " + dive.getMessage());
                                if (!serialWasGenerated) {
                                    // user provided serial and it's duplicate -> conflict
                                    return ResponseEntity.status(HttpStatus.CONFLICT)
                                            .body("serial no :" + addComponent.getSerialNo()
                                                    + " is already available, Serial no is case sensitive");
                                }
                                if (attempt >= MAX_RETRIES) {
                                    return ResponseEntity.status(HttpStatus.CONFLICT)
                                            .body("Could not auto-resolve serial collision after retries");
                                }
                                // regenerate and retry
                                String newSerial = generateSerialForKeyword(addComponent.getKeyword());
                                componentDetails.setSerial_No(newSerial.toLowerCase());
                                historyChangedByAdmin.setSerial_No(newSerial.toLowerCase());
                                System.out.println("Retrying save with regenerated serial: " + newSerial + " (attempt "
                                        + attempt + ")");
                                // continue loop to retry
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Internal server error");
                            }
                        }

                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Your role is not authorized to add Items");
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // function for update items
    public ResponseEntity<?> issueItems(HttpServletRequest request, AssignItemDetailsRequest assignItemDetails) {

        String loggedInUserName = null;
        String message = "";
        String subKeyMessage = "";
        try {
            loggedInUserName = request.getUserPrincipal().getName();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
        }
        try {
            if (loggedInUserName != null && loggedInUserName.length() > 0) {
                adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
                if (adminUserEntity == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Login");
                } else {
                    if (adminUserEntity.getRoleModel() == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Role Assign to You");
                    }
                    String loginUserRole = adminUserEntity.getRoleModel().getRoleName().trim().toLowerCase();

                    if (assignItemDetails == null || assignItemDetails.getSerialNo() == null
                            || assignItemDetails.getSerialNo().trim().length() < 0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("serial no could not be null");
                    } else {// getting data from database on the basis of serial no.
                        ItemDetailsEntity itemDetails = itemDetailsDAO
                                .getItemDetailsBySerialNo(assignItemDetails.getSerialNo().trim().toLowerCase());

                        if (itemDetails == null) {// check is their have data against provided serial no
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Serial nu not registered");
                        } else {// process after verify that, data have stored in db against this serial no
                            // creating new object for history table
                            ItemHistoryUpdatedByAdminEntity historyChangedByAdmin = new ItemHistoryUpdatedByAdminEntity();

                            if (adminUserEntity.getRoleModel().getRoleName() != null) {
                                if (!adminUserEntity.getRoleModel().getRoleName().toLowerCase().equals("admin")) {

                                    if (adminUserEntity.getRegionEntity() == null) {
                                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body("No region assign to you");
                                    } else {
                                        if (!itemDetails.getRegion().equals(adminUserEntity.getRegionEntity())) {
                                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                    .body("this item not belonging to your region");
                                        }
                                    }
                                }
                            } else {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No role assign to you");
                            }

                            // check for party name both at db and from user
                            if ((itemDetails.getPartyName() == null || (itemDetails.getPartyName() != null
                                    && itemDetails.getPartyName().trim().length() <= 0))
                                    && (assignItemDetails.getPartyName() == null
                                            || (assignItemDetails.getPartyName() != null
                                                    && assignItemDetails.getPartyName().trim().length() <= 0))) {

                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("Party name required at first time for update");
                            } else if (assignItemDetails.getPartyName() != null
                                    && assignItemDetails.getPartyName().trim().length() > 0) {
                                if (itemDetails.getPartyName() != null
                                        && itemDetails.getPartyName().trim().length() > 0) {
                                    if (!itemDetails.getPartyName().toLowerCase()
                                            .equals(assignItemDetails.getPartyName().toLowerCase())) {
                                        itemDetails.setPartyName(assignItemDetails.getPartyName().toLowerCase());
                                        historyChangedByAdmin
                                                .setPartyName(assignItemDetails.getPartyName().toLowerCase());
                                    }
                                } else {
                                    itemDetails.setPartyName(assignItemDetails.getPartyName().toLowerCase());
                                    historyChangedByAdmin.setPartyName(assignItemDetails.getPartyName().toLowerCase());
                                }
                            } else {

                            }

                            // boxNo
                            if (assignItemDetails.getBoxNo() != null
                                    && assignItemDetails.getBoxNo().trim().length() > 0) {
                                if (itemDetails.getBoxNo() != null && itemDetails.getBoxNo().trim().length() > 0) {
                                    if (!itemDetails.getBoxNo().toLowerCase()
                                            .equals(assignItemDetails.getBoxNo().toLowerCase())) {// check previous and
                                                                                                  // new not equal
                                        itemDetails.setBoxNo(assignItemDetails.getBoxNo());
                                        historyChangedByAdmin.setBoxNo(assignItemDetails.getBoxNo());
                                    }
                                } else {
                                    if (!itemDetails.getBoxNo().toLowerCase()
                                            .equals(assignItemDetails.getBoxNo().toLowerCase())) {// check previous and
                                                                                                  // new not equal
                                        itemDetails.setBoxNo(assignItemDetails.getBoxNo());
                                        historyChangedByAdmin.setBoxNo(assignItemDetails.getBoxNo());
                                    }
                                }
                            }

                            // rack no
                            if (assignItemDetails.getRackNo() != null
                                    && assignItemDetails.getRackNo().trim().length() > 0) {
                                if (itemDetails.getRack_No() != null && itemDetails.getRack_No().trim().length() > 0) {
                                    if (!itemDetails.getRack_No().toLowerCase()
                                            .equals(assignItemDetails.getRackNo().toLowerCase())) {
                                        itemDetails.setRack_No(assignItemDetails.getRackNo());
                                        historyChangedByAdmin.setRack_No(assignItemDetails.getRackNo());
                                    }
                                } else {
                                    itemDetails.setRack_No(assignItemDetails.getRackNo());
                                    historyChangedByAdmin.setRack_No(assignItemDetails.getRackNo());
                                }
                            }

                            // spare Location
                            if (assignItemDetails.getSpareLocation() != null
                                    && assignItemDetails.getSpareLocation().trim().length() > 0) {
                                if (itemDetails.getSpare_Location() != null
                                        && itemDetails.getSpare_Location().trim().length() > 0) {
                                    if (!itemDetails.getSpare_Location().toLowerCase()
                                            .equals(assignItemDetails.getSpareLocation().toLowerCase())) {
                                        itemDetails.setSpare_Location(assignItemDetails.getSpareLocation());
                                        historyChangedByAdmin.setSpare_Location(assignItemDetails.getSpareLocation());
                                    }
                                } else {
                                    itemDetails.setSpare_Location(assignItemDetails.getSpareLocation());
                                    historyChangedByAdmin.setSpare_Location(assignItemDetails.getSpareLocation());
                                }
                            }

                            // item Description
                            if (assignItemDetails.getItemDescription() != null
                                    && assignItemDetails.getItemDescription().trim().length() > 0) {
                                if (itemDetails.getItemDescription() != null
                                        && itemDetails.getItemDescription().trim().length() > 0) {
                                    if (!itemDetails.getItemDescription().toLowerCase()
                                            .equals(assignItemDetails.getItemDescription().toLowerCase())) {
                                        itemDetails.setItemDescription(assignItemDetails.getItemDescription());
                                        historyChangedByAdmin
                                                .setItemDescription(assignItemDetails.getItemDescription());
                                    }
                                } else {
                                    itemDetails.setItemDescription(assignItemDetails.getItemDescription());
                                    historyChangedByAdmin.setItemDescription(assignItemDetails.getItemDescription());
                                }
                            }

                            // remark
                            if (assignItemDetails.getRemark() != null
                                    && assignItemDetails.getRemark().trim().length() > 0) {
                                if (itemDetails.getRemark() != null && itemDetails.getRemark().trim().length() > 0) {
                                    if (!itemDetails.getRemark().toLowerCase()
                                            .equals(assignItemDetails.getRemark().toLowerCase())) {
                                        itemDetails.setRemark(assignItemDetails.getRemark());
                                        historyChangedByAdmin.setRemark(assignItemDetails.getRemark());
                                    }
                                } else {
                                    itemDetails.setRemark(assignItemDetails.getRemark());
                                    historyChangedByAdmin.setRemark(assignItemDetails.getRemark());
                                }
                            }

                            // item Status
                            if (assignItemDetails.getItemStatus() != null
                                    && assignItemDetails.getItemStatus().trim().length() > 0) {
                                if (itemDetails.getItemStatusId() == null) {
                                    itemStatusOptionEntity = itemStatusOptionDAO.getItemStatusOptionDetails(
                                            assignItemDetails.getItemStatus().toLowerCase());
                                    if (itemStatusOptionEntity != null) {
                                        itemDetails.setItemStatusId(itemStatusOptionEntity);
                                        historyChangedByAdmin.setItemStatusId(itemStatusOptionEntity);
                                    } else {
                                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body("this item status is not listed add another one");
                                    }
                                } else {
                                    if (!itemDetails.getItemStatusId().getItemStatus().toLowerCase()
                                            .equals(assignItemDetails.getItemStatus().toLowerCase())) {
                                        itemStatusOptionEntity = itemStatusOptionDAO.getItemStatusOptionDetails(
                                                assignItemDetails.getItemStatus().toLowerCase());
                                        if (itemStatusOptionEntity != null) {
                                            itemDetails.setItemStatusId(itemStatusOptionEntity);
                                            historyChangedByAdmin.setItemStatusId(itemStatusOptionEntity);
                                        } else {
                                            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                    .body("this item status is not listed add another one");
                                        }
                                    }
                                }
                            }

                            // item Availability
                            if (assignItemDetails.getItemAvailability() != null
                                    && assignItemDetails.getItemAvailability().trim().length() > 0) {
                                // check is their trying to updating available status to repairing or delete
                                if (assignItemDetails.getItemAvailability().toLowerCase().equals("repairing")
                                        || assignItemDetails.getItemAvailability().toLowerCase().equals("delete")) {
                                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                            .body("you can not change avilable status "
                                                    + assignItemDetails.getItemAvailability() + " from here");
                                }
                                // check itemdetails have avilable status at table or not
                                if (itemDetails.getAvailableStatusId() == null) {
                                    itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption(
                                            assignItemDetails.getItemAvailability().toLowerCase());
                                    if (itemAvailEntity != null) {
                                        itemDetails.setAvailableStatusId(itemAvailEntity);
                                        historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                    } else {
                                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body("this option is not listed add another one");
                                    }
                                } else {// if available status stored in table then check for both are same or not
                                    if (itemDetails.getAvailableStatusId().getItemAvailableOption().toLowerCase()
                                            .equals("repairing")
                                            || itemDetails.getAvailableStatusId().getItemAvailableOption().toLowerCase()
                                                    .equals("delete")) {
                                        if (loginUserRole.equals("employee")) {
                                            System.out.println(loginUserRole);
                                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                                    "Only admin or manager can change repairing or detele item status");
                                        }
                                    }
                                    if (!itemDetails.getAvailableStatusId().getItemAvailableOption().toLowerCase()
                                            .equals(assignItemDetails.getItemAvailability().toLowerCase())) {

                                        itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption(
                                                assignItemDetails.getItemAvailability().toLowerCase());
                                        if (itemAvailEntity != null) {
                                            itemDetails.setAvailableStatusId(itemAvailEntity);
                                            historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                        } else {
                                            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                    .body("this option is not listed add another one");
                                        }
                                    }
                                }
                            }

                            // Return Duration Logic
                            if (assignItemDetails.getReturnDuration() != null) {
                                itemDetails.setReturnDuration(assignItemDetails.getReturnDuration());
                                // Automatically set status to "issued" (likely the correct DB value) if return
                                // duration is set
                                String statusToSet = "issued";
                                itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption(statusToSet);

                                if (itemAvailEntity == null) {
                                    statusToSet = "issue";
                                    itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption(statusToSet);
                                }

                                if (itemAvailEntity != null) {
                                    itemDetails.setAvailableStatusId(itemAvailEntity);
                                    historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                }
                            }

                            // only for admin and manager
                            if (loginUserRole.equals("admin") || loginUserRole.equals("manager")) {
                                // only for admin set region
                                if (loginUserRole.equals("admin")) {
                                    if (assignItemDetails.getRegion() != null
                                            && assignItemDetails.getRegion().trim().length() > 0) {
                                        regionEntity = regionDAO
                                                .findByCity(assignItemDetails.getRegion().toLowerCase());
                                        if (regionEntity != null) {
                                            if (itemDetails.getRegion().equals(regionEntity)) {
                                                // if previous and new region are same then leave it
                                            } else {
                                                // if new and previous region is changed then
                                                itemDetails.setRegion(regionEntity);
                                                historyChangedByAdmin.setRegion(regionEntity);
                                            }
                                        } else {
                                            message = ", this region not listed in region list other details update successfully";
                                        }
                                    }

                                } // admin set region section closed

                                // model no
                                if (assignItemDetails.getModelNo() != null
                                        && assignItemDetails.getModelNo().trim().length() > 0) {
                                    if (itemDetails.getModelNo() != null
                                            && itemDetails.getModelNo().trim().length() > 0) {
                                        if (!itemDetails.getModelNo().toLowerCase()
                                                .equals(assignItemDetails.getModelNo().toLowerCase())) {
                                            itemDetails.setModelNo(assignItemDetails.getModelNo());
                                            historyChangedByAdmin.setModelNo(assignItemDetails.getModelNo());
                                        }
                                    } else {
                                        itemDetails.setModelNo(assignItemDetails.getModelNo());
                                        historyChangedByAdmin.setModelNo(assignItemDetails.getModelNo());
                                    }
                                }

                                // system name
                                if (assignItemDetails.getSystemName() != null
                                        && assignItemDetails.getSystemName().trim().length() > 0) {
                                    if (itemDetails.getSystem() != null
                                            && itemDetails.getSystem().trim().length() > 0) {
                                        if (!itemDetails.getSystem().toLowerCase()
                                                .equals(assignItemDetails.getSystemName().toLowerCase())) {
                                            itemDetails.setSystem(assignItemDetails.getSystemName());
                                            historyChangedByAdmin.setSystem(assignItemDetails.getSystemName());
                                        }
                                    } else {
                                        itemDetails.setSystem(assignItemDetails.getSystemName());
                                        historyChangedByAdmin.setSystem(assignItemDetails.getSystemName());
                                    }
                                }

                                // system version
                                if (assignItemDetails.getSystemVersion() != null
                                        && assignItemDetails.getSystemVersion().trim().length() > 0) {
                                    if (itemDetails.getSystem_Version() != null
                                            && itemDetails.getSystem_Version().trim().length() > 0) {
                                        if (!itemDetails.getSystem_Version().toLowerCase()
                                                .equals(assignItemDetails.getSystemVersion().toLowerCase())) {
                                            itemDetails.setSystem_Version(assignItemDetails.getSystemVersion());
                                            historyChangedByAdmin
                                                    .setSystem_Version(assignItemDetails.getSystemVersion());
                                        }
                                    } else {
                                        itemDetails.setSystem_Version(assignItemDetails.getSystemVersion());
                                        historyChangedByAdmin.setSystem_Version(assignItemDetails.getSystemVersion());
                                    }
                                }

                                // module for
                                if (assignItemDetails.getModuleFor() != null
                                        && assignItemDetails.getModuleFor().trim().length() > 0) {
                                    if (itemDetails.getModuleFor() != null
                                            && itemDetails.getModuleFor().trim().length() > 0) {
                                        if (!itemDetails.getModuleFor().toLowerCase()
                                                .equals(assignItemDetails.getModuleFor().toLowerCase())) {
                                            itemDetails.setModuleFor(assignItemDetails.getModuleFor());
                                            historyChangedByAdmin.setModuleFor(assignItemDetails.getModuleFor());
                                        }
                                    } else {
                                        itemDetails.setModuleFor(assignItemDetails.getModuleFor());
                                        historyChangedByAdmin.setModuleFor(assignItemDetails.getModuleFor());
                                    }
                                }

                                // check keyword and sub keyword section for adding
                                if (assignItemDetails.getKeywordName() != null
                                        && assignItemDetails.getKeywordName().trim().length() > 0) {
                                    keywordEntity = keywordDAO
                                            .getByKeyword(assignItemDetails.getKeywordName().toLowerCase());
                                    if (keywordEntity != null) {
                                        if (itemDetails.getKeywordEntity() == null) {// if previous keyword null
                                            itemDetails.setKeywordEntity(keywordEntity);
                                            historyChangedByAdmin.setKeywordEntity(keywordEntity);

                                            // check sub keyword if keyword exist
                                            if (assignItemDetails.getSubKeywordName() != null
                                                    && assignItemDetails.getSubKeywordName().trim().length() > 0) {
                                                subKeywordEntity = subKeywordDAO.getSpecificSubKeyword(keywordEntity,
                                                        assignItemDetails.getSubKeywordName().toLowerCase());
                                                if (subKeywordEntity != null) {
                                                    if (itemDetails.getSubKeyWordEntity() == null) {
                                                        itemDetails.setSubKeyWordEntity(subKeywordEntity);
                                                        historyChangedByAdmin.setSubKeyWordEntity(subKeywordEntity);
                                                    } else {
                                                        if (!itemDetails.getSubKeyWordEntity()
                                                                .equals(subKeywordEntity)) {
                                                            itemDetails.setSubKeyWordEntity(subKeywordEntity);
                                                            historyChangedByAdmin.setSubKeyWordEntity(subKeywordEntity);
                                                        }
                                                    }
                                                } else {
                                                    subKeyMessage = ", sub keyword not listed under this keyword, item saved under keyword only";
                                                }
                                            }
                                        } else { // if previous keyword not null
                                            if (!itemDetails.getKeywordEntity().equals(keywordEntity)) {
                                                itemDetails.setKeywordEntity(keywordEntity);
                                                historyChangedByAdmin.setKeywordEntity(keywordEntity);

                                                // check sub keyword if keyword exist
                                                if (assignItemDetails.getSubKeywordName() != null
                                                        && assignItemDetails.getSubKeywordName().trim().length() > 0) {
                                                    subKeywordEntity = subKeywordDAO.getSpecificSubKeyword(
                                                            keywordEntity,
                                                            assignItemDetails.getSubKeywordName().toLowerCase());
                                                    if (subKeywordEntity != null) {
                                                        if (!itemDetails.getSubKeyWordEntity()
                                                                .equals(subKeywordEntity)) {
                                                            itemDetails.setSubKeyWordEntity(subKeywordEntity);
                                                            historyChangedByAdmin.setSubKeyWordEntity(subKeywordEntity);
                                                        }
                                                    } else {
                                                        subKeyMessage = ", sub keyword not listed under this keyword, item saved under keyword only";
                                                    }
                                                }
                                            } else {
                                                if (assignItemDetails.getSubKeywordName() != null
                                                        && assignItemDetails.getSubKeywordName().trim().length() > 0) {
                                                    subKeywordEntity = subKeywordDAO.getSpecificSubKeyword(
                                                            keywordEntity,
                                                            assignItemDetails.getSubKeywordName().toLowerCase());
                                                    if (subKeywordEntity != null) {
                                                        if (itemDetails.getSubKeyWordEntity() == null) {
                                                            itemDetails.setSubKeyWordEntity(subKeywordEntity);
                                                            historyChangedByAdmin.setSubKeyWordEntity(subKeywordEntity);
                                                        } else {
                                                            if (!itemDetails.getSubKeyWordEntity()
                                                                    .equals(subKeywordEntity)) {
                                                                itemDetails.setSubKeyWordEntity(subKeywordEntity);
                                                                historyChangedByAdmin
                                                                        .setSubKeyWordEntity(subKeywordEntity);
                                                            }
                                                        }
                                                    } else {
                                                        subKeyMessage = ", sub keyword not listed under this keyword, item saved under keyword only";
                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body("keyword is new, First add this keyword");
                                    }
                                } // keyword and subkeyword section closed

                            } // admin and manager section closed

                            boolean isEmpty = true;
                            for (Field field : historyChangedByAdmin.getClass().getDeclaredFields()) {
                                field.setAccessible(true);
                                Object value = field.get(historyChangedByAdmin);
                                if (value != null) {
                                    isEmpty = false;
                                    break;
                                }
                            }

                            if (isEmpty) {
                                // The object is considered empty
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("All field  is upto date" + message + "" + subKeyMessage);
                            } else {
                                // if (historyChangedByAdmin != null) {
                                // setting date and update person name

                                historyChangedByAdmin.setSerial_No(itemDetails.getSerial_No());
                                itemDetails.setEmpEmail(loggedInUserName);
                                historyChangedByAdmin.setUpdatedByEmail(loggedInUserName);

                                itemDetails.setUpdate_Date(ZonedDateTime.now());
                                historyChangedByAdmin.setUpdate_Date(ZonedDateTime.now());

                                // setting table reference for admin history table
                                historyChangedByAdmin.setItemDetailsEntity(itemDetails);
                                List<ItemHistoryUpdatedByAdminEntity> historyChangedByAdminList = new ArrayList<ItemHistoryUpdatedByAdminEntity>();
                                historyChangedByAdminList.add(historyChangedByAdmin);
                                // add reference for admin history table
                                itemDetails.setItemHistoryUpdatedByAdminEntityList(historyChangedByAdminList);

                            }

                            itemDetails = itemDetailsDAO.save(itemDetails);// save to database
                            if (itemDetails != null) {
                                return ResponseEntity.status(HttpStatus.OK)
                                        .body("Item updated succesfully" + message + "" + subKeyMessage);
                            } else {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Something went wrong");
                            }

                        }

                    }

                }

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    /// handling the item updation for the region change only .......
    ///
    /// ...
    /// ...
    /// ...
    ///

    public ResponseEntity<?> updateItemRegionOnly(HttpServletRequest request, RegionUpdateDTO regionDto) {
        String loggedInUserName = null;

        // 1. Authentication Check (Same as your existing code)
        try {
            loggedInUserName = request.getUserPrincipal().getName();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
        }

        try {
            if (loggedInUserName != null && loggedInUserName.length() > 0) {
                adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());

                if (adminUserEntity == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Login");
                }

                // 2. Validate Input
                if (regionDto.getSerialNo() == null || regionDto.getNewRegionName() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Serial No and Region Name are required");
                }

                // 3. Get the Item
                ItemDetailsEntity itemDetails = itemDetailsDAO
                        .getItemDetailsBySerialNo(regionDto.getSerialNo().trim().toLowerCase());

                if (itemDetails == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Serial No not registered");
                }

                // 4. Get the New Region Entity
                RegionEntity newRegionEntity = regionDAO.findByCity(regionDto.getNewRegionName().toLowerCase()); // Assuming
                                                                                                                 // findByCity
                                                                                                                 // is
                                                                                                                 // your
                                                                                                                 // method

                if (newRegionEntity == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Region not found in database");
                }

                // 5. Check if Region is actually different
                if (itemDetails.getRegion() != null && itemDetails.getRegion().equals(newRegionEntity)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Item is already in this region");
                }

                // A. Create the History Object
                ItemHistoryUpdatedByAdminEntity historyChangedByAdmin = new ItemHistoryUpdatedByAdminEntity();

                // B. Set the Region in BOTH Item and History
                itemDetails.setRegion(newRegionEntity);
                historyChangedByAdmin.setRegion(newRegionEntity);

                // C. Set Remark (Optional, but helpful)
                historyChangedByAdmin.setRemark("Quick Update: Region changed to " + regionDto.getNewRegionName());

                // D. Set Metadata (Dates and User)
                ZonedDateTime now = ZonedDateTime.now();

                itemDetails.setEmpEmail(loggedInUserName);
                itemDetails.setUpdate_Date(now);

                historyChangedByAdmin.setSerial_No(itemDetails.getSerial_No());
                historyChangedByAdmin.setUpdatedByEmail(loggedInUserName);
                historyChangedByAdmin.setUpdate_Date(now);

                // E. Link Entities (This causes the History to save automatically)
                historyChangedByAdmin.setItemDetailsEntity(itemDetails);

                // NOTE: Using your specific list logic
                List<ItemHistoryUpdatedByAdminEntity> historyList = new ArrayList<>();
                historyList.add(historyChangedByAdmin);
                itemDetails.setItemHistoryUpdatedByAdminEntityList(historyList);

                // 7. Save to Database
                itemDetails = itemDetailsDAO.save(itemDetails);

                if (itemDetails != null) {
                    return ResponseEntity.status(HttpStatus.OK).body("Region updated successfully");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save changes");
                }

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    // find item via serial no
    public ResponseEntity<?> getItemDetailsBySerialNo(HttpServletRequest request, String serialNo) {

        String loggedInUserName = null;
        try {
            loggedInUserName = request.getUserPrincipal().getName();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
        }
        try {
            if (loggedInUserName != null && loggedInUserName.length() > 0) {
                adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
                if (adminUserEntity == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Login");
                } else {

                    if (serialNo == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Serial no  is required please add serial number first");
                    }
                    String loginUserRole = adminUserEntity.getRoleModel().getRoleName().toLowerCase();
                    itemDetailsEntity = itemDetailsDAO.getItemDetailsBySerialNo(serialNo.trim().toLowerCase());
                    if (itemDetailsEntity != null) {
                        if (!loginUserRole.equals("admin")) {
                            if (!adminUserEntity.getRegionEntity().equals(itemDetailsEntity.getRegion())) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body("this item not belonging to your region");
                            }
                        }
                        ItemDetailsResponse itemDetailsResponse = new ItemDetailsResponse();
                        if (itemDetailsEntity.getId() != null) {
                            itemDetailsResponse.setItemId(itemDetailsEntity.getId());
                        }
                        if (itemDetailsEntity.getSerial_No() != null && !itemDetailsEntity.getSerial_No().isEmpty()) {
                            itemDetailsResponse.setSerialNo(itemDetailsEntity.getSerial_No().trim().toLowerCase());
                        }
                        if (itemDetailsEntity.getBoxNo() != null && !itemDetailsEntity.getBoxNo().isEmpty()) {
                            itemDetailsResponse.setBoxNo(itemDetailsEntity.getBoxNo());
                        }
                        if (itemDetailsEntity.getPartNo() != null && !itemDetailsEntity.getPartNo().isEmpty()) {
                            itemDetailsResponse.setPartNo(itemDetailsEntity.getPartNo());
                        }
                        if (itemDetailsEntity.getModelNo() != null && !itemDetailsEntity.getModelNo().isEmpty()) {
                            itemDetailsResponse.setModelNo(itemDetailsEntity.getModelNo());
                        }
                        if (itemDetailsEntity.getRack_No() != null && !itemDetailsEntity.getRack_No().isEmpty()) {
                            itemDetailsResponse.setRackNo(itemDetailsEntity.getRack_No());
                        }
                        if (itemDetailsEntity.getItemStatusId() != null) {

                            itemDetailsResponse
                                    .setItemStatus(itemDetailsEntity.getItemStatusId().getItemStatus().toUpperCase());
                        }
                        if (itemDetailsEntity.getSpare_Location() != null
                                && !itemDetailsEntity.getSpare_Location().isEmpty()) {
                            itemDetailsResponse.setSpareLocation(itemDetailsEntity.getSpare_Location());
                        }
                        if (itemDetailsEntity.getSystem() != null && !itemDetailsEntity.getSystem().isEmpty()) {
                            itemDetailsResponse.setSystem(itemDetailsEntity.getSystem());
                        }
                        if (itemDetailsEntity.getSystem_Version() != null
                                && !itemDetailsEntity.getSystem_Version().isEmpty()) {
                            itemDetailsResponse.setSystemVersion(itemDetailsEntity.getSystem_Version());
                        }
                        if (itemDetailsEntity.getModuleFor() != null && !itemDetailsEntity.getModuleFor().isEmpty()) {
                            itemDetailsResponse.setModuleFor(itemDetailsEntity.getModuleFor());
                        }
                        if (itemDetailsEntity.getAvailableStatusId() != null) {
                            itemDetailsResponse.setItemAvailability(
                                    itemDetailsEntity.getAvailableStatusId().getItemAvailableOption().toUpperCase());
                        }
                        if (itemDetailsEntity.getItemDescription() != null
                                && !itemDetailsEntity.getItemDescription().isEmpty()) {
                            itemDetailsResponse.setItemDescription(itemDetailsEntity.getItemDescription());
                        }
                        if (itemDetailsEntity.getRemark() != null && !itemDetailsEntity.getRemark().isEmpty()) {
                            itemDetailsResponse.setRemark(itemDetailsEntity.getRemark());
                        }
                        if (itemDetailsEntity.getEmpEmail() != null && !itemDetailsEntity.getEmpEmail().isEmpty()) {
                            itemDetailsResponse.setEmpEmail(itemDetailsEntity.getEmpEmail());
                        }
                        if (itemDetailsEntity.getAddedByEmail() != null
                                && !itemDetailsEntity.getAddedByEmail().isEmpty()) {
                            itemDetailsResponse.setAddedByEmail(itemDetailsEntity.getAddedByEmail());
                        }
                        if (itemDetailsEntity.getPartyName() != null && !itemDetailsEntity.getPartyName().isEmpty()) {
                            itemDetailsResponse.setPartyName(itemDetailsEntity.getPartyName().toUpperCase());
                        }
                        if (itemDetailsEntity.getUpdate_Date() != null) {
                            itemDetailsResponse.setUpdateDate(itemDetailsEntity.getUpdate_Date());
                        }
                        if (itemDetailsEntity.getAdding_Date() != null) {
                            itemDetailsResponse.setAddingDate(itemDetailsEntity.getAdding_Date());
                        }
                        if (itemDetailsEntity.getRegion() != null) {
                            itemDetailsResponse.setRegion(itemDetailsEntity.getRegion().getCity().toUpperCase());
                        }
                        if (itemDetailsEntity.getKeywordEntity() != null) {
                            itemDetailsResponse
                                    .setKeyword(itemDetailsEntity.getKeywordEntity().getKeywordName().toUpperCase());
                        }
                        if (itemDetailsEntity.getSubKeyWordEntity() != null) {
                            itemDetailsResponse.setSubKeyword(
                                    itemDetailsEntity.getSubKeyWordEntity().getSubKeyword().toUpperCase());
                        }

                        return ResponseEntity.status(HttpStatus.OK).body(itemDetailsResponse);
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("item not found");
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }

    }

    // Getting Item Details via keyword subkeyword and region for admin
    public ResponseEntity<?> getItemDetailsByKeyword(HttpServletRequest request,
            GetItemByKeywordRequest requestDetails) {

        String loggedInUserName = null;
        String message = "";
        try {
            loggedInUserName = request.getUserPrincipal().getName();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
        }
        try {
            if (loggedInUserName != null && loggedInUserName.length() > 0) {
                adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
                if (adminUserEntity == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Login");
                } else {

                    RegionEntity region = new RegionEntity();
                    KeywordEntity keyword = new KeywordEntity();
                    SubKeywordEntity subKeyword = new SubKeywordEntity();
                    ItemAvailableStatusOptionEntity availableDetail = new ItemAvailableStatusOptionEntity();
                    ItemStatusOptionEntity itemStatus = new ItemStatusOptionEntity();
                    String partNo = "";
                    String systemName = "";

                    String loginUserRole = adminUserEntity.getRoleModel().getRoleName().toLowerCase();

                    // if (requestDetails == null || requestDetails.getKeyword() == null
                    // || (requestDetails.getKeyword()!=null &&
                    // requestDetails.getKeyword().trim().length() <= 0)) {
                    // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("keyword
                    // required");
                    // }
                    if (loginUserRole.equals("admin")) {// region for admin start
                        if (requestDetails.getRegion() == null || (requestDetails.getRegion() != null
                                && requestDetails.getRegion().trim().length() <= 0)) {

                            message = "Region required for Admin Role";
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
                        } else {
                            // regionEntity=null;
                            region = regionDAO.findByCity(requestDetails.getRegion().trim().toLowerCase());
                            if (region == null) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The region with this "
                                        + requestDetails.getRegion().trim() + "city name is not listed");
                            }
                        }
                    } else {// region for manager start
                        if (adminUserEntity.getRegionEntity() != null) {
                            // regionEntity=null;
                            region = adminUserEntity.getRegionEntity();
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No region assign to you");
                        }
                    }

                    // keyword and SubKeyword
                    if (requestDetails.getKeyword() != null && requestDetails.getKeyword().trim().length() > 0) {
                        // keywordEntity=null;
                        keyword = keywordDAO.getByKeyword(requestDetails.getKeyword().toLowerCase());
                        if (keyword == null) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("Keyword is not listed with this name");
                        } else if (requestDetails.getSubKeyword() != null
                                && requestDetails.getSubKeyword().trim().length() > 0 && keyword != null) {
                            // subKeywordEntity=null;
                            subKeyword = subKeywordDAO.getSpecificSubKeyword(keyword,
                                    requestDetails.getSubKeyword().toLowerCase());
                            if (subKeyword == null || subKeyword.getId() == null) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("Sub keyword id not listed/Not belonging to this keyword");
                            }
                        }
                    }

                    // item Availability
                    if (requestDetails.getItemAvailability() != null
                            && requestDetails.getItemAvailability().trim().length() > 0) {
                        // itemAvailEntity=null;
                        availableDetail = itemAvailableStatusDAO
                                .getStatusDetailsByOption(requestDetails.getItemAvailability().trim().toLowerCase());
                        if (availableDetail == null || availableDetail.getId() == null) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("This Available option is not listed");
                        }
                    }

                    // item Status Option
                    if (requestDetails.getItemStatus() != null && requestDetails.getItemStatus().trim().length() > 0) {
                        // itemStatusOptionEntity=null;
                        itemStatus = itemStatusOptionDAO
                                .getItemStatusOptionDetails(requestDetails.getItemStatus().toLowerCase());
                        if (itemStatus == null || itemStatus.getId() == null) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("This item status option is not listed");
                        }
                    }

                    if (requestDetails.getPartNo() != null && requestDetails.getPartNo().trim().length() > 0) {
                        partNo = requestDetails.getPartNo().trim().toLowerCase();
                    }

                    if (requestDetails.getSystemName() != null && requestDetails.getSystemName().trim().length() > 0) {
                        systemName = requestDetails.getSystemName().trim().toLowerCase();
                    }

                    if (region == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Region required");
                    }
                    if (partNo.isEmpty() && keyword.getId() == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Keyword or PartNo required");
                    }

                    Specification<ItemDetailsEntity> specification = ItemDetailsDynamicQueryBuilder.getItemDetailsQuery(
                            region, keyword, subKeyword, availableDetail, itemStatus, partNo, systemName);
                    List<ItemDetailsEntity> itemList = new ArrayList<>();
                    itemList = itemDetailsDAO.findAll(specification);
                    if (itemList != null && !itemList.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.OK).body(itemList);
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("No data listed, with your specific search");
                    }

                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

    public DashboardSummaryDto getDashboardSummary(boolean isAdmin, String userRegion) {
        List<RegionCountDto> regionCounts = new ArrayList<>();

        // Get status counts (same for admin and non-admin)
        long availableCount = itemDetailsDAO.countAvailableItems();
        long issuedCount = itemDetailsDAO.countIssuedItems();
        long repairingCount = itemDetailsDAO.countRepairingItems();

        if (isAdmin) {
            long total = itemDetailsDAO.countAllItems();

            List<Object[]> rows = itemDetailsDAO.countByRegion(); // returns [region, count] where region may be String
                                                                  // or RegionEntity
            for (Object[] row : rows) {
                String regionName = null;
                long cnt = 0L;

                if (row != null && row.length >= 2) {
                    Object r0 = row[0];
                    Object r1 = row[1];

                    // 1) Extract region name safely
                    if (r0 instanceof String) {
                        regionName = (String) r0;
                    } else if (r0 != null) {
                        try {
                            // If it's a RegionEntity (or proxy), attempt to call getCity() via reflection
                            // to be safe
                            Class<?> clazz = r0.getClass();
                            try {
                                java.lang.reflect.Method m = clazz.getMethod("getCity");
                                Object cityObj = m.invoke(r0);
                                if (cityObj != null) {
                                    regionName = cityObj.toString();
                                }
                            } catch (NoSuchMethodException nsme) {
                                // fallback to toString if getCity not present
                                regionName = r0.toString();
                            }
                        } catch (Exception e) {
                            // fallback to toString on any error
                            regionName = r0.toString();
                        }
                    }

                    // 2) Extract count safely
                    if (r1 instanceof Number) {
                        cnt = ((Number) r1).longValue();
                    } else if (r1 != null) {
                        try {
                            cnt = Long.parseLong(r1.toString());
                        } catch (NumberFormatException ignored) {
                            cnt = 0L;
                        }
                    }
                }

                if (regionName == null || regionName.trim().isEmpty()) {
                    regionName = "UNKNOWN";
                }

                regionCounts.add(new RegionCountDto(regionName, cnt));
            }

            return new DashboardSummaryDto(total, availableCount, issuedCount, repairingCount, regionCounts);
        } else {
            // Non-admin user (manager/employee): only their region counts should be visible
            long total = 0L;

            if (userRegion != null && !userRegion.trim().isEmpty()) {
                String normalized = userRegion.trim().toLowerCase();
                long cnt = itemDetailsDAO.countByRegionName(normalized); // ensure DAO has this method
                total = cnt;
                regionCounts.add(new RegionCountDto(userRegion.trim(), cnt));
            } else {
                total = 0L;
            }

            return new DashboardSummaryDto(total, availableCount, issuedCount, repairingCount, regionCounts);
        }
    }

    private String generateSerialForKeyword(String rawKeyword) {
        if (rawKeyword == null || rawKeyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword required to generate serial");
        }
        String normalized = rawKeyword.trim().toLowerCase();
        long count = itemDetailsDAO.countByKeywordNameLower(normalized);
        String base = rawKeyword.trim().toUpperCase().replaceAll("\\s+", "_");
        String candidate;
        int attempt = 0;
        do {
            long seq = count + 1 + attempt;
            candidate = String.format("%s-%04d", base, seq);
            attempt++;
            if (attempt > 200) {
                throw new RuntimeException("Unable to generate unique serial for keyword: " + rawKeyword);
            }
        } while (itemDetailsDAO.isSerialExist(candidate.trim().toLowerCase()));
        return candidate.trim().toLowerCase();
    }

}
