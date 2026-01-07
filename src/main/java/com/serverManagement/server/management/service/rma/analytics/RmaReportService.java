package com.serverManagement.server.management.service.rma.analytics;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.serverManagement.server.management.dao.rma.request.RmaItemDAO;
import com.serverManagement.server.management.dao.rma.request.RmaRequestDAO;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;
import com.serverManagement.server.management.entity.rma.request.RmaRequestEntity;

@Service
public class RmaReportService {

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @Autowired
    private RmaRequestDAO rmaRequestDAO;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // PDF Styling Constants
    private static final float MARGIN = 40;
    private static final float Y_START = 750;
    private static final float BOTTOM_MARGIN = 50;
    private static final float ROW_HEIGHT = 20;
    private static final float HEADER_HEIGHT = 25;
    private static final Color HEADER_BG_COLOR = new Color(220, 220, 220); // Light Gray
    private static final Color LINE_COLOR = Color.BLACK;

    /**
     * Generate RMA by Creator Report
     */
    public byte[] generateByCreatorReport(ZonedDateTime startDate, ZonedDateTime endDate) throws IOException {
        List<RmaRequestEntity> requests = getFilteredRequests(startDate, endDate);

        // Group by creator email
        Map<String, List<RmaRequestEntity>> groupedByCreator = new LinkedHashMap<>();
        for (RmaRequestEntity r : requests) {
            String creator = r.getCreatedByEmail() != null ? r.getCreatedByEmail() : "Unknown";
            groupedByCreator.computeIfAbsent(creator, k -> new ArrayList<>()).add(r);
        }

        return generateGroupedReport("RMA Report by Creator", "Creator", groupedByCreator, startDate, endDate);
    }

    /**
     * Generate RMA by Customer Report
     */
    public byte[] generateByCustomerReport(ZonedDateTime startDate, ZonedDateTime endDate) throws IOException {
        List<RmaRequestEntity> requests = getFilteredRequests(startDate, endDate);

        // Group by customer company name
        Map<String, List<RmaRequestEntity>> groupedByCustomer = new LinkedHashMap<>();
        for (RmaRequestEntity r : requests) {
            String customerName = "Unknown";
            if (r.getCustomer() != null && r.getCustomer().getCompanyName() != null) {
                customerName = r.getCustomer().getCompanyName();
            } else if (r.getCompanyName() != null) {
                customerName = r.getCompanyName();
            }
            groupedByCustomer.computeIfAbsent(customerName, k -> new ArrayList<>()).add(r);
        }

        return generateGroupedReport("RMA Report by Customer", "Customer", groupedByCustomer, startDate, endDate);
    }

    /**
     * Generate RMA by Status Report
     */
    public byte[] generateByStatusReport(ZonedDateTime startDate, ZonedDateTime endDate) throws IOException {
        List<RmaItemEntity> items = getFilteredItems(startDate, endDate);

        // Group by status
        Map<String, List<RmaItemEntity>> groupedByStatus = new LinkedHashMap<>();
        for (RmaItemEntity i : items) {
            String status = (i.getRepairStatus() != null && !i.getRepairStatus().trim().isEmpty()) ? i.getRepairStatus()
                    : "UNASSIGNED";
            groupedByStatus.computeIfAbsent(status, k -> new ArrayList<>()).add(i);
        }

        return generateItemGroupedReport("RMA Report by Status", "Status", groupedByStatus, startDate, endDate);
    }

    /**
     * Generate RMA by Repair Type Report (LOCAL vs DEPOT)
     */
    public byte[] generateByRepairTypeReport(ZonedDateTime startDate, ZonedDateTime endDate) throws IOException {
        List<RmaItemEntity> items = getFilteredItems(startDate, endDate);

        // Group by repair type
        Map<String, List<RmaItemEntity>> groupedByType = new LinkedHashMap<>();
        for (RmaItemEntity i : items) {
            String type = i.getRepairType() != null ? i.getRepairType() : "Unknown";
            groupedByType.computeIfAbsent(type, k -> new ArrayList<>()).add(i);
        }

        return generateItemGroupedReport("RMA Report by Repair Type", "Repair Type", groupedByType, startDate, endDate);
    }

    /**
     * Generate RMA by Technician Report
     */
    public byte[] generateByTechnicianReport(ZonedDateTime startDate, ZonedDateTime endDate) throws IOException {
        List<RmaItemEntity> items = getFilteredItems(startDate, endDate);

        // Filter only assigned/repaired items and group by technician
        Map<String, List<RmaItemEntity>> groupedByTech = new LinkedHashMap<>();
        for (RmaItemEntity i : items) {
            if (i.getAssignedToEmail() != null && !i.getAssignedToEmail().isEmpty()) {
                String tech = i.getAssignedToName() != null ? i.getAssignedToName() : i.getAssignedToEmail();
                groupedByTech.computeIfAbsent(tech, k -> new ArrayList<>()).add(i);
            }
        }

        return generateItemGroupedReport("RMA Report by Technician", "Technician", groupedByTech, startDate, endDate);
    }

    /**
     * Generate TAT Compliance Report
     */
    public byte[] generateTatComplianceReport(ZonedDateTime startDate, ZonedDateTime endDate) throws IOException {
        List<RmaItemEntity> items = getFilteredItems(startDate, endDate);

        // Separate into compliant vs non-compliant
        List<RmaItemEntity> compliant = new ArrayList<>();
        List<RmaItemEntity> exceeded = new ArrayList<>();

        for (RmaItemEntity item : items) {
            if (item.getRmaRequest() != null && item.getRmaRequest().getCustomer() != null) {
                Integer tat = item.getRmaRequest().getCustomer().getTat();
                if (tat != null && item.getAssignedDate() != null) {
                    ZonedDateTime deadline = item.getAssignedDate().plusDays(tat);
                    ZonedDateTime completedAt = item.getRepairedDate();

                    if (completedAt != null) {
                        if (completedAt.isBefore(deadline) || completedAt.isEqual(deadline)) {
                            compliant.add(item);
                        } else {
                            exceeded.add(item);
                        }
                    } else {
                        // Still in progress
                        if (ZonedDateTime.now().isAfter(deadline)) {
                            exceeded.add(item);
                        } else {
                            compliant.add(item);
                        }
                    }
                }
            }
        }

        Map<String, List<RmaItemEntity>> tatGroups = new LinkedHashMap<>();
        tatGroups.put("Within TAT (" + compliant.size() + " items)", compliant);
        tatGroups.put("Exceeded TAT (" + exceeded.size() + " items)", exceeded);

        return generateItemGroupedReport("TAT Compliance Report", "Status", tatGroups, startDate, endDate);
    }

    /**
     * Generate Monthly Summary Report
     */
    public byte[] generateMonthlySummaryReport(ZonedDateTime startDate, ZonedDateTime endDate) throws IOException {
        List<RmaItemEntity> items = getFilteredItems(startDate, endDate);
        List<RmaRequestEntity> requests = getFilteredRequests(startDate, endDate);

        // Calculate summary statistics
        int totalRequests = requests.size();
        int totalItems = items.size();

        // Group by status dynamically matching "RMA Report by Status"
        Map<String, Long> statusCounts = items.stream()
                .collect(Collectors.groupingBy(
                        i -> (i.getRepairStatus() != null && !i.getRepairStatus().trim().isEmpty())
                                ? i.getRepairStatus()
                                : "UNASSIGNED",
                        Collectors.counting()));

        long localRepair = items.stream().filter(i -> "LOCAL".equalsIgnoreCase(i.getRepairType())).count();
        long depotRepair = items.stream().filter(i -> "DEPOT".equalsIgnoreCase(i.getRepairType())).count();

        return generateSummaryReport(startDate, endDate, totalRequests, totalItems,
                statusCounts, localRepair, depotRepair);
    }

    // ==================== Helper Methods (Data Fetching) ====================

    private List<RmaRequestEntity> getFilteredRequests(ZonedDateTime startDate, ZonedDateTime endDate) {
        List<RmaRequestEntity> allRequests = rmaRequestDAO.findAll();

        if (startDate == null && endDate == null) {
            return allRequests;
        }

        return allRequests.stream()
                .filter(r -> {
                    ZonedDateTime created = r.getCreatedDate();
                    if (created == null)
                        return false;
                    if (startDate != null && created.isBefore(startDate))
                        return false;
                    if (endDate != null && created.isAfter(endDate))
                        return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<RmaItemEntity> getFilteredItems(ZonedDateTime startDate, ZonedDateTime endDate) {
        List<RmaItemEntity> allItems = rmaItemDAO.findAll();

        if (startDate == null && endDate == null) {
            return allItems;
        }

        return allItems.stream()
                .filter(i -> {
                    // Use assigned date or repaired date for filtering
                    ZonedDateTime dateToCheck = i.getAssignedDate();
                    if (dateToCheck == null) {
                        dateToCheck = i.getRepairedDate();
                    }
                    if (dateToCheck == null)
                        return true; // Include items without dates
                    if (startDate != null && dateToCheck.isBefore(startDate))
                        return false;
                    if (endDate != null && dateToCheck.isAfter(endDate))
                        return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    private String calculateRequestStatus(RmaRequestEntity req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            return "NO ITEMS";
        }
        boolean allComplete = req.getItems().stream().allMatch(i -> "REPAIRED".equalsIgnoreCase(i.getRepairStatus()) ||
                "CANT_BE_REPAIRED".equalsIgnoreCase(i.getRepairStatus()));
        return allComplete ? "CLOSED" : "OPEN";
    }

    // ==================== PDF Generation Methods with Improved Formatting
    // ====================

    private byte[] generateGroupedReport(String title, String groupLabel,
            Map<String, List<RmaRequestEntity>> groupedData,
            ZonedDateTime startDate, ZonedDateTime endDate) throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float yPosition = Y_START;

            // Draw Header (Logo, Title, Date)
            yPosition = drawReportHeader(contentStream, title, startDate, endDate, yPosition);

            // Calculate total stats
            int totalRequests = groupedData.values().stream().mapToInt(List::size).sum();

            // Draw Summary Box
            yPosition = drawSummaryBox(contentStream, yPosition,
                    "Report Summary",
                    String.format("Total Groups: %d | Total Requests: %d", groupedData.size(), totalRequests));

            // Table Columns Definition
            float[] colWidths = { 100, 100, 80, 100, 135 }; // Total 515 (A4 width 595 - 80 margin = 515)
            String[] headers = { "RMA Req. No", "Date", "Items", "Type", "Status" };

            // Iterate through groups
            for (Map.Entry<String, List<RmaRequestEntity>> entry : groupedData.entrySet()) {
                // Check if we need a new page for the group header
                if (yPosition < BOTTOM_MARGIN + 60) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = Y_START;
                    // Re-draw generic header on new page (optional, simpler to just continue)
                }

                // Draw Group Header
                drawSectionHeader(contentStream, yPosition,
                        groupLabel + ": " + entry.getKey() + " (" + entry.getValue().size() + ")");
                yPosition -= 40;

                // Draw Table Header
                drawTableHeaderRow(contentStream, yPosition, headers, colWidths);
                yPosition -= HEADER_HEIGHT;

                // Draw Table Rows
                for (RmaRequestEntity req : entry.getValue()) {
                    if (yPosition < BOTTOM_MARGIN) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = Y_START;

                        // Redraw table header on new page
                        drawTableHeaderRow(contentStream, yPosition, headers, colWidths);
                        yPosition -= HEADER_HEIGHT;
                    }

                    String[] rowData = {
                            req.getRequestNumber() != null ? req.getRequestNumber() : "-",
                            req.getCreatedDate() != null ? req.getCreatedDate().format(DATE_FORMATTER) : "-",
                            String.valueOf(req.getItems() != null ? req.getItems().size() : 0),
                            req.getRepairType() != null ? req.getRepairType() : "-",
                            calculateRequestStatus(req)
                    };

                    drawTableRow(contentStream, yPosition, rowData, colWidths);
                    yPosition -= ROW_HEIGHT;
                }
                yPosition -= 35; // Space between groups
            }

            // Add Page Numbers
            addPageNumbers(document, title);

            contentStream.close();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] generateItemGroupedReport(String title, String groupLabel,
            Map<String, List<RmaItemEntity>> groupedData,
            ZonedDateTime startDate, ZonedDateTime endDate) throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float yPosition = Y_START;

            // Draw Header
            yPosition = drawReportHeader(contentStream, title, startDate, endDate, yPosition);

            // Summary
            int totalItems = groupedData.values().stream().mapToInt(List::size).sum();
            yPosition = drawSummaryBox(contentStream, yPosition,
                    "Report Summary",
                    String.format("Total Groups: %d | Total Items: %d", groupedData.size(), totalItems));

            // Table Columns
            float[] colWidths = { 100, 120, 100, 80, 115 };
            String[] headers = { "RMA  No", "Product", "Serial No", "Type", "Status" };

            // Draw groups
            for (Map.Entry<String, List<RmaItemEntity>> entry : groupedData.entrySet()) {
                if (yPosition < BOTTOM_MARGIN + 60) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = Y_START;
                }

                drawSectionHeader(contentStream, yPosition,
                        groupLabel + ": " + entry.getKey() + " (" + entry.getValue().size() + ")");
                yPosition -= 40;

                drawTableHeaderRow(contentStream, yPosition, headers, colWidths);
                yPosition -= HEADER_HEIGHT;

                for (RmaItemEntity item : entry.getValue()) {
                    if (yPosition < BOTTOM_MARGIN) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = Y_START;
                        drawTableHeaderRow(contentStream, yPosition, headers, colWidths);
                        yPosition -= HEADER_HEIGHT;
                    }

                    String rmaNo = item.getRmaNo() != null ? item.getRmaNo()
                            : (item.getRmaRequest() != null ? item.getRmaRequest().getRequestNumber() : "-");
                    String product = item.getProduct() != null ? item.getProduct() : "-";
                    // Truncate long product names
                    if (product.length() > 20)
                        product = product.substring(0, 18) + "...";

                    String serialNo = item.getSerialNo() != null ? item.getSerialNo() : "N/A";
                    if (serialNo.startsWith("NA-")) {
                        serialNo = "N/A";
                    }

                    String[] rowData = {
                            rmaNo,
                            product,
                            serialNo,
                            item.getRepairType() != null ? item.getRepairType() : "-",
                            (item.getRepairStatus() != null && !item.getRepairStatus().trim().isEmpty())
                                    ? item.getRepairStatus()
                                    : "UNASSIGNED"
                    };

                    drawTableRow(contentStream, yPosition, rowData, colWidths);
                    yPosition -= ROW_HEIGHT;
                }
                yPosition -= 35;
            }

            addPageNumbers(document, title);
            contentStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] generateSummaryReport(ZonedDateTime startDate, ZonedDateTime endDate,
            int totalRequests, int totalItems, Map<String, Long> statusCounts, long localRepair, long depotRepair)
            throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = Y_START;
                float pageWidth = page.getMediaBox().getWidth();

                // Header
                yPosition = drawReportHeader(contentStream, "RMA Monthly Summary Report", startDate, endDate,
                        yPosition);

                // 1. Overview Section
                drawSectionHeader(contentStream, yPosition, "Operational Overview");
                yPosition -= 35;

                // Draw 2 large stats boxes side by side
                float boxWidth = (pageWidth - 2 * MARGIN - 20) / 2;
                drawStatBox(contentStream, MARGIN, yPosition, boxWidth, "Total Requests",
                        String.valueOf(totalRequests));
                drawStatBox(contentStream, MARGIN + boxWidth + 20, yPosition, boxWidth, "Total Items",
                        String.valueOf(totalItems));
                yPosition -= 70;

                // 2. Status Breakdown Table
                drawSectionHeader(contentStream, yPosition, "Status Breakdown");
                yPosition -= 35;

                float[] colWidths = { 350, 100 };
                String[] statusHeaders = { "Status Category", "Count" };

                drawTableHeaderRow(contentStream, yPosition, statusHeaders, colWidths);
                yPosition -= HEADER_HEIGHT;

                String[][] statusData = new String[statusCounts.size()][2];
                int i = 0;
                for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
                    statusData[i][0] = entry.getKey();
                    statusData[i][1] = String.valueOf(entry.getValue());
                    i++;
                }

                for (String[] row : statusData) {
                    drawTableRow(contentStream, yPosition, row, colWidths);
                    yPosition -= ROW_HEIGHT;
                }
                yPosition -= 30;

                // 3. Repair Type Breakdown
                drawSectionHeader(contentStream, yPosition, "Repair Type Breakdown");
                yPosition -= 35;

                drawTableHeaderRow(contentStream, yPosition, statusHeaders, colWidths);
                yPosition -= HEADER_HEIGHT;

                String[][] typeData = {
                        { "Local Repair", String.valueOf(localRepair) },
                        { "Depot Repair", String.valueOf(depotRepair) }
                };

                for (String[] row : typeData) {
                    drawTableRow(contentStream, yPosition, row, colWidths);
                    yPosition -= ROW_HEIGHT;
                }
                yPosition -= 30;

                // 4. Performance Metrics
                drawSectionHeader(contentStream, yPosition, "Performance Metrics");
                yPosition -= 35;

                // Calculate approximate completion from status keys
                long completedCount = statusCounts.entrySet().stream()
                        .filter(e -> {
                            String k = e.getKey().toUpperCase();
                            return k.contains("REPAIRED") || k.contains("CLOSED") || k.contains("FAULTY");
                        })
                        .mapToLong(Map.Entry::getValue)
                        .sum();

                double completionRate = totalItems > 0 ? ((double) completedCount / totalItems * 100) : 0;
                drawStatBox(contentStream, MARGIN, yPosition, pageWidth - 2 * MARGIN, "Completion Rate",
                        String.format("%.1f %%", completionRate));

                // Footer
                drawFooter(contentStream, pageWidth);
            }

            addPageNumbers(document, "RMA Summary Report");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    // ==================== Drawing Helper Methods ====================

    private float drawReportHeader(PDPageContentStream contentStream, String title, ZonedDateTime start,
            ZonedDateTime end, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
        contentStream.setNonStrokingColor(new Color(41, 128, 185)); // Professional Blue
        contentStream.newLineAtOffset(MARGIN, y);
        contentStream.showText(sanitizeText(title));
        contentStream.endText();

        y -= 25;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.setNonStrokingColor(Color.DARK_GRAY);
        contentStream.newLineAtOffset(MARGIN, y);
        String dateRange = "Period: " + (start != null ? start.format(DATE_FORMATTER) : "Start") + " to "
                + (end != null ? end.format(DATE_FORMATTER) : "Present");
        contentStream.showText(sanitizeText(dateRange));
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(400, y);
        contentStream.showText(sanitizeText("Generated: " + ZonedDateTime.now().format(DATETIME_FORMATTER)));
        contentStream.endText();

        y -= 20;

        // Draw separator line
        contentStream.setStrokingColor(Color.LIGHT_GRAY);
        contentStream.moveTo(MARGIN, y);
        contentStream.lineTo(550, y);
        contentStream.stroke();
        contentStream.setStrokingColor(Color.BLACK); // Reset

        return y - 30; // Return new Y position
    }

    private float drawSummaryBox(PDPageContentStream contentStream, float y, String title, String content)
            throws IOException {
        // Draw Light Blue Box
        contentStream.setNonStrokingColor(new Color(235, 245, 251));
        contentStream.addRect(MARGIN, y - 40, 515, 40);
        contentStream.fill();

        // Border
        contentStream.setStrokingColor(new Color(41, 128, 185));
        contentStream.addRect(MARGIN, y - 40, 515, 40);
        contentStream.stroke();
        contentStream.setStrokingColor(Color.BLACK);

        // Text
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.setNonStrokingColor(new Color(41, 128, 185));
        contentStream.newLineAtOffset(MARGIN + 10, y - 15);
        contentStream.showText(sanitizeText(title));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.newLineAtOffset(MARGIN + 10, y - 30);
        contentStream.showText(sanitizeText(content));
        contentStream.endText();

        return y - 60;
    }

    private void drawSectionHeader(PDPageContentStream contentStream, float y, String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
        contentStream.setNonStrokingColor(new Color(44, 62, 80)); // Dark Navy
        contentStream.newLineAtOffset(MARGIN, y);
        contentStream.showText(sanitizeText(text));
        contentStream.endText();
    }

    private void drawTableHeaderRow(PDPageContentStream contentStream, float y, String[] headers, float[] colWidths)
            throws IOException {
        float height = HEADER_HEIGHT;
        float width = 0;
        for (float w : colWidths)
            width += w;

        // Background
        contentStream.setNonStrokingColor(HEADER_BG_COLOR);
        contentStream.addRect(MARGIN, y, width, height);
        contentStream.fill();

        // Borders
        contentStream.setStrokingColor(LINE_COLOR);
        contentStream.addRect(MARGIN, y, width, height);
        contentStream.stroke();

        // Vertical grid lines
        float x = MARGIN;
        for (float w : colWidths) {
            contentStream.moveTo(x, y);
            contentStream.lineTo(x, y + height);
            contentStream.stroke();
            x += w;
        }

        // Text
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);

        float textX = MARGIN + 5;
        float textY = y + 8;

        for (int i = 0; i < headers.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(textX, textY);
            contentStream.showText(sanitizeText(headers[i]));
            contentStream.endText();
            textX += colWidths[i];
        }
    }

    private void drawTableRow(PDPageContentStream contentStream, float y, String[] values, float[] colWidths)
            throws IOException {
        float height = ROW_HEIGHT;
        float width = 0;
        for (float w : colWidths)
            width += w;

        // Grid lines (border box)
        contentStream.setStrokingColor(LINE_COLOR);
        contentStream.addRect(MARGIN, y, width, height);
        contentStream.stroke();

        // Vertical lines
        float x = MARGIN;
        for (float w : colWidths) {
            contentStream.moveTo(x, y);
            contentStream.lineTo(x, y + height);
            contentStream.stroke();
            x += w;
        }

        // Text
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);

        float textX = MARGIN + 5;
        float textY = y + 6;

        for (int i = 0; i < values.length; i++) {
            String val = values[i] != null ? values[i] : "";

            // Improve text clipping using actual string width
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float fontSize = 9;
            float cellWidth = colWidths[i] - 10; // Margin padding (5 left + 5 right)

            if (val.length() > 0) {
                val = truncateText(val, cellWidth, font, fontSize);
            }

            contentStream.beginText();
            contentStream.newLineAtOffset(textX, textY);
            contentStream.showText(sanitizeText(val));
            contentStream.endText();
            textX += colWidths[i];
        }
    }

    private void drawStatBox(PDPageContentStream contentStream, float x, float y, float width, String label,
            String value) throws IOException {
        float height = 50;

        contentStream.setNonStrokingColor(new Color(248, 249, 249));
        contentStream.addRect(x, y - height, width, height);
        contentStream.fill();

        contentStream.setStrokingColor(Color.LIGHT_GRAY);
        contentStream.addRect(x, y - height, width, height);
        contentStream.stroke();

        contentStream.setNonStrokingColor(Color.GRAY);
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        contentStream.newLineAtOffset(x + 10, y - 20);
        contentStream.showText(sanitizeText(label));
        contentStream.endText();

        contentStream.setNonStrokingColor(new Color(41, 128, 185));
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.newLineAtOffset(x + 10, y - 40);
        contentStream.showText(sanitizeText(value));
        contentStream.endText();
    }

    private String sanitizeText(String text) {
        if (text == null)
            return "";
        // Replace all control characters (including newlines, tabs) with space to avoid
        // PDFBox encoding errors
        return text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .trim();
    }

    private void drawFooter(PDPageContentStream contentStream, float pageWidth) throws IOException {
        contentStream.setStrokingColor(Color.LIGHT_GRAY);
        contentStream.moveTo(MARGIN, 30);
        contentStream.lineTo(pageWidth - MARGIN, 30);
        contentStream.stroke();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
        contentStream.setNonStrokingColor(Color.GRAY);
        contentStream.newLineAtOffset(MARGIN, 20);
        contentStream.showText(sanitizeText("Motorola Solutions India Pvt. Ltd. | Confidential"));
        contentStream.endText();
    }

    private void addPageNumbers(PDDocument document, String title) throws IOException {
        int pageCount = document.getNumberOfPages();
        for (int i = 0; i < pageCount; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                contentStream.setNonStrokingColor(Color.DARK_GRAY);
                float width = page.getMediaBox().getWidth();
                String text = "Page " + (i + 1) + " of " + pageCount;
                // Center text
                float textSize = (new PDType1Font(Standard14Fonts.FontName.HELVETICA)).getStringWidth(text) / 1000 * 9;
                contentStream.newLineAtOffset(width - MARGIN - textSize, 20);
                contentStream.showText(sanitizeText(text));
                contentStream.endText();
            }
        }
    }

    private String truncateText(String text, float maxWidth, PDType1Font font, float fontSize) throws IOException {
        if (text == null)
            return "";
        float width = font.getStringWidth(text) / 1000 * fontSize;
        if (width <= maxWidth)
            return text;

        int len = text.length();
        int newLen = (int) (len * (maxWidth / width)) - 3;
        if (newLen < 1)
            newLen = 1;

        while (newLen > 0) {
            String candidate = text.substring(0, newLen) + "...";
            if (font.getStringWidth(candidate) / 1000 * fontSize <= maxWidth) {
                return candidate;
            }
            newLen--;
        }
        return "...";
    }
}
