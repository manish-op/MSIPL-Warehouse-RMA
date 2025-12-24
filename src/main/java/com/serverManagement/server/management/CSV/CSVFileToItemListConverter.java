package com.serverManagement.server.management.CSV;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
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

@Component
public class CSVFileToItemListConverter {

    private final ItemDetailsDAO itemDetailsDAO;
    private final RegionDAO regionDAO;
    private final KeywordDAO keywordDAO;
    private final SubKeywordDAO subKeywordDAO;
    private final ItemAvailableStatusOptionDAO itemAvailableStatusDAO;
    private final ItemStatusOptionDAO itemStatusOptionDAO;

    public CSVFileToItemListConverter(ItemDetailsDAO itemDetailsDAO, RegionDAO regionDAO, KeywordDAO keywordDAO,
            SubKeywordDAO subKeywordDAO, ItemAvailableStatusOptionDAO itemAvailableStatusDAO,
            ItemStatusOptionDAO itemStatusOptionDAO) {
        super();
        this.itemDetailsDAO = itemDetailsDAO;
        this.regionDAO = regionDAO;
        this.keywordDAO = keywordDAO;
        this.subKeywordDAO = subKeywordDAO;
        this.itemAvailableStatusDAO = itemAvailableStatusDAO;
        this.itemStatusOptionDAO = itemStatusOptionDAO;
    }

    private static final String[] CANONICAL_FIELD_NAMES = {
            "serialNo", "rackNo", "boxNo", "spareLocation", "partNo", "modelNo", "keyword",
            "subKeyword", "system", "systemVersion", "moduleFor", "itemDescription", "partyName", "remark",
            "region", "itemStatus", "availableStatus", "lastUpdatedByEmail", "addedByEmail", "update_Date",
            "adding_Date"
    };

    public Set<ItemDetailsEntity> csvToItemSet(MultipartFile csvFile, AdminUserEntity userDetails)
            throws IOException, Exception {

        Set<ItemDetailsEntity> setOfItemDetails = new HashSet<ItemDetailsEntity>();

        try (Reader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {

            // --- : Use ColumnPositionMappingStrategy ---
            ColumnPositionMappingStrategy<AddItemRequest> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(AddItemRequest.class);

            strategy.setColumnMapping(CANONICAL_FIELD_NAMES);

            CsvToBean<AddItemRequest> csvToBean = new CsvToBeanBuilder<AddItemRequest>(reader)
                    .withMappingStrategy(strategy)
                    .withSkipLines(1)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            /// start testing
            String loggedInUserName = userDetails.getEmail().toLowerCase();
            String loginUserRole = userDetails.getRoleModel().getRoleName().trim().toLowerCase();
            RegionEntity forManagerRegion = userDetails.getRegionEntity();
            if (loginUserRole.equals("manager")) {
                if (forManagerRegion == null) {
                    throw new Exception("No region assign to you");
                }
            }
            for (AddItemRequest addComponent : csvToBean.parse()) {

                try {

                    // --- : Use isBlank() for clearer checks ---
                    if (addComponent == null ||
                            addComponent.getSerialNo() == null || addComponent.getSerialNo().isBlank() ||
                            addComponent.getKeyword() == null || addComponent.getKeyword().isBlank()) {
                        // System.out.println("Skipping row: SerialNo or Keyword is blank.");
                        continue; // Skip rows without mandatory fields
                    }

                    boolean serialAvailable = itemDetailsDAO
                            .isSerialExist(addComponent.getSerialNo().trim().toLowerCase());
                    if (serialAvailable) {// check for duplicate serial no
                        // System.out.println("Skipping row: SerialNo already exists: " +
                        // addComponent.getSerialNo());
                        continue;
                    } else {
                        ItemDetailsEntity componentDetails = new ItemDetailsEntity();
                        ItemHistoryUpdatedByAdminEntity historyChangedByAdmin = new ItemHistoryUpdatedByAdminEntity();

                        if (addComponent.getSerialNo() != null && !addComponent.getSerialNo().isEmpty()) {
                            componentDetails.setSerial_No(addComponent.getSerialNo().trim().toLowerCase());
                            historyChangedByAdmin.setSerial_No(addComponent.getSerialNo().trim().toLowerCase());
                        }
                        // region adding section open
                        if (loginUserRole.equals("admin")) {
                            if (addComponent.getRegion() != null && addComponent.getRegion().trim().length() > 0) {

                                RegionEntity regionEntity = regionDAO
                                        .findByCity(addComponent.getRegion().toLowerCase());
                                if (regionEntity != null) {
                                    componentDetails.setRegion(regionEntity);
                                    historyChangedByAdmin.setRegion(regionEntity);
                                } else {
                                    // System.out.println("Skipping row: Region not found in DB: " +
                                    // addComponent.getRegion());
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        } else {
                            componentDetails.setRegion(forManagerRegion);
                            historyChangedByAdmin.setRegion(forManagerRegion);
                        }
                        // region adding section closed

                        // check keyword and sub keyword section for adding
                        if (addComponent.getKeyword() != null && addComponent.getKeyword().trim().length() > 0) {

                            KeywordEntity keywordEntity = keywordDAO
                                    .getByKeyword(addComponent.getKeyword().toLowerCase());

                            if (keywordEntity != null) {
                                componentDetails.setKeywordEntity(keywordEntity);
                                historyChangedByAdmin.setKeywordEntity(keywordEntity);
                                // check sub keyword if keyword exist

                                if (addComponent.getSubKeyword() != null
                                        && addComponent.getSubKeyword().trim().length() > 0) {

                                    SubKeywordEntity subKeywordEntity = subKeywordDAO.getSpecificSubKeyword(
                                            keywordEntity, addComponent.getSubKeyword().toLowerCase().trim());

                                    if (subKeywordEntity != null) {
                                        componentDetails.setSubKeyWordEntity(subKeywordEntity);
                                        historyChangedByAdmin.setSubKeyWordEntity(subKeywordEntity);

                                    } else {

                                    }
                                }
                            } else {
                                // System.out.println("Skipping row: Keyword not found in DB: " +
                                // addComponent.getKeyword());
                                continue;
                            }

                        }

                        // keyword and sub-keyword section closed
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
                            ItemStatusOptionEntity itemStatusOptionEntity = itemStatusOptionDAO
                                    .getItemStatusOptionDetails(addComponent.getItemStatus().toLowerCase());
                            if (itemStatusOptionEntity != null) {
                                componentDetails.setItemStatusId(itemStatusOptionEntity);
                                historyChangedByAdmin.setItemStatusId(itemStatusOptionEntity);
                            } else {
                                itemStatusOptionEntity = itemStatusOptionDAO.getItemStatusOptionDetails("new");
                                if (itemStatusOptionEntity != null) {
                                    componentDetails.setItemStatusId(itemStatusOptionEntity);
                                    historyChangedByAdmin.setItemStatusId(itemStatusOptionEntity);
                                }

                            }
                        } else {
                            ItemStatusOptionEntity itemStatusOptionEntity = itemStatusOptionDAO
                                    .getItemStatusOptionDetails("new");
                            if (itemStatusOptionEntity != null) {
                                componentDetails.setItemStatusId(itemStatusOptionEntity);
                                historyChangedByAdmin.setItemStatusId(itemStatusOptionEntity);
                            } else {
                                continue;
                            }
                        }
                        // rack no
                        if (addComponent.getRackNo() != null && addComponent.getRackNo().trim().length() > 0) {
                            componentDetails.setRack_No(addComponent.getRackNo());
                            historyChangedByAdmin.setRack_No(addComponent.getRackNo());
                        }
                        // spare location
                        if (addComponent.getSpareLocation() != null
                                && addComponent.getSpareLocation().trim().length() > 0) {
                            componentDetails.setSpare_Location(addComponent.getSpareLocation().trim());
                            historyChangedByAdmin.setSpare_Location(addComponent.getSpareLocation());
                        }
                        // system name
                        if (addComponent.getSystem() != null && addComponent.getSystem().trim().length() > 0) {
                            componentDetails.setSystem(addComponent.getSystem());
                            historyChangedByAdmin.setSystem(addComponent.getSystem());
                        }
                        // party name
                        if (addComponent.getPartyName() != null && addComponent.getPartyName().trim().length() > 0) {
                            componentDetails.setPartyName(addComponent.getPartyName());
                            historyChangedByAdmin.setPartyName(addComponent.getPartyName());
                        }
                        // remark
                        if (addComponent.getRemark() != null && addComponent.getRemark().trim().length() > 0) {
                            componentDetails.setRemark(addComponent.getRemark());
                            historyChangedByAdmin.setRemark(addComponent.getRemark());
                        }
                        // system version
                        if (addComponent.getSystemVersion() != null
                                && addComponent.getSystemVersion().trim().length() > 0) {
                            componentDetails.setSystem_Version(addComponent.getSystemVersion());
                            historyChangedByAdmin.setSystem_Version(addComponent.getSystemVersion());
                        }
                        // availability status
                        if (addComponent.getAvailableStatus() != null
                                && addComponent.getAvailableStatus().trim().length() > 0) {
                            if (addComponent.getAvailableStatus().toLowerCase().equals("issue")
                                    || addComponent.getAvailableStatus().toLowerCase().equals("available")) {
                                ItemAvailableStatusOptionEntity itemAvailEntity = itemAvailableStatusDAO
                                        .getStatusDetailsByOption(addComponent.getAvailableStatus().toLowerCase());
                                if (itemAvailEntity != null) {
                                    componentDetails.setAvailableStatusId(itemAvailEntity);
                                    historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                } else {
                                    itemAvailEntity = itemAvailableStatusDAO.getStatusDetailsByOption("available");
                                    if (itemAvailEntity != null) {
                                        componentDetails.setAvailableStatusId(itemAvailEntity);
                                        historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                    }
                                }
                            } else {
                                ItemAvailableStatusOptionEntity itemAvailEntity = itemAvailableStatusDAO
                                        .getStatusDetailsByOption("available");
                                if (itemAvailEntity != null) {
                                    componentDetails.setAvailableStatusId(itemAvailEntity);
                                    historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                                }
                            }
                        } else {
                            ItemAvailableStatusOptionEntity itemAvailEntity = itemAvailableStatusDAO
                                    .getStatusDetailsByOption("available");
                            if (itemAvailEntity != null) {
                                componentDetails.setAvailableStatusId(itemAvailEntity);
                                historyChangedByAdmin.setAvailableStatusId(itemAvailEntity);
                            } else {
                                continue;
                            }
                        }

                        componentDetails.setAddedByEmail(loggedInUserName);
                        historyChangedByAdmin.setUpdatedByEmail(loggedInUserName);

                        componentDetails.setEmpEmail(loggedInUserName);

                        componentDetails.setAdding_Date(ZonedDateTime.now());
                        historyChangedByAdmin.setUpdate_Date(ZonedDateTime.now());

                        componentDetails.setUpdate_Date(ZonedDateTime.now());

                        if (historyChangedByAdmin != null) {
                            // setting table reference for Admin history table
                            historyChangedByAdmin.setItemDetailsEntity(componentDetails);
                            List<ItemHistoryUpdatedByAdminEntity> historyChangedByAdminList = new ArrayList<ItemHistoryUpdatedByAdminEntity>();
                            historyChangedByAdminList.add(historyChangedByAdmin);
                            // add reference for admin history table
                            componentDetails.setItemHistoryUpdatedByAdminEntityList(historyChangedByAdminList);
                        }
                        // ... (End of your existing logic) ...

                        setOfItemDetails.add(componentDetails);

                    }

                    // --- : Don't stop the whole process for one bad row ---
                } catch (Exception e) {
                    System.err.println("Skipping row due to error: " + e.getMessage());
                    e.printStackTrace();
                    continue; // Move to the next item in csvToBean.parse()
                }

            }

            return setOfItemDetails;

        } catch (IOException e) {
            throw new IOException("Something went wrong at the time of conversion");
        }
    }
}