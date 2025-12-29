package com.serverManagement.server.management.service.rma.workflow;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.krysalis.barcode4j.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.rma.RmaOutwardGatepassDAO;
import com.serverManagement.server.management.dao.rma.RmaItemDAO;
import com.serverManagement.server.management.dao.rma.RmaRequestDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.rma.workflow.RmaOutwardGatepassEntity;
import com.serverManagement.server.management.entity.rma.request.RmaItemEntity;
import com.serverManagement.server.management.entity.rma.request.RmaRequestEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

/**
 * Service for generating RMA Outward Gatepass PDFs (Local Repair)
 */
@Service
public class RmaOutwardGatepassService {

    @Autowired
    private RmaRequestDAO rmaRequestDAO;

    @Autowired
    private RmaItemDAO rmaItemDAO;

    @Autowired
    private RmaOutwardGatepassDAO rmaOutwardGatepassDAO;

    @Autowired
    private AdminUserDAO adminUserDAO;

    // PDF Dimensions (A4)
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN_LEFT = 36;
    private static final float MARGIN_RIGHT = 36;
    private static final float MARGIN_TOP = 36;
    private static final float MARGIN_BOTTOM = 36;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    private PDFont regularFont;
    private PDFont boldFont;

    /**
     * Generate Outward Gatepass PDF for items in an RMA request
     */
    @Transactional
    public ResponseEntity<?> generateGatepass(HttpServletRequest request, String requestNumber) {

        try {
            // 1. Try finding by Auto-Generated Request Number
            RmaRequestEntity rmaRequest = rmaRequestDAO.findByRequestNumber(requestNumber);

            // 2. If not found, try finding by Manual RMA Number
            if (rmaRequest == null) {

                rmaRequest = rmaRequestDAO.findByRmaNo(requestNumber);
            }

            List<RmaItemEntity> items;

            if (rmaRequest == null) {

                // Fallback: Try to find items by their item-level RMA number (legacy support)
                items = rmaItemDAO.findByRmaNo(requestNumber);

                if (items.isEmpty()) {

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("RMA Request/Items not found for number: " + requestNumber);
                }

                // Create a dummy request entity for PDF generation context
                // Check if we already created a dummy request for this 'legacy' number to avoid
                // duplicates
                // Actually if we check findByRequestNumber above, we might find a previously
                // created dummy.
                // But dummy sets requestNumber = input.

                rmaRequest = new RmaRequestEntity();
                rmaRequest.setRequestNumber(requestNumber);
                rmaRequest.setCompanyName("N/A (Legacy Item)");
                rmaRequest.setReturnAddress("N/A"); // Ensure non-null
                rmaRequest.setCreatedDate(ZonedDateTime.now());

                // Set other required fields to avoid NPE/Constraints
                rmaRequest.setEmail("legacy@placeholder.com");
                rmaRequest.setContactName("N/A");
                rmaRequest.setTelephone("0000000000");
                rmaRequest.setMobile("0000000000");

            } else {

                // Get REPAIRED items for this request
                items = rmaItemDAO.findByRmaRequest(rmaRequest);

                // Filter for REPAIRED items
                // Modified to allow all items for the Request, as Depot items might have
                // different statuses
                // like "GGN_RECEIVED_FROM_DEPOT" etc.
                // items.removeIf(item -> item.getRepairStatus() == null ||
                // !"REPAIRED".equalsIgnoreCase(item.getRepairStatus()));
            }

            if (items.isEmpty()) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No REPAIRED items found for RMA: " + requestNumber);
            }

            // Get logged in user info
            String loggedInUserEmail = null;
            String loggedInUserName = null;
            try {
                if (request.getUserPrincipal() != null) {
                    loggedInUserEmail = request.getUserPrincipal().getName();
                    AdminUserEntity loggedInUser = adminUserDAO.findByEmail(loggedInUserEmail.toLowerCase());
                    loggedInUserName = loggedInUser != null ? loggedInUser.getName() : loggedInUserEmail;
                } else {
                    loggedInUserEmail = "system";
                    loggedInUserName = "System";
                }
            } catch (Exception e) {
                System.err.println("User auth warning: " + e.getMessage());
                loggedInUserEmail = "system";
                loggedInUserName = "System";
            }

            // Generate gatepass number
            String gatepassNumber = generateGatepassNumber();

            // Create gatepass record
            RmaOutwardGatepassEntity gatepass = new RmaOutwardGatepassEntity();
            gatepass.setGatepassNumber(gatepassNumber);

            // Handle legacy items that don't have a persisted request
            if (rmaRequest.getId() == null) {
                // Persist the dummy request so we can link it
                try {
                    rmaRequest = rmaRequestDAO.save(rmaRequest);
                } catch (Exception ex) {
                    System.err.println("Failed to save dummy request: " + ex.getMessage());
                    // If save fails (e.g. duplicate key), try to fetch it again?
                    // This is edge case. Proceeding.
                    throw ex;
                }
            }
            gatepass.setRmaRequest(rmaRequest);
            // For OUTWARD, Consignee is the Customer (Company Name from Request)
            gatepass.setConsigneeName(rmaRequest.getCompanyName() != null ? rmaRequest.getCompanyName() : "N/A");
            gatepass.setConsigneeAddress(rmaRequest.getReturnAddress() != null ? rmaRequest.getReturnAddress() : "N/A");
            gatepass.setGeneratedDate(ZonedDateTime.now());
            gatepass.setGeneratedByEmail(loggedInUserEmail);
            gatepass.setGeneratedByName(loggedInUserName);
            gatepass.setItemCount(items.size());

            rmaOutwardGatepassDAO.save(gatepass);

            // Generate PDF
            byte[] pdfBytes = generatePdf(gatepass, rmaRequest, items);

            // Return PDF response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "OutwardGatepass_" + gatepassNumber + ".pdf";
            ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(filename)
                    .build();
            headers.setContentDisposition(contentDisposition);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error generating gatepass: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate gatepass: " + e.getMessage());
        }
    }

    private String generateGatepassNumber() {
        Long maxId = rmaOutwardGatepassDAO.findMaxId();
        long nextId = (maxId == null) ? 1 : maxId + 1;
        return String.format("OGP-%04d", nextId);
    }

    private byte[] generatePdf(RmaOutwardGatepassEntity gatepass, RmaRequestEntity rmaRequest,
            List<RmaItemEntity> items) throws Exception {

        PDDocument document = new PDDocument();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        float yPosition = PAGE_HEIGHT - MARGIN_TOP;

        // --- Header with Logo ---
        try (InputStream logoStream = getClass().getClassLoader().getResourceAsStream("images/companyLogo.png")) {
            if (logoStream != null) {
                BufferedImage awtImage = ImageIO.read(logoStream);
                PDImageXObject companyLogo = LosslessFactory.createFromImage(document, awtImage);
                contentStream.drawImage(companyLogo, MARGIN_LEFT, yPosition - 50, 50, 50);
            }
        } catch (IOException e) {
            System.err.println("Error loading logo: " + e.getMessage());
        }

        // Title
        drawText(contentStream, boldFont, 18, "RMA OUTWARD GATEPASS", PAGE_WIDTH / 2, yPosition - 25,
                TextAlignment.TA_CENTER);

        // Gatepass Number (right side)
        drawText(contentStream, boldFont, 12, "Sl. No: " + gatepass.getGatepassNumber(),
                PAGE_WIDTH - MARGIN_RIGHT, yPosition - 15, TextAlignment.TA_RIGHT);

        yPosition -= 70;

        // Horizontal line
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(1);
        contentStream.moveTo(MARGIN_LEFT, yPosition);
        contentStream.lineTo(PAGE_WIDTH - MARGIN_RIGHT, yPosition);
        contentStream.stroke();

        yPosition -= 25;

        // --- Consignee Details ---
        drawText(contentStream, boldFont, 11, "CONSIGNEE NAME:", MARGIN_LEFT, yPosition, TextAlignment.TA_LEFT);
        drawText(contentStream, regularFont, 11, gatepass.getConsigneeName(), MARGIN_LEFT + 110, yPosition,
                TextAlignment.TA_LEFT);

        drawText(contentStream, boldFont, 11, "DATE:", PAGE_WIDTH / 2 + 50, yPosition, TextAlignment.TA_LEFT);
        String dateStr = gatepass.getGeneratedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        drawText(contentStream, regularFont, 11, dateStr, PAGE_WIDTH / 2 + 90, yPosition, TextAlignment.TA_LEFT);

        yPosition -= 20;

        drawText(contentStream, boldFont, 11, "ADDRESS:", MARGIN_LEFT, yPosition, TextAlignment.TA_LEFT);
        float addressHeight = drawWrappedText(contentStream, regularFont, 10, gatepass.getConsigneeAddress(),
                MARGIN_LEFT + 70, yPosition, 200, TextAlignment.TA_LEFT);

        drawText(contentStream, boldFont, 11, "RMA REF:", PAGE_WIDTH / 2 + 50, yPosition, TextAlignment.TA_LEFT);
        String rmaRef = (rmaRequest.getRmaNo() != null && !rmaRequest.getRmaNo().isEmpty())
                ? rmaRequest.getRmaNo()
                : rmaRequest.getRequestNumber();
        drawText(contentStream, regularFont, 11, rmaRef, PAGE_WIDTH / 2 + 100, yPosition,
                TextAlignment.TA_LEFT);

        yPosition -= Math.max(addressHeight, 20) + 35; // Added extra space after RMA REF

        // --- Items Table ---
        float[] columnWidths = { 0.5f, 2f, 2f, 1.5f, 2.5f, 0.5f };
        float[] actualColumnWidths = new float[columnWidths.length];
        float totalRelativeWidth = 0;
        for (float w : columnWidths)
            totalRelativeWidth += w;
        for (int i = 0; i < columnWidths.length; i++) {
            actualColumnWidths[i] = (columnWidths[i] / totalRelativeWidth) * CONTENT_WIDTH;
        }

        float tableHeaderHeight = 25;
        float tableRowHeight = 22;

        // Draw Table Header
        drawTableHeader(contentStream, boldFont, 9,
                new String[] { "Sl.No", "Product Name", "Serial No.", "Model", "Repair Remarks", "Qty" },
                actualColumnWidths, MARGIN_LEFT, yPosition, tableHeaderHeight);
        yPosition -= tableHeaderHeight;

        // Draw Table Rows
        int itemCounter = 1;
        for (RmaItemEntity item : items) {
            if (yPosition < MARGIN_BOTTOM + tableRowHeight + 80) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                yPosition = PAGE_HEIGHT - MARGIN_TOP;
                drawTableHeader(contentStream, boldFont, 9,
                        new String[] { "Sl.No", "Product Name", "Serial No.", "Model", "Repair Remarks", "Qty" },
                        actualColumnWidths, MARGIN_LEFT, yPosition, tableHeaderHeight);
                yPosition -= tableHeaderHeight;
            }

            float currentX = MARGIN_LEFT;
            float cellY = yPosition;

            // Sl.No
            drawCell(contentStream, regularFont, 9, String.valueOf(itemCounter), currentX, cellY,
                    actualColumnWidths[0], tableRowHeight, TextAlignment.TA_CENTER);
            currentX += actualColumnWidths[0];

            // Product Name
            drawCell(contentStream, regularFont, 9, item.getProduct() != null ? item.getProduct() : "",
                    currentX, cellY, actualColumnWidths[1], tableRowHeight, TextAlignment.TA_LEFT);
            currentX += actualColumnWidths[1];

            // Serial No
            drawCell(contentStream, regularFont, 9, item.getSerialNo() != null ? item.getSerialNo() : "",
                    currentX, cellY, actualColumnWidths[2], tableRowHeight, TextAlignment.TA_LEFT);
            currentX += actualColumnWidths[2];

            // Model
            drawCell(contentStream, regularFont, 9, item.getModel() != null ? item.getModel() : "",
                    currentX, cellY, actualColumnWidths[3], tableRowHeight, TextAlignment.TA_LEFT);
            currentX += actualColumnWidths[3];

            // Remarks (Repair Remarks or Fault Description)
            String remarks = item.getRepairRemarks() != null ? item.getRepairRemarks()
                    : (item.getFaultDescription() != null ? item.getFaultDescription() : "");
            if (remarks.length() > 30)
                remarks = remarks.substring(0, 30) + "...";
            drawCell(contentStream, regularFont, 8, remarks, currentX, cellY, actualColumnWidths[4],
                    tableRowHeight, TextAlignment.TA_LEFT);
            currentX += actualColumnWidths[4];

            // Qty
            drawCell(contentStream, regularFont, 9, "1", currentX, cellY, actualColumnWidths[5],
                    tableRowHeight, TextAlignment.TA_CENTER);

            // Draw row border
            drawRowBorder(contentStream, MARGIN_LEFT, yPosition, yPosition - tableRowHeight, actualColumnWidths);

            yPosition -= tableRowHeight;
            itemCounter++;
        }

        // --- Footer at bottom of page ---
        // Calculate footer position at the bottom
        float footerY = MARGIN_BOTTOM + 100;

        // If current position is already near bottom, start new page for footer
        if (yPosition < footerY + 50) {
            contentStream.close();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            footerY = MARGIN_BOTTOM + 100;
        }

        yPosition = footerY;

        drawText(contentStream, boldFont, 11, "RECEIVED BY: ___________________", MARGIN_LEFT, yPosition,
                TextAlignment.TA_LEFT);
        drawText(contentStream, boldFont, 11, "SECURITY SEAL & SIGNATURE",
                PAGE_WIDTH - MARGIN_RIGHT - 150, yPosition, TextAlignment.TA_LEFT);

        yPosition -= 25;
        drawText(contentStream, boldFont, 11, "ADDRESS/STAMP: ________________", MARGIN_LEFT, yPosition,
                TextAlignment.TA_LEFT);

        yPosition -= 25;
        drawText(contentStream, boldFont, 11, "NAME: ________________________", MARGIN_LEFT, yPosition,
                TextAlignment.TA_LEFT);

        yPosition -= 40;
        drawText(contentStream, regularFont, 9, "Generated by: " + gatepass.getGeneratedByName(),
                MARGIN_LEFT, yPosition, TextAlignment.TA_LEFT);

        contentStream.close();
        document.save(outputStream);
        document.close();

        return outputStream.toByteArray();
    }

    // --- Helper Methods ---

    private void drawText(PDPageContentStream contentStream, PDFont font, float fontSize, String text,
            float x, float y, TextAlignment alignment) throws IOException {
        if (text == null)
            text = "";
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.setNonStrokingColor(Color.BLACK);
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float startX;
        if (alignment == TextAlignment.TA_LEFT) {
            startX = x;
        } else if (alignment == TextAlignment.TA_RIGHT) {
            startX = x - textWidth;
        } else {
            startX = x - (textWidth / 2);
        }
        contentStream.newLineAtOffset(startX, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawTableHeader(PDPageContentStream contentStream, PDFont font, float fontSize,
            String[] headers, float[] columnWidths, float startX, float startY, float rowHeight)
            throws IOException {
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(1);
        float currentX = startX;
        for (int i = 0; i < headers.length; i++) {
            contentStream.setNonStrokingColor(new Color(220, 220, 220));
            contentStream.addRect(currentX, startY - rowHeight, columnWidths[i], rowHeight);
            contentStream.fillAndStroke();
            drawText(contentStream, font, fontSize, headers[i], currentX + columnWidths[i] / 2,
                    startY - rowHeight / 2 - 4, TextAlignment.TA_CENTER);
            currentX += columnWidths[i];
        }
    }

    private void drawCell(PDPageContentStream contentStream, PDFont font, float fontSize, String text,
            float x, float y, float width, float height, TextAlignment alignment) throws IOException {
        contentStream.setNonStrokingColor(Color.BLACK);
        float textX = alignment == TextAlignment.TA_LEFT ? x + 3 : x + width / 2;
        drawText(contentStream, font, fontSize, text, textX, y - height / 2 - 4, alignment);
    }

    private void drawRowBorder(PDPageContentStream contentStream, float startX, float topY, float bottomY,
            float[] columnWidths) throws IOException {
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(0.5f);
        // Bottom line
        contentStream.moveTo(startX, bottomY);
        contentStream.lineTo(startX + CONTENT_WIDTH, bottomY);
        contentStream.stroke();
        // Vertical lines
        float currentX = startX;
        for (int i = 0; i <= columnWidths.length; i++) {
            contentStream.moveTo(currentX, topY);
            contentStream.lineTo(currentX, bottomY);
            contentStream.stroke();
            if (i < columnWidths.length)
                currentX += columnWidths[i];
        }
    }

    private float drawWrappedText(PDPageContentStream contentStream, PDFont font, float fontSize,
            String text, float x, float y, float maxWidth, TextAlignment alignment) throws IOException {
        if (text == null || text.isEmpty())
            return 0;
        List<String> lines = splitTextIntoLines(font, fontSize, text, maxWidth);
        float currentY = y;
        float lineHeight = 12;
        for (String line : lines) {
            drawText(contentStream, font, fontSize, line, x, currentY, alignment);
            currentY -= lineHeight;
        }
        return y - currentY;
    }

    private List<String> splitTextIntoLines(PDFont font, float fontSize, String text, float maxWidth)
            throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (font.getStringWidth(currentLine + " " + word) / 1000 * fontSize < maxWidth) {
                if (currentLine.length() > 0)
                    currentLine.append(" ");
                currentLine.append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        if (currentLine.length() > 0)
            lines.add(currentLine.toString());
        return lines;
    }
}
