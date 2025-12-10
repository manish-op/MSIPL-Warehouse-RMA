package com.serverManagement.server.management.service.itemDetails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter; // <-- ADDED import
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.serverManagement.server.management.CSV.CSVFileToItemListConverter;
import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.itemDetails.ItemDetailsDAO;
import com.serverManagement.server.management.dao.region.RegionDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsDTOForCSV;
import com.serverManagement.server.management.entity.itemDetails.ItemDetailsEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class CSVConverterService {

    private final AdminUserDAO adminUserDAO;
    private final RegionDAO regionDAO;
    private final ItemDetailsDAO itemDetailsDAO;
    private final CSVFileToItemListConverter cSVFileToItemListConverter;
    private AdminUserEntity adminUserEntity;


    // Centralized headers for consistency between export and import -->
    private static final String[] CANONICAL_FIELD_NAMES = {
            "serialNo", "rackNo", "boxNo", "spareLocation", "partNo", "modelNo", "keyword",
            "subKeyword", "system", "systemVersion", "moduleFor", "itemDescription", "partyName", "remark",
            "region", "itemStatus", "availableStatus", "lastUpdatedByEmail", "addedByEmail", "update_Date", "adding_Date"
    };

    private static final String[] DISPLAY_HEADERS = {
            "SerialNo", "RackNo", "BoxNo", "SpareLocation", "PartNo", "ModelNo", "Keyword",
            "SubKeyword", "System", "SystemVersion", "ModuleFor", "ItemDescription", "PartyName", "Remark",
            "Region", "ItemStatus", "AvailableStatus", "LastUpdatedByEmail", "AddedByEmail", "Update_Date", "Adding_Date"
    };


    private static final Set<String> MANDATORY_FIELDS = Set.of(
            "SerialNo", "RackNo", "Keyword", "Region", "ItemStatus", "AvailableStatus"
    );

    public CSVConverterService(AdminUserDAO adminUserDAO, RegionDAO regionDAO, ItemDetailsDAO itemDetailsDAO,
                               CSVFileToItemListConverter cSVFileToItemListConverter) {
        super();
        this.adminUserDAO = adminUserDAO;
        this.regionDAO = regionDAO;
        this.itemDetailsDAO = itemDetailsDAO;
        this.cSVFileToItemListConverter = cSVFileToItemListConverter;

    }


    //method to add item via csv file

    public ResponseEntity<?> addItemsViaCSV(HttpServletRequest request, MultipartFile csvFile)
            throws IOException, Exception {

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
                    String loginUserRole = adminUserEntity.getRoleModel().getRoleName().toLowerCase();
                    if (loginUserRole.equals("admin")) {// check for Admin or manager login

                        if (csvFile == null) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                    "SerialNo or Keyword or Region or RackNo or AvailableStatus or ItemStatus must not be null");
                        } else {

                            Set<ItemDetailsEntity> itemDetailsSet = new HashSet<>();
                            List<ItemDetailsEntity> itemDetailsList = new ArrayList<>();
                            itemDetailsSet = cSVFileToItemListConverter.csvToItemSet(csvFile, adminUserEntity);

                            if (!itemDetailsSet.isEmpty()) {
                                itemDetailsList = itemDetailsDAO.saveAll(itemDetailsSet);

                                if (!itemDetailsList.isEmpty()) {
                                    return ResponseEntity.status(HttpStatus.OK).body("item List saved SuccessFully");
                                } else {
                                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .body("Internal Server error");
                                }
                            } else {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("file not contain any new item with  unique Serial no." + itemDetailsSet);
                            }

                        }
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only admin can perform this task");
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not loggedIn");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Something went wrong");
        }
    }



    // This is the completely updated method for generating the CSV file -->

    public ResponseEntity<?> getRegionItemCsv(HttpServletRequest request, String regionName) throws Exception {
        String loggedInUserName = null;
        try {
            loggedInUserName = request.getUserPrincipal().getName();
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated, Login first");
        }

        try {
            if (loggedInUserName != null && !loggedInUserName.isEmpty()) {
                adminUserEntity = adminUserDAO.findByEmail(loggedInUserName.toLowerCase());
                if (adminUserEntity == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Details Not Available");
                }

                String loginUserRole = adminUserEntity.getRoleModel().getRoleName().toLowerCase();
                if (!loginUserRole.equals("admin") && !loginUserRole.equals("manager")) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only admin or manager can perform this task");
                }

                RegionEntity regionEntity;
                if (loginUserRole.equals("admin")) {
                    if (regionName == null || regionName.trim().isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please specify the region");
                    }
                    regionEntity = regionDAO.findByCity(regionName.trim().toLowerCase());
                    if (regionEntity == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This region is not listed");
                    }
                } else { // Manager role
                    regionEntity = adminUserEntity.getRegionEntity();
                    if (regionEntity == null || regionEntity.getId() == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No region is assigned to you");
                    }
                }

                List<ItemDetailsDTOForCSV> regionItemList = itemDetailsDAO.getRegionAllItemList(regionEntity);
                if (regionItemList.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No data available to export.");
                }

                regionItemList.sort((e1, e2) -> e1.getKeywordName().compareTo(e2.getKeywordName()));

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(outputStream);

                String[] exportHeaders = Arrays.stream(DISPLAY_HEADERS)
                        .map(header -> MANDATORY_FIELDS.contains(header) ? header + "*" : header)
                        .toArray(String[]::new);

                CSVFormat format = CSVFormat.EXCEL.builder().setHeader(exportHeaders).build();

                try (final CSVPrinter csvPrinter = new CSVPrinter(writer, format)) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    for (ItemDetailsDTOForCSV itemDetail : regionItemList) {
                        String formattedUpdateDate = itemDetail.getUpdate_Date() != null ? itemDetail.getUpdate_Date().format(formatter) : "";
                        String formattedAddingDate = itemDetail.getAdding_Date() != null ? itemDetail.getAdding_Date().format(formatter) : "";

                        csvPrinter.printRecord(
                                itemDetail.getSerial_No(),
                                itemDetail.getRack_No(),
                                itemDetail.getBoxNo(),
                                itemDetail.getSpare_Location(),
                                itemDetail.getPartNo(),
                                itemDetail.getModelNo(),
                                itemDetail.getKeywordName(),
                                itemDetail.getSubKeyword(),
                                itemDetail.getSystem(),
                                itemDetail.getSystem_Version(),
                                itemDetail.getModuleFor(),
                                itemDetail.getItemDescription(),
                                itemDetail.getPartyName(),
                                itemDetail.getRemark(),
                                itemDetail.getCity(),
                                itemDetail.getItemStatus(),
                                itemDetail.getItemAvailableOption(),
                                itemDetail.getEmpEmail(),
                                itemDetail.getAddedByEmail(),
                                formattedUpdateDate,
                                formattedAddingDate
                        );
                    }
                }

                writer.flush();
                InputStreamResource data = new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray()));

                String city = regionEntity.getCity(); // This works for both Admin and Manager

                // 2. Set a default name if city is null or empty
                String baseName = "ItemList"; // Default name
                if (city != null && !city.trim().isEmpty()) {
                    baseName = city.trim();
                }

                // 3. Sanitize the baseName to be a safe filename
                //    This replaces spaces and any non-alphanumeric (except dots/hyphens) with an underscore
                String safeFilename = baseName.replaceAll("[^a-zA-Z0-9.-]", "_");

                // 4. Create the final filename
                String dynamicFilename = safeFilename + ".csv";

                // 5. Use the new dynamic filename in the header
                //    We add quotes around the filename, which is best practice
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dynamicFilename + "\"")
                        .contentType(MediaType.parseMediaType("application/csv"))
                        .body(data);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not loggedIn");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Something went wrong during CSV export.");
        }
    }
}