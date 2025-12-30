package com.serverManagement.server.management.service.rma.analytics;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.serverManagement.server.management.dao.rma.RmaItemDAO;
import com.serverManagement.server.management.dao.rma.RmaRequestDAO;
import com.serverManagement.server.management.dto.rma.dashboard.DailyTrendDto;
import com.serverManagement.server.management.dto.rma.dashboard.RmaDashboardStatsDto;
import com.serverManagement.server.management.dto.rma.dashboard.TatComplianceReportDto;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;
import com.serverManagement.server.management.entity.rma.request.RmaRequestEntity;

@Service
public class RmaDashboardService {

    @Autowired
    private RmaRequestDAO rmaRequestDAO;

    @Autowired
    private RmaItemDAO rmaItemDAO;

    /**
     * Get RMA dashboard statistics
     * Returns counts of total requests, total items, repaired, and unrepaired items
     * Also returns daily trend for the last 7 days and recent RMA numbers
     */
    public ResponseEntity<?> getRmaDashboardStats() {
        try {
            long totalRequests = rmaRequestDAO.count();
            long totalItems = rmaItemDAO.count();
            long repairedCount = rmaItemDAO.countRepaired();
            long unrepairedCount = rmaItemDAO.countUnrepaired();

            // Calculate Daily Trends for the last 7 days
            List<DailyTrendDto> dailyTrends = new ArrayList<>();
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime sevenDaysAgo = now.minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);

            // Get requests for the last 7 days to minimize memory usage
            List<RmaRequestEntity> recentRequests = rmaRequestDAO.findByCreatedDateBetween(sevenDaysAgo, now);

            // Create a map for quick lookups
            // Group by Date (YYYY-MM-DD)
            java.util.Map<java.time.LocalDate, Long> requestsByDate = recentRequests.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            req -> req.getCreatedDate().withZoneSameInstant(now.getZone()).toLocalDate(),
                            java.util.stream.Collectors.counting()));

            // Iterate last 7 days to ensure all days are present even if 0 requests
            java.time.format.DateTimeFormatter dayFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE");

            for (int i = 6; i >= 0; i--) {
                ZonedDateTime date = now.minusDays(i);
                java.time.LocalDate localDate = date.toLocalDate();

                String dayName = date.format(dayFormatter); // Mon, Tue, etc.
                long count = requestsByDate.getOrDefault(localDate, 0L);

                dailyTrends.add(new DailyTrendDto(dayName, count));
            }

            // Fetch recent RMA numbers from rma_item table (non-null, limited to 10)
            List<String> recentRmaNumbers = new ArrayList<>();
            try {
                List<RmaItemEntity> allItems = rmaItemDAO.findAll();
                recentRmaNumbers = allItems.stream()
                        .filter(item -> item.getRmaNo() != null && !item.getRmaNo().trim().isEmpty()
                                && !item.getRmaNo().startsWith("RMA-") // Exclude auto-generated format
                                && !"Unknown".equalsIgnoreCase(item.getRmaNo().trim()))
                        .map(RmaItemEntity::getRmaNo)
                        .distinct()
                        .limit(10)
                        .collect(java.util.stream.Collectors.toList());
            } catch (Exception e) {
                // If failed to fetch, just return empty list
                e.printStackTrace();
            }

            RmaDashboardStatsDto stats = new RmaDashboardStatsDto(
                    totalRequests,
                    totalItems,
                    repairedCount,
                    unrepairedCount,
                    dailyTrends,
                    recentRmaNumbers);

            // Calculate SLA compliance stats
            try {
                List<RmaRequestEntity> allRequests = rmaRequestDAO.findAll();
                long totalWithTat = 0;
                long onTrack = 0;
                long atRisk = 0;
                long breached = 0;

                for (RmaRequestEntity req : allRequests) {
                    if (req.getTat() != null && req.getDueDate() != null) {
                        totalWithTat++;
                        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, req.getDueDate());
                        int halfTat = req.getTat() / 2;

                        if (daysRemaining < 0) {
                            breached++;
                        } else if (daysRemaining <= halfTat) {
                            atRisk++;
                        } else {
                            onTrack++;
                        }
                    }
                }

                stats.setTotalWithTat(totalWithTat);
                stats.setOnTrackCount(onTrack);
                stats.setAtRiskCount(atRisk);
                stats.setBreachedCount(breached);

                // Calculate compliance rate (closed within TAT)
                // For now, use (onTrack + atRisk) / totalWithTat as a simple metric
                if (totalWithTat > 0) {
                    double complianceRate = (double) (totalWithTat - breached) / totalWithTat * 100;
                    stats.setComplianceRate(Math.round(complianceRate * 10.0) / 10.0); // Round to 1 decimal
                } else {
                    stats.setComplianceRate(100.0); // No TAT defined = 100% compliant
                }
            } catch (Exception e) {
                // SLA calculation failed, set defaults
                stats.setTotalWithTat(0L);
                stats.setOnTrackCount(0L);
                stats.setAtRiskCount(0L);
                stats.setBreachedCount(0L);
                stats.setComplianceRate(100.0);
                System.err.println("Warning: Failed to calculate SLA stats: " + e.getMessage());
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch RMA statistics: " + e.getMessage());
        }
    }

    /**
     * Get TAT Compliance Report - customer-wise breakdown
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getTatComplianceReport() {
        try {
            List<RmaRequestEntity> allRequests = rmaRequestDAO.findAll();
            ZonedDateTime now = ZonedDateTime.now();

            // Group requests by company name
            java.util.Map<String, java.util.List<RmaRequestEntity>> requestsByCompany = allRequests.stream()
                    .collect(java.util.stream.Collectors.groupingBy(RmaRequestEntity::getCompanyName));

            List<TatComplianceReportDto> report = new java.util.ArrayList<>();

            for (java.util.Map.Entry<String, java.util.List<RmaRequestEntity>> entry : requestsByCompany.entrySet()) {
                String companyName = entry.getKey();
                java.util.List<RmaRequestEntity> companyRequests = entry.getValue();

                TatComplianceReportDto dto = new TatComplianceReportDto();
                dto.setCompanyName(companyName);
                dto.setTotalRequests((long) companyRequests.size());

                // Get default TAT from customer if available
                RmaRequestEntity firstReq = companyRequests.get(0);
                if (firstReq.getCustomer() != null && firstReq.getCustomer().getTat() != null) {
                    dto.setDefaultTat(firstReq.getCustomer().getTat());
                }

                long requestsWithTat = 0;
                long completedWithinTat = 0;
                long completedAfterTat = 0;
                long stillOpen = 0;
                long onTrack = 0;
                long atRisk = 0;
                long breached = 0;
                ZonedDateTime oldestOpenDueDate = null;

                for (RmaRequestEntity req : companyRequests) {
                    if (req.getTat() == null || req.getDueDate() == null) {
                        continue;
                    }
                    requestsWithTat++;

                    // Check if all items in request are delivered (completed)
                    boolean allDelivered = true;
                    ZonedDateTime latestDeliveryDate = null;

                    if (req.getItems() != null) {
                        for (RmaItemEntity item : req.getItems()) {
                            String status = item.getRmaStatus();
                            // Check for DELIVERED status (case-insensitive)
                            if (status == null || !status.toUpperCase().contains("DELIVERED")) {
                                allDelivered = false;
                            } else {
                                // Track latest delivery date
                                ZonedDateTime itemDeliveryDate = item.getDeliveryDate();
                                if (itemDeliveryDate == null) {
                                    itemDeliveryDate = item.getDepotReturnDeliveredDate();
                                }
                                if (itemDeliveryDate != null) {
                                    if (latestDeliveryDate == null
                                            || itemDeliveryDate.isAfter(latestDeliveryDate)) {
                                        latestDeliveryDate = itemDeliveryDate;
                                    }
                                }
                            }
                        }
                    }

                    if (allDelivered && latestDeliveryDate != null) {
                        // Completed - check if within TAT
                        if (!latestDeliveryDate.isAfter(req.getDueDate())) {
                            completedWithinTat++;
                        } else {
                            completedAfterTat++;
                        }
                    } else {
                        // Still open
                        stillOpen++;
                        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, req.getDueDate());
                        int halfTat = req.getTat() / 2;

                        if (daysRemaining < 0) {
                            breached++;
                        } else if (daysRemaining <= halfTat) {
                            atRisk++;
                        } else {
                            onTrack++;
                        }

                        // Track oldest open due date
                        if (oldestOpenDueDate == null || req.getDueDate().isBefore(oldestOpenDueDate)) {
                            oldestOpenDueDate = req.getDueDate();
                        }
                    }
                }

                dto.setRequestsWithTat(requestsWithTat);
                dto.setCompletedWithinTat(completedWithinTat);
                dto.setCompletedAfterTat(completedAfterTat);
                dto.setStillOpen(stillOpen);
                dto.setOnTrack(onTrack);
                dto.setAtRisk(atRisk);
                dto.setBreached(breached);
                dto.setOldestOpenDueDate(oldestOpenDueDate);

                // Calculate compliance rate (only for completed requests)
                long totalCompleted = completedWithinTat + completedAfterTat;
                if (totalCompleted > 0) {
                    double rate = (double) completedWithinTat / totalCompleted * 100;
                    dto.setComplianceRate(Math.round(rate * 10.0) / 10.0);
                } else if (requestsWithTat > 0) {
                    // No completed yet, show as N/A (null)
                    dto.setComplianceRate(null);
                } else {
                    // No requests with TAT
                    dto.setComplianceRate(null);
                }

                // Only include customers with at least one request with TAT
                if (requestsWithTat > 0) {
                    report.add(dto);
                }
            }

            // Sort by company name
            report.sort((a, b) -> a.getCompanyName().compareToIgnoreCase(b.getCompanyName()));

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate TAT compliance report: " + e.getMessage());
        }
    }
}
