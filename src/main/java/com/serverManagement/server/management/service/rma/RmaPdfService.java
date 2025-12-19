package com.serverManagement.server.management.service.rma;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dto.rma.DeliveryChallanRequest;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.io.InputStream;

import com.serverManagement.server.management.entity.rma.DepotDispatchEntity;
import com.serverManagement.server.management.dao.rma.DepotDispatchDAO;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class RmaPdfService {

    @Autowired
    private DepotDispatchDAO depotDispatchDAO;

    public byte[] generateDeliveryChallan(DeliveryChallanRequest request) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                float margin = 30;
                float yStart = 780;
                float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
                float yPosition = yStart;
                float bottomMargin = 70;

                // Title
                // Load Logo
                try (InputStream imageStream = getClass().getResourceAsStream("/images/companyLogo.png")) {
                    if (imageStream != null) {
                        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document,
                                imageStream.readAllBytes(), "logo");
                        float logoWidth = 30;
                        float logoHeight = 30; // Aspect ratio? Assuming square-ish or fitting in 30x30

                        // Draw logo
                        contentStream.drawImage(pdImage, margin, yPosition, logoWidth, logoHeight);

                        // Adjust text position
                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                        contentStream.newLineAtOffset(margin + logoWidth + 10, yPosition + 8); // +8 to align vertically
                                                                                               // somewhat
                        contentStream.showText("MOTOROLA SOLUTIONS");
                        contentStream.endText();
                    } else {
                        // Fallback if image not found
                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("MOTOROLA SOLUTIONS");
                        contentStream.endText();
                    }
                } catch (Exception e) {
                    // Fallback on error
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("MOTOROLA SOLUTIONS");
                    contentStream.endText();
                }

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(tableWidth - 50, yPosition);
                contentStream.showText("Delivery Challan");
                contentStream.endText();

                yPosition -= 30;

                // Header Table (Consignor / Consignee)
                // Draw Box
                float headerHeight = 450;
                contentStream.setLineWidth(1f);
                contentStream.addRect(margin, yPosition - headerHeight, tableWidth, headerHeight);
                contentStream.stroke();

                // Vertical Line Separation (approx 60% for left, 40% for right)
                float splitX = margin + (tableWidth * 0.60f);
                contentStream.moveTo(splitX, yPosition);
                contentStream.lineTo(splitX, yPosition - headerHeight);
                contentStream.stroke();

                // === LEFT SIDE (Consignor / Consignee) ===
                // Split Left side horizontally for Consignor (Top) and Consignee (Bottom)
                float rowHeight = headerHeight / 2;
                contentStream.moveTo(margin, yPosition - rowHeight);
                contentStream.lineTo(splitX, yPosition - rowHeight);
                contentStream.stroke();

                // 1. Consignor (Top Left)
                float leftX = margin + 5;
                float textY = yPosition - 15;

                // Box label "Consignor"
                drawText(contentStream, "Consignor", leftX, textY, true);

                // Details to right of label - Reduced Spacing to 70
                float detailsX = leftX + 70;
                drawText(contentStream, "Motorola Solutions", detailsX, textY, true);
                textY -= 12;
                drawText(contentStream, "5th Floor, Tower A,", detailsX, textY, false);
                textY -= 12;
                drawText(contentStream, "Gurgaon - 122002", detailsX, textY, false);
                textY -= 12;
                drawText(contentStream, "Haryana, India", detailsX, textY, false);
                textY -= 12;
                drawText(contentStream, "Navneet Sharma", detailsX, textY, false);
                textY -= 12;
                drawText(contentStream, "9821859076", detailsX, textY, false);
                textY -= 12;
                drawText(contentStream, "GST IN: O", margin + 5, textY, false); // Bottom of top box

                // 2. Consignee (Bottom Left)
                textY = yPosition - rowHeight - 15;
                drawText(contentStream, "Consignee", leftX, textY, true);

                detailsX = leftX + 70;
                String cName = request.getConsigneeName();
                String cAddress = request.getConsigneeAddress();

                // Check if it's the default Bangalore case
                boolean isDefaultBangalore = "Motorola Solutions India Pvt Ltd".equalsIgnoreCase(cName)
                        && (cAddress == null || cAddress.trim().equalsIgnoreCase("Bangalore") || cAddress.isEmpty());

                if (isDefaultBangalore) {
                    drawText(contentStream, "MOTOROLA SOLUTIONS INDIA", detailsX, textY, true);
                    textY -= 12;
                    drawText(contentStream, "PRIVATE LIMITED,", detailsX, textY, true);
                    textY -= 12;
                    drawText(contentStream, "C/O COMMUNICATION TEST", detailsX, textY, false);
                    textY -= 12;
                    drawText(contentStream, "DESIGN INDIA PRIVATE LIMITED", detailsX, textY, false);
                    textY -= 12;
                    drawText(contentStream, "No.48/1, 2nd Main Road,", detailsX, textY, false);
                    textY -= 12;
                    drawText(contentStream, "Peenya Industrial Area,", detailsX, textY, false);
                    textY -= 12;
                    drawText(contentStream, "Bangalore - 560 058 Karnataka,", detailsX, textY, false);
                    textY -= 12;
                    drawText(contentStream, "Land Mark: RBL Bank", detailsX, textY, false);
                    textY -= 12;
                    drawText(contentStream, "CONTACT NAME : K.UMAMAHESWARI", detailsX, textY, false);
                    textY -= 12;
                    drawText(contentStream, "GST # 29AAACM9343D1ZG", detailsX, textY, false);
                    textY -= 12;
                    drawText(contentStream, "Ph: +91 9844218850", detailsX, textY, false);
                } else {
                    // Use Payload Values
                    if (cName != null) {
                        drawText(contentStream, cName, detailsX, textY, true);
                        textY -= 12;
                    }
                    if (cAddress != null) {
                        String[] lines = cAddress.split("\n");
                        for (String line : lines) {
                            // Basic wrapping or just printing lines
                            // Avoiding proper text wrapping implementation for simplicity unless needed,
                            // assuming users enter newlines or strings aren't infinitely long.
                            if (line.length() > 40) {
                                // Simple split if too long
                                // (This is a naive approach, real wrapping is complex in PDFBox)
                                String p1 = line.substring(0, 40);
                                String p2 = line.substring(40);
                                drawText(contentStream, p1, detailsX, textY, false);
                                textY -= 12;
                                drawText(contentStream, p2, detailsX, textY, false);
                                textY -= 12;
                            } else {
                                drawText(contentStream, line, detailsX, textY, false);
                                textY -= 12;
                            }
                        }
                    }
                    // Print GST if available in payload?
                    // Currently DC Request has gstIn
                    if (request.getGstIn() != null && !request.getGstIn().isEmpty()) {
                        drawText(contentStream, "GST # " + request.getGstIn(), detailsX, textY, false);
                        textY -= 12;
                    }
                }

                // === RIGHT SIDE ===
                // Top Right: Shipping Info
                // 4 Rows approximately
                float rightX = splitX + 5;
                float rightY = yPosition - 15;

                // Horizontal lines on right side
                float rightRowH = rowHeight / 4;

                // MSIPL/2025 | DC No
                drawText(contentStream, "MSIPL/2025", rightX, rightY, true);

                // Use requested DC No or fallback
                final String finalDcNumber;
                String incomingDc = request.getDcNo();
                if (incomingDc == null || incomingDc.isEmpty()) {
                    finalDcNumber = "1";
                } else {
                    finalDcNumber = incomingDc;
                    try {
                        boolean exists = depotDispatchDAO.findAll().stream()
                                .anyMatch(d -> d.getDcNo() != null && d.getDcNo().equals(finalDcNumber));

                        if (!exists) {
                            DepotDispatchEntity newDispatch = new DepotDispatchEntity();
                            newDispatch.setDcNo(finalDcNumber);
                            newDispatch.setDispatchDate(java.time.ZonedDateTime.now());
                            newDispatch.setCourierName("Local/Customer"); // Placeholder
                            depotDispatchDAO.save(newDispatch);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to save DC usage in PDF service: " + e.getMessage());
                    }
                }

                drawText(contentStream, finalDcNumber, rightX + 100, rightY, false);
                contentStream.moveTo(splitX, yPosition - rightRowH);
                contentStream.lineTo(margin + tableWidth, yPosition - rightRowH);
                contentStream.stroke();

                // Date
                rightY -= rightRowH;
                drawText(contentStream, "Date", rightX, rightY, true);
                drawText(contentStream, LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), rightX + 110,
                        rightY, false);
                contentStream.moveTo(splitX, yPosition - (rightRowH * 2));
                contentStream.lineTo(margin + tableWidth, yPosition - (rightRowH * 2));
                contentStream.stroke();

                // Transporter ID
                rightY -= rightRowH;
                drawText(contentStream, "Transporter ID", rightX, rightY, true);
                drawText(contentStream, request.getTransporterId() != null ? request.getTransporterId() : "",
                        rightX + 110, rightY, false);
                contentStream.moveTo(splitX, yPosition - (rightRowH * 3));
                contentStream.lineTo(margin + tableWidth, yPosition - (rightRowH * 3));
                contentStream.stroke();

                // Mode of Shipment
                rightY -= rightRowH;
                drawText(contentStream, "Mode of Shipment", rightX, rightY, true);
                drawText(contentStream, request.getModeOfShipment(), rightX + 110, rightY, false);

                // Remarks (Right Bottom)
                float remarksY = yPosition - rowHeight - 15;
                drawText(contentStream, "Remarks:- Sending Material", rightX, remarksY, true);
                remarksY -= 12;
                drawText(contentStream, "for Job work", rightX, remarksY, true);
                remarksY -= 12;
                drawText(contentStream, "Returns purpose only,", rightX, remarksY, true);
                remarksY -= 12;
                drawText(contentStream, "Not for Sale", rightX, remarksY, true);
                remarksY -= 12;
                drawText(contentStream, "\"No Sale transaction involve", rightX, remarksY, true);
                remarksY -= 12;
                drawText(contentStream, "in this transaction\",", rightX, remarksY, true);
                remarksY -= 12;
                drawText(contentStream, "Hence no commercial", rightX, remarksY, true);
                remarksY -= 12;
                drawText(contentStream, "value involved.", rightX, remarksY, true);

                yPosition -= (headerHeight + 20);

                // ITEMS TABLE
                float[] colWidths = { 25, 70, 130, 30, 25, 45, 45, 30, 30, 30, 50 };
                String[] headers = { "Sr", "Material Code", "Description", "UOM", "Qty", "Rate", "Amount", "CGST",
                        "SGST", "IGST", "Value" };

                float tableY = yPosition;
                float currentY = tableY;

                // Header Border
                drawTableScanline(contentStream, margin, currentY, tableWidth);
                currentY -= 20;

                // Header Text and Vertical Lines
                float currentX = margin;
                contentStream.moveTo(currentX, tableY);
                contentStream.lineTo(currentX, currentY);
                contentStream.stroke(); // First vertical line

                for (int i = 0; i < headers.length; i++) {
                    drawText(contentStream, headers[i], currentX + 2, currentY + 6, true);
                    currentX += colWidths[i];
                    // Draw vertical line after each column
                    contentStream.moveTo(currentX, tableY);
                    contentStream.lineTo(currentX, currentY);
                    contentStream.stroke();
                }
                drawTableScanline(contentStream, margin, currentY, tableWidth);

                float headerBottomY = currentY;

                // Draw Items
                double totalAmount = 0;
                int totalQty = 0;

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);

                if (request.getItems() != null) {
                    for (DeliveryChallanRequest.DcItemDto item : request.getItems()) {
                        float rowTopY = currentY;
                        currentY -= 20;
                        currentX = margin;

                        // Vertical Line Start
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // Sr
                        drawText(contentStream, String.valueOf(item.getSlNo()), currentX + 2, currentY + 5, false);
                        currentX += colWidths[0];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // Code (Material Code -> Serial No)
                        drawText(contentStream, item.getSerialNo() != null ? item.getSerialNo() : "", currentX + 2,
                                currentY + 5, false);
                        currentX += colWidths[1];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // Desc
                        String desc = item.getProduct();
                        if (item.getModel() != null && !item.getModel().isEmpty()) {
                            desc += " - " + item.getModel();
                        }
                        if (desc != null && desc.length() > 25)
                            desc = desc.substring(0, 25) + "...";
                        drawText(contentStream, desc != null ? desc : "", currentX + 2, currentY + 5, false);
                        currentX += colWidths[2];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // UOM
                        drawText(contentStream, "each", currentX + 2, currentY + 5, false);
                        currentX += colWidths[3];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // Qty
                        drawText(contentStream, "1", currentX + 2, currentY + 5, false);
                        totalQty++;
                        currentX += colWidths[4];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // Rate
                        double rate = 0;
                        try {
                            rate = Double.parseDouble(item.getRate());
                        } catch (Exception e) {
                        }
                        drawText(contentStream, String.format("%.2f", rate), currentX + 2, currentY + 5, false);
                        currentX += colWidths[5];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // Amount
                        drawText(contentStream, String.format("%.2f", rate), currentX + 2, currentY + 5, false);
                        currentX += colWidths[6];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // CGST
                        drawText(contentStream, "0%", currentX + 2, currentY + 5, false);
                        currentX += colWidths[7];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // SGST
                        drawText(contentStream, "0%", currentX + 2, currentY + 5, false);
                        currentX += colWidths[8];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // IGST
                        drawText(contentStream, "0%", currentX + 2, currentY + 5, false);
                        currentX += colWidths[9];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        // Value
                        drawText(contentStream, String.format("%.2f", rate), currentX + 2, currentY + 5, false);
                        currentX += colWidths[10];
                        contentStream.moveTo(currentX, rowTopY);
                        contentStream.lineTo(currentX, currentY);
                        contentStream.stroke();

                        totalAmount += rate;

                        // Horizontal line for this row
                        drawTableScanline(contentStream, margin, currentY, tableWidth);
                    }
                }

                // Draw Total Row
                float totalRowTopY = currentY;
                currentY -= 20;
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);

                // Total Label
                drawText(contentStream, "Total", margin + colWidths[0] + colWidths[1], currentY + 5, true);

                // Vertical lines only for needed spots on total
                // Start line
                contentStream.moveTo(margin, totalRowTopY);
                contentStream.lineTo(margin, currentY);
                contentStream.stroke();
                // End line
                contentStream.moveTo(margin + tableWidth, totalRowTopY);
                contentStream.lineTo(margin + tableWidth, currentY);
                contentStream.stroke();

                // Total Qty
                float totalQtyX = margin + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3];
                // Vertical line before Qty
                contentStream.moveTo(totalQtyX, totalRowTopY);
                contentStream.lineTo(totalQtyX, currentY);
                contentStream.stroke();

                drawText(contentStream, String.valueOf(totalQty), totalQtyX + 2, currentY + 5, true);

                // Vertical line after Qty
                contentStream.moveTo(totalQtyX + colWidths[4], totalRowTopY);
                contentStream.lineTo(totalQtyX + colWidths[4], currentY);
                contentStream.stroke();

                // Total Value (Last Col)
                float totalValX = margin + tableWidth - colWidths[10];
                // Vertical line before Value
                contentStream.moveTo(totalValX, totalRowTopY);
                contentStream.lineTo(totalValX, currentY);
                contentStream.stroke();

                drawText(contentStream, String.format("%.2f", totalAmount), totalValX + 2, currentY + 5, true);

                drawTableScanline(contentStream, margin, currentY, tableWidth);

                // Footer Info (Boxes, Dims, Weight)
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                currentY -= 30;
                drawText(contentStream, "No of Boxes:  " + request.getBoxes(), margin, currentY, true);
                currentY -= 15;
                drawText(contentStream, "Dimensions:   " + request.getDimensions(), margin, currentY, false);
                currentY -= 15;
                drawText(contentStream, "Weight:       " + request.getWeight(), margin, currentY, false);

                currentY -= 15;
                drawText(contentStream, "Total No of Boxes: 1", margin + 200, currentY + 30, true);
                drawText(contentStream, "Total: " + String.format("%.2f", totalAmount), margin + tableWidth - 100,
                        currentY + 30, true);

                // Signatory
                currentY -= 50;
                drawText(contentStream, "For Motorola Solutions India Pvt. Ltd.", margin + 300, currentY, true);
                currentY -= 40;
                drawText(contentStream, "Authorised Signatory", margin + 300, currentY, false);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void drawText(PDPageContentStream contentStream, String text, float x, float y, boolean bold)
            throws IOException {
        contentStream.beginText();
        contentStream.setFont(
                new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA),
                10);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawTableScanline(PDPageContentStream contentStream, float x, float y, float width)
            throws IOException {
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + width, y);
        contentStream.stroke();
    }
}
