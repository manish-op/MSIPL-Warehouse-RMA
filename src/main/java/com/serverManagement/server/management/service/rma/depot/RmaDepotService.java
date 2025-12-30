package com.serverManagement.server.management.service.rma.depot;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serverManagement.server.management.dao.rma.depot.DepotDispatchDAO;
import com.serverManagement.server.management.dao.rma.common.ProductValueDAO;
import com.serverManagement.server.management.dao.rma.common.RmaAuditLogDAO;
import com.serverManagement.server.management.dao.rma.request.RmaItemDAO;
import com.serverManagement.server.management.dao.rma.request.RmaRequestDAO;
import com.serverManagement.server.management.dao.rma.common.TransporterDAO;
import com.serverManagement.server.management.dto.rma.depot.DeliveryChallanRequest;
import com.serverManagement.server.management.entity.rma.depot.DepotDispatchEntity;
import com.serverManagement.server.management.entity.rma.common.ProductValueEntity;
import com.serverManagement.server.management.entity.rma.common.RmaAuditLogEntity;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;
import com.serverManagement.server.management.entity.rma.request.RmaRequestEntity;
import com.serverManagement.server.management.entity.rma.common.TransporterEntity;

@Service
public class RmaDepotService {

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @Autowired
    private DepotDispatchDAO depotDispatchDAO;

    @Autowired
    private TransporterDAO transporterDAO;

    @Autowired
    private RmaAuditLogDAO rmaAuditLogDAO;

    @Autowired
    private RmaRequestDAO rmaRequestDAO;

    @Autowired
    private ProductValueDAO productValueDAO;

    /**
     * Save DC Details (Transporter, etc) handling for logic previously in
     * monolithic service
     * Used by RmaDocumentController
     */
    @Transactional
    public void saveDcDetails(DeliveryChallanRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty())
            return;

        // Collect Item IDs
        List<Long> itemIds = request.getItems().stream()
                .map(DeliveryChallanRequest.DcItemDto::getItemId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (itemIds.isEmpty())
            return;

        List<RmaItemEntity> rmaItems = rmaItemDAO.findAllById(itemIds);
        if (rmaItems.isEmpty())
            return;

        // Use existing dispatch from first item if available, or create new
        DepotDispatchEntity dispatch = rmaItems.get(0).getDepotDispatch();
        if (dispatch == null) {
            dispatch = new DepotDispatchEntity();
            dispatch.setCreatedAt(ZonedDateTime.now());
        }

        // Update fields
        if (request.getTransporterId() != null && !request.getTransporterId().isEmpty()) {

            Optional<TransporterEntity> transParams = Optional.empty();
            if (request.getTransporterName() != null) {
                transParams = transporterDAO.findByName(request.getTransporterName());
            }

            if (transParams.isPresent()) {
                dispatch.setTransporter(transParams.get());
            } else if (request.getTransporterName() != null && request.getTransporterId() != null) {
                // Create new Transporter if name provided but not found, and ID is available
                TransporterEntity newTransporter = new TransporterEntity();
                newTransporter.setName(request.getTransporterName());
                newTransporter.setTransporterId(request.getTransporterId()); // The business ID entered by user
                newTransporter = transporterDAO.save(newTransporter);
                dispatch.setTransporter(newTransporter);
            }
        }

        dispatch.setCourierName(request.getTransporterName()); // Legacy field

        // Save DC Number so auto-increment works
        if (request.getDcNo() != null && !request.getDcNo().isEmpty()) {
            dispatch.setDcNo(request.getDcNo());
            dispatch.setDispatchDate(ZonedDateTime.now());
        }

        // Save dispatch
        dispatch = depotDispatchDAO.save(dispatch);

        // Link items
        for (RmaItemEntity item : rmaItems) {
            item.setDepotDispatch(dispatch);
        }
        rmaItemDAO.saveAll(rmaItems);

        // Save Product Rates / Values for future auto-fill
        if (request.getItems() != null) {
            for (DeliveryChallanRequest.DcItemDto itemDto : request.getItems()) {
                try {
                    String rate = itemDto.getRate();
                    String product = itemDto.getProduct();
                    String model = itemDto.getModel();

                    if (rate != null && !rate.trim().isEmpty() && product != null && !product.trim().isEmpty()) {
                        // Normalize
                        product = product.trim();
                        model = (model != null) ? model.trim() : "";
                        rate = rate.trim();

                        // Check if exists
                        Optional<ProductValueEntity> startVal = productValueDAO
                                .findByProductAndModel(product, model);

                        ProductValueEntity valEntity;
                        if (startVal.isPresent()) {
                            valEntity = startVal.get();
                            valEntity.setValue(rate); // update with new rate
                            valEntity.setLastUpdated(ZonedDateTime.now());
                        } else {
                            valEntity = new ProductValueEntity();
                            valEntity.setProduct(product);
                            valEntity.setModel(model);
                            valEntity.setValue(rate);
                            valEntity.setLastUpdated(ZonedDateTime.now());
                        }
                        productValueDAO.save(valEntity);
                    }
                } catch (Exception e) {
                    // Log but don't fail the transaction just for rate saving
                    e.printStackTrace();
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Depot Operations (Migrated from RmaDepotOperationsController)
    // -------------------------------------------------------------------------

    @Transactional
    public void markAsReceived(List<Long> itemIds, String userEmail, String userName, String ipAddress) {
        if (itemIds == null || itemIds.isEmpty())
            throw new IllegalArgumentException("No items provided");

        List<RmaItemEntity> items = rmaItemDAO.findAllById(itemIds);
        if (items.isEmpty())
            throw new IllegalArgumentException("No items found");

        for (RmaItemEntity item : items) {
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }

            String oldStage = item.getDepotStage();
            item.setDepotStage("AT_DEPOT_RECEIVED");
            item.setRepairStatus("RECEIVED_AT_DEPOT");

            createAuditLog(item, "DEPOT_STATUS_CHANGED", "Stage: " + (oldStage != null ? oldStage : "UNKNOWN"),
                    "Stage: AT_DEPOT_RECEIVED", userEmail, userName, ipAddress,
                    "Item marked as received at Bangalore depot");
        }
        rmaItemDAO.saveAll(items);
    }

    @Transactional
    public void markAsRepaired(List<Long> itemIds, String repairStatus, String userEmail, String userName,
            String ipAddress) {
        if (itemIds == null || itemIds.isEmpty())
            throw new IllegalArgumentException("No items provided");

        List<RmaItemEntity> items = rmaItemDAO.findAllById(itemIds);
        if (items.isEmpty())
            throw new IllegalArgumentException("No items found");

        for (RmaItemEntity item : items) {
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }

            String oldStage = item.getDepotStage();
            item.setDepotStage("AT_DEPOT_REPAIRED");

            String status = repairStatus;
            if (status == null || status.trim().isEmpty()) {
                status = "REPAIRED";
            }
            item.setRepairStatus(status + "_AT_DEPOT");

            createAuditLog(item, "DEPOT_STATUS_CHANGED", "Stage: " + (oldStage != null ? oldStage : "UNKNOWN"),
                    "Stage: AT_DEPOT_REPAIRED (" + status + ")", userEmail, userName, ipAddress,
                    "Item marked as " + status + " at Depot");
        }
        rmaItemDAO.saveAll(items);
    }

    @Transactional
    public void markReceivedAtGurgaon(List<Long> itemIds, String userEmail, String userName, String ipAddress) {
        if (itemIds == null || itemIds.isEmpty())
            throw new IllegalArgumentException("No items provided");

        List<RmaItemEntity> items = rmaItemDAO.findAllById(itemIds);
        if (items.isEmpty())
            throw new IllegalArgumentException("No items found");

        for (RmaItemEntity item : items) {
            if (!"DEPOT".equalsIgnoreCase(item.getRepairType())) {
                continue;
            }

            String oldStage = item.getDepotStage();
            item.setDepotStage("GGN_RECEIVED_FROM_DEPOT");
            item.setRepairStatus("RECEIVED_AT_GURGAON");

            createAuditLog(item, "DEPOT_STATUS_CHANGED", "Stage: " + (oldStage != null ? oldStage : "UNKNOWN"),
                    "Stage: GGN_RECEIVED_FROM_DEPOT", userEmail, userName, ipAddress,
                    "Repaired depot item received at Gurgaon");
        }
        rmaItemDAO.saveAll(items);
    }

    @Transactional
    public String markFaultyAndCreateNewRma(Long itemId, String userEmail, String userName, String ipAddress) {
        if (itemId == null)
            throw new IllegalArgumentException("Item ID cannot be null");

        RmaItemEntity originalItem = rmaItemDAO.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Original item not found"));

        // 1. Mark old item as FAULTY
        String oldStage = originalItem.getDepotStage();
        String oldRmaNo = originalItem.getRmaNo() != null ? originalItem.getRmaNo()
                : (originalItem.getRmaRequest() != null ? originalItem.getRmaRequest().getRequestNumber() : null);

        originalItem.setDepotStage("GGN_RETURNED_FAULTY");
        originalItem.setDepotCycleClosed(Boolean.TRUE);
        originalItem.setRmaStatus("CLOSED_FAULTY");
        originalItem.setRemarks("Closed as Faulty returned from Depot. New RMA generated.");
        rmaItemDAO.save(originalItem);

        createAuditLog(originalItem, "DEPOT_RETURNED_FAULTY", "Stage: " + oldStage, "Status: CLOSED_FAULTY", userEmail,
                userName, ipAddress, "Item marked faulty. Cycle closed.");

        // 2. CREATE NEW RMA REQUEST
        RmaRequestEntity originalRequest = originalItem.getRmaRequest();
        RmaRequestEntity newRequest = new RmaRequestEntity();

        String newRequestNumber = "RMA-" + (System.currentTimeMillis() / 1000);
        newRequest.setRequestNumber(newRequestNumber);

        if (originalRequest != null) {
            newRequest.setCustomer(originalRequest.getCustomer());
            newRequest.setCompanyName(originalRequest.getCompanyName());
            newRequest.setContactName(originalRequest.getContactName());
            newRequest.setEmail(originalRequest.getEmail());
            newRequest.setMobile(originalRequest.getMobile());
            newRequest.setTelephone(originalRequest.getTelephone());
            newRequest.setReturnAddress(originalRequest.getReturnAddress());
        }

        newRequest.setCreatedDate(ZonedDateTime.now());
        newRequest.setCreatedByEmail(userEmail);

        rmaRequestDAO.save(newRequest);

        // 3. CREATE NEW RMA ITEM linked to new request
        RmaItemEntity newItem = new RmaItemEntity();
        newItem.setRmaRequest(newRequest);
        newItem.setRmaNo(newRequestNumber);
        newItem.setSerialNo(originalItem.getSerialNo());
        newItem.setProduct(originalItem.getProduct());
        newItem.setModel(originalItem.getModel());
        newItem.setRepairStatus(originalItem.getRepairStatus());

        // Copy technical details
        newItem.setFmUlatex(originalItem.getFmUlatex());
        newItem.setCodeplug(originalItem.getCodeplug());
        newItem.setFlashCode(originalItem.getFlashCode());
        newItem.setEncryption(originalItem.getEncryption());
        newItem.setFirmwareVersion(originalItem.getFirmwareVersion());
        newItem.setLowerFirmwareVersion(originalItem.getLowerFirmwareVersion());
        newItem.setInvoiceNo(originalItem.getInvoiceNo());
        newItem.setDateCode(originalItem.getDateCode());

        newItem.setFaultDescription("Re-created from Faulty Depot Return. Ref Old RMA: " + oldRmaNo);
        newItem.setRepairType("DEPOT");
        newItem.setDepotStage("PENDING_DISPATCH_TO_DEPOT");
        newItem.setRmaStatus("OPEN");

        rmaItemDAO.save(newItem);

        return newRequestNumber;
    }

    private void createAuditLog(RmaItemEntity item, String action, String oldVal, String newVal, String email,
            String name, String ip, String remarks) {
        RmaAuditLogEntity auditLog = new RmaAuditLogEntity();
        auditLog.setRmaItemId(item.getId());
        String rmaNo = item.getRmaNo() != null ? item.getRmaNo()
                : (item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : null);
        auditLog.setRmaNo(rmaNo);
        auditLog.setAction(action);
        auditLog.setOldValue(oldVal);
        auditLog.setNewValue(newVal);
        auditLog.setPerformedByEmail(email);
        auditLog.setPerformedByName(name);
        auditLog.setIpAddress(ip);
        auditLog.setRemarks(remarks);
        rmaAuditLogDAO.save(auditLog);
    }
}
