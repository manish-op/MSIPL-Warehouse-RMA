package com.serverManagement.server.management.service.rma.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.rma.common.ProductValueDAO;
import com.serverManagement.server.management.dto.rma.common.ProductModelDTO;
import com.serverManagement.server.management.entity.rma.common.ProductValueEntity;
import com.serverManagement.server.management.dao.keyword.KeywordDAO;
import com.serverManagement.server.management.dao.rma.common.RmaAuditLogDAO;
import com.serverManagement.server.management.dto.rma.common.ProductCatalogDTO;
import com.serverManagement.server.management.entity.rma.common.RmaAuditLogEntity;

@Service
public class RmaCommonService {

    @Autowired
    private RmaAuditLogDAO rmaAuditLogDAO;

    @Autowired
    private KeywordDAO keywordDAO;

    @Autowired
    private ProductValueDAO productValueDAO;

    /**
     * Get product catalog from KeywordEntity for RMA form dropdown
     */
    public ResponseEntity<?> getProductCatalog() {
        try {
            List<String> keywords = keywordDAO.getKeywordList();
            List<ProductCatalogDTO> catalog = new ArrayList<>();

            for (String keyword : keywords) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    catalog.add(new ProductCatalogDTO(keyword, "", ""));
                }
            }

            // Add a default "Other" option if not already present
            boolean hasOther = catalog.stream().anyMatch(p -> "Other".equalsIgnoreCase(p.getName()));
            if (!hasOther) {
                catalog.add(new ProductCatalogDTO("Other", "", ""));
            }

            return ResponseEntity.ok(catalog);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch product catalog: " + e.getMessage());
        }
    }

    /**
     * Get all audit logs for viewing the audit trail
     */
    public ResponseEntity<?> getAuditLogs() {
        try {
            List<RmaAuditLogEntity> logs = rmaAuditLogDAO.findAllOrderByPerformedAtDesc();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch audit logs");
        }
    }

    /**
     * Get audit logs for a specific RMA item
     */
    public ResponseEntity<?> getAuditLogsByItemId(Long itemId) {
        try {
            List<RmaAuditLogEntity> logs = rmaAuditLogDAO.findByRmaItemId(itemId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch item audit logs");
        }
    }

    /**
     * Get saved rates for a list of products/models
     * Used for DC generation
     */
    public ResponseEntity<?> getProductRates(List<ProductModelDTO> items) {
        try {
            Map<String, String> rates = new HashMap<>();
            // Optimize: find all by (product, model) - but JPA might not support "IN"
            // tuples easily without custom query.
            // For now, iterate. If list is small (DC items usually < 20), it's fine.
            // Or we could fetch ALL values for these products?

            for (ProductModelDTO item : items) {
                if (item.getProduct() == null)
                    continue;
                String model = item.getModel() == null ? "" : item.getModel().trim();
                String product = item.getProduct().trim();

                String key = product + "::" + model;
                ProductValueEntity entity = productValueDAO.findByProductAndModel(product, model).orElse(null);
                if (entity != null) {
                    rates.put(key, entity.getValue());
                }
            }
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch product rates");
        }
    }

}
