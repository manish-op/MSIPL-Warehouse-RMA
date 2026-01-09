package com.serverManagement.server.management.service.rma.workflow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dto.rma.depot.DeliveryChallanRequest;

@Service
public class RmaPdfService {

    private static final Logger logger = LoggerFactory.getLogger(RmaPdfService.class);

    @Autowired
    private com.serverManagement.server.management.dao.rma.depot.ProductHsnRepository productHsnRepository;

    // Constants for layout
    private static final float MARGIN = 25;

    // Updated for 15 Columns: Sr, Code, Desc, HSN, UOM, Qty, Rate, Amt, CGST(R,A),
    // SGST(R,A), IGST(R,A), Val
    private static final String[] HEADERS_MAIN = {
            "Sr No#", "Material Code", "Material Description", "HSN",
            "UOM", "Qty", "Rate", "Amount",
            "CGST", "SGST", "IGST", "Value"
    };

    // Sub-headers for Tax columns (indices 8-13 in 0-based 15-col layout)
    // 0-7 are standard. 8=CGST-R, 9=CGST-A, etc.
    private static final String[] HEADERS_SUB = {
            "", "", "", "", "", "", "", "",
            "Rate", "Amt", "Rate", "Amt", "Rate", "Amt",
            ""
    };

    // Total 15 columns widths
    // Page Width ~595. Margins 25*2=50. Usable: 545.
    // 18+50+120+35+25+20+42+42 + (22+23)*3 + 45 = ~552
    private static final float[] COL_WIDTHS = {
            25, 60, 103, 35, 25, 20, 42, 42, // 0-7: Sr to Amt (MatCode increased to 60)
            22, 23, 22, 23, 22, 23, // 8-13: Taxes (3 pairs)
            45 // 14: Value
    };

    public byte[] generateDeliveryChallan(DeliveryChallanRequest request) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float y = 800; // Start Y
                float width = page.getMediaBox().getWidth() - 2 * MARGIN;

                // 1. Header Section
                float currentY = drawHeaderSection(document, contentStream, request, y, width);

                // 2. Items Table
                float tableBottomY = drawItemsTable(contentStream, request, currentY, width);

                // 3. Footer
                drawFooterSection(contentStream, request, tableBottomY, width);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private float drawHeaderSection(PDDocument document, PDPageContentStream contentStream,
            DeliveryChallanRequest request, float yStart, float width) throws IOException {
        float y = yStart;
        float headerHeight = 220; // Compact height

        // Main Box Border
        contentStream.setLineWidth(1.0f);
        contentStream.addRect(MARGIN, y - headerHeight, width, headerHeight);
        contentStream.stroke();

        // Vertical Split Line (Approx 65/35 split per new image)
        float splitX = MARGIN + (width * 0.65f);
        contentStream.moveTo(splitX, y);
        contentStream.lineTo(splitX, y - headerHeight);
        contentStream.stroke();

        // --- LEFT SIDE ---
        // Logo and Title
        try (InputStream imageStream = getClass().getResourceAsStream("/images/companyLogo.png")) {
            if (imageStream != null) {
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageStream.readAllBytes(),
                        "logo");
                contentStream.drawImage(pdImage, MARGIN + 10, y - 35, 25, 25);

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE), 16);
                contentStream.newLineAtOffset(MARGIN + 45, y - 27);
                contentStream.showText("MOTOROLA SOLUTIONS");
                contentStream.endText();
            } else {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(MARGIN + 10, y - 27);
                contentStream.showText("MOTOROLA SOLUTIONS");
                contentStream.endText();
            }
        } catch (Exception e) {
        }

        // Horizontal line for Consignor/Consignee split
        // Consignor Section
        float splitY = y - 90; // Higher split
        contentStream.moveTo(MARGIN, splitY);
        contentStream.lineTo(splitX, splitY);
        contentStream.stroke();

        // Consignor Details
        float textY = y - 45;
        float textX = MARGIN + 4;
        drawText(contentStream, "Consignor", textX, textY, true, 7);

        float detailsX = textX + 50;

        // Dynamic Consignor
        String cnrName = request.getConsignorName();
        String cnrAddr = request.getConsignorAddress();
        String cnrGst = request.getConsignorGst();

        // Fallback if empty (keep currently hardcoded as default if user doesn't
        // select?)
        if (cnrName == null || cnrName.isEmpty())
            cnrName = "Motorola Solutions India Pvt. Ltd.";

        List<String> consignorLines = new ArrayList<>();
        consignorLines.add(cnrName);

        if (cnrAddr != null && !cnrAddr.isEmpty()) {
            String[] lines = cnrAddr.split("\n"); // Handle pre-formatted lines or split by comma if needed
            // If it comes from the dropdown text which is comma separated, we might want to
            // split it by line length or comma
            // The dropdown address usually has commas. Let's try to wrap it.
            // But for now, let's assume raw string and basic wrapping
            // Or just split by comma if it's one long line
            // Actually, the AddressSeeder format has commas.
            // Let's rely on simple line wrapping logic similar to Consignee
            // But for consistent look with existing hardcode, let's just add it as is and
            // wrap.
            consignorLines.add(cnrAddr);
        } else {
            // Default Hardcoded Address fallback
            consignorLines.add("Building No. 8A, 05th Floor, DLF Cyber City, Phase II,");
            consignorLines.add("Gurgaon 122002, Haryana, India,");
        }

        if (cnrGst != null && !cnrGst.isEmpty()) {
            consignorLines.add("GST: " + cnrGst);
        } else {
            consignorLines.add("GST: 06AAACM9343D1ZQ");
        }

        float currentCnrY = textY;
        for (String line : consignorLines) {
            if (line.length() > 65) {
                drawText(contentStream, line.substring(0, 65), detailsX, currentCnrY, false, 6);
                currentCnrY -= 8;
                drawText(contentStream, line.substring(65), detailsX, currentCnrY, false, 6);
            } else {
                // Name often bold
                boolean isBold = line.equalsIgnoreCase(cnrName) || line.startsWith("GST");
                drawText(contentStream, line, detailsX, currentCnrY, isBold, 6);
            }
            currentCnrY -= 8;
        }

        // Consignee Section (Rest of Left Side)
        textY = splitY - 12;
        drawText(contentStream, "Consignee", MARGIN + 4, textY, true, 7);

        // Consignee Logic
        String cName = request.getConsigneeName();
        String cAddress = request.getConsigneeAddress();
        // ... (Logic for default address same, just font size tweaks)
        boolean isDefaultBangalore = "Motorola Solutions India Pvt Ltd".equalsIgnoreCase(cName)
                && (cAddress == null || cAddress.trim().equalsIgnoreCase("Bangalore") || cAddress.isEmpty());

        List<String> consigneeLines = new ArrayList<>();
        if (isDefaultBangalore) {
            consigneeLines.add("MWI India Private Limited"); // As per image
            consigneeLines.add("2nd Floor, 4/1, 4th Main, 60 Feet Double Road,");
            consigneeLines.add("HIG Dollars Colony, RMV 2nd Stage,");
            consigneeLines.add("Bangalore 560094, Karnataka, India");
            consigneeLines.add("Landmark: Top of Bon Vivant, Next to RMV Club");
            consigneeLines.add("GST No.: 29AAACM9083G1ZX");
            consigneeLines.add("Contact Person: Mr Antony Raj (+91 91085 28140)");
        } else {
            if (cName != null)
                consigneeLines.add(cName);
            if (cAddress != null) {
                String[] lines = cAddress.split("\n");
                for (String l : lines)
                    consigneeLines.add(l);
            }
            if (request.getGstIn() != null && !request.getGstIn().isEmpty()) {
                consigneeLines.add("GST # " + request.getGstIn());
            }
        }

        float currentTextY = textY;
        for (String line : consigneeLines) {
            if (line.length() > 65) {
                drawText(contentStream, line.substring(0, 65), detailsX, currentTextY, false, 6);
                currentTextY -= 8;
                drawText(contentStream, line.substring(65), detailsX, currentTextY, false, 6);
            } else {
                boolean isBold = line.startsWith("MWI") || line.startsWith("Contact");
                drawText(contentStream, line, detailsX, currentTextY, isBold, 6);
            }
            currentTextY -= 8;
        }

        // --- RIGHT SIDE ---
        // Header Label Box
        float rightHeaderH = 35;
        contentStream.addRect(splitX, y - rightHeaderH, width * 0.35f, rightHeaderH);
        contentStream.stroke();
        drawTextCentered(contentStream, "Delivery Challan", splitX + ((width * 0.35f) / 2), y - 22, true, 10);

        float rightY = y - rightHeaderH;

        // Row 1: MSIPL / 2025 ... Date
        float rowH = 25;
        contentStream.moveTo(splitX, rightY - rowH);
        contentStream.lineTo(MARGIN + width, rightY - rowH);
        contentStream.stroke();

        float midRightX = splitX + (width * 0.35f) * 0.6f; // Split for Label/Value
        contentStream.moveTo(midRightX, rightY);
        contentStream.lineTo(midRightX, rightY - 2 * rowH); // Spans 2 rows
        contentStream.stroke();

        drawTextCentered(contentStream, "MSIPL/2025", splitX + (midRightX - splitX) / 2, rightY - 15, true, 7);
        // Generate DC No Logic ...
        String finalDcNumber = (request.getDcNo() == null || request.getDcNo().isEmpty()) ? "20250923"
                : request.getDcNo();
        drawTextCentered(contentStream, finalDcNumber, midRightX + (MARGIN + width - midRightX) / 2, rightY - 15, false,
                7);

        // Row 2: Date
        rightY -= rowH;
        contentStream.moveTo(splitX, rightY - rowH);
        contentStream.lineTo(MARGIN + width, rightY - rowH);
        contentStream.stroke();
        drawTextCentered(contentStream, "Date", splitX + (midRightX - splitX) / 2, rightY - 15, true, 7);
        drawTextCentered(contentStream, LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yy")),
                midRightX + (MARGIN + width - midRightX) / 2, rightY - 15, false, 7);

        // Row 3: Mode of Shipment
        rightY -= rowH;
        contentStream.moveTo(splitX, rightY - rowH);
        contentStream.lineTo(MARGIN + width, rightY - rowH);
        contentStream.stroke();
        drawTextCentered(contentStream, "Mode of Shipment", splitX + (midRightX - splitX) / 2, rightY - 15, true, 7);
        drawTextCentered(contentStream, request.getModeOfShipment() != null ? request.getModeOfShipment() : "By Road",
                midRightX + (MARGIN + width - midRightX) / 2, rightY - 15, false, 7);

        // Row 4: Remarks
        rightY -= rowH;
        // Remaining space
        drawText(contentStream, "Remarks:- Sending Material for Testing", splitX + 5, rightY - 10, true, 6);
        drawText(contentStream, "purpose only and Returnable basis, Not for Sale \"No", splitX + 5, rightY - 18, true,
                6);
        drawText(contentStream, "Sale transaction involve in this transaction\".", splitX + 5, rightY - 26, true, 6);
        drawText(contentStream, "Hence no commercial value involved.", splitX + 5, rightY - 34, true, 6);
        drawText(contentStream, "Value mentioned for transportation purpose only.", splitX + 5, rightY - 42, true, 6);

        return y - headerHeight;
    }

    private float drawItemsTable(PDPageContentStream contentStream, DeliveryChallanRequest request, float yStart,
            float width) throws IOException {
        float y = yStart - 5;
        float headerRowHeight = 25;

        // 1. Header Box
        contentStream.setLineWidth(1.0f);
        contentStream.addRect(MARGIN, y - headerRowHeight, width, headerRowHeight);
        contentStream.stroke();

        // Main Horizontal Split for headers (only for Tax columns 8-13)
        // 0-7 are normal. 8-13 are tax. 14 is Value.
        float taxStartX = 0;
        for (int k = 0; k < 8; k++)
            taxStartX += COL_WIDTHS[k]; // Sum 0-7
        taxStartX += MARGIN;

        float taxWidth = 0;
        for (int k = 8; k <= 13; k++)
            taxWidth += COL_WIDTHS[k]; // Sum 8-13

        float midHeaderY = y - (headerRowHeight / 2);
        contentStream.moveTo(taxStartX, midHeaderY);
        contentStream.lineTo(taxStartX + taxWidth, midHeaderY);
        contentStream.stroke();

        // Vertical Lines & Text
        float currentX = MARGIN;
        for (int i = 0; i < 15; i++) {
            float colW = COL_WIDTHS[i];

            // Tax Columns Logic (8-13)
            boolean isMergedCol = (i >= 8 && i <= 13);
            boolean isMergedPairLeft = (i == 8 || i == 10 || i == 12); // Rate

            // Draw Vertical Line (Right)
            if (isMergedCol) {
                if (isMergedPairLeft) {
                    // Split line (half height)
                    contentStream.moveTo(currentX + colW, midHeaderY);
                    contentStream.lineTo(currentX + colW, y - headerRowHeight);
                } else {
                    // Full line at end of pair (which matches Main vertical)
                    contentStream.moveTo(currentX + colW, y);
                    contentStream.lineTo(currentX + colW, y - headerRowHeight);
                }
                contentStream.stroke();
            } else {
                // Standard Columns (0-7, 14)
                contentStream.moveTo(currentX + colW, y);
                contentStream.lineTo(currentX + colW, y - headerRowHeight);
                contentStream.stroke();
            }

            // Draw Text
            drawHeaderText(contentStream, i, currentX, y, headerRowHeight, midHeaderY, colW);
            currentX += colW;
        }

        y -= headerRowHeight;

        // 2. Rows
        double totalAmount = 0;
        int totalQty = 0;

        if (request.getItems() != null) {
            int srNo = 1;
            for (DeliveryChallanRequest.DcItemDto item : request.getItems()) {
                // Calculate Row Height needed
                String desc = item.getProduct() + (item.getModel() != null ? " - " + item.getModel() : "");
                float descWidth = COL_WIDTHS[2] - 4;
                List<String> wrappedDesc = wrapText(desc, descWidth,
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA), 6);
                float rowHeight = Math.max(12, wrappedDesc.size() * 8 + 4);

                // Draw Row Border
                contentStream.setLineWidth(0.5f);
                contentStream.addRect(MARGIN, y - rowHeight, width, rowHeight);
                contentStream.stroke();

                currentX = MARGIN;
                // Draw Vertical Lines
                for (int i = 0; i < 15; i++) {
                    currentX += COL_WIDTHS[i];
                    contentStream.moveTo(currentX, y);
                    contentStream.lineTo(currentX, y - rowHeight);
                    contentStream.stroke();
                }

                currentX = MARGIN;
                for (int i = 0; i < 15; i++) {
                    String text = "";
                    switch (i) {
                        case 0:
                            text = String.valueOf(srNo);
                            break; // Use srNo variable
                        case 1:
                            text = item.getSerialNo();
                            break;
                        case 2:
                            text = "";
                            break; // Desc ref

                        case 3:
                            // HSN Code priority: Manual > DB > Default
                            String hsnCode = item.getHsnCode();
                            if (hsnCode == null || hsnCode.trim().isEmpty()) {
                                hsnCode = "84715000"; // Default
                                if (productHsnRepository != null) {
                                    String pName = item.getProduct();
                                    if (pName != null) {
                                        var hsnOpt = productHsnRepository.findByProductName(pName);
                                        if (hsnOpt.isPresent()) {
                                            hsnCode = hsnOpt.get().getHsnCode();
                                        }
                                    }
                                }
                            } else {
                                // Save manual HSN for future use if valid
                                if (productHsnRepository != null) {
                                    try {
                                        String pName = item.getProduct();
                                        if (pName != null && !pName.isEmpty()) {
                                            var hsnOpt = productHsnRepository.findByProductName(pName);
                                            if (hsnOpt.isEmpty() || !hsnOpt.get().getHsnCode().equals(hsnCode)) {
                                                com.serverManagement.server.management.entity.rma.depot.ProductHsnEntity newHsn = hsnOpt
                                                        .orElse(new com.serverManagement.server.management.entity.rma.depot.ProductHsnEntity());
                                                if (newHsn.getProductName() == null)
                                                    newHsn.setProductName(pName);
                                                newHsn.setHsnCode(hsnCode);
                                                productHsnRepository.save(newHsn);
                                            }
                                        }
                                    } catch (Exception e) {
                                        logger.error("Failed to save HSN", e);
                                    }
                                }
                            }
                            text = hsnCode;
                            break; // HSN
                        case 4:
                            text = "Nos.";
                            break; // UOM
                        case 5:
                            text = "1";
                            break; // Qty
                        case 6:
                            text = formatCurrency(item.getRate());
                            break; // Rate
                        case 7:
                            text = formatCurrency(item.getRate());
                            break; // Amount
                        case 8:
                            text = "0%";
                            break; // CGST R
                        case 9:
                            text = "-";
                            break; // CGST A
                        case 10:
                            text = "0%";
                            break; // SGST R
                        case 11:
                            text = "-";
                            break; // SGST A
                        case 12:
                            text = "0%";
                            break; // IGST R
                        case 13:
                            text = "-";
                            break; // IGST A
                        case 14:
                            text = formatCurrency(item.getRate());
                            break; // Value
                    }

                    if (i == 2) {
                        // Draw Wrapped Desc
                        float textY = y - 10;
                        for (String line : wrappedDesc) {
                            if (line != null)
                                drawText(contentStream, line, currentX + 2, textY, false, 6);
                            textY -= 8;
                        }
                    } else {
                        // Centered
                        if (text != null)
                            drawTextCenteredInBox(contentStream, text, currentX, y - rowHeight, COL_WIDTHS[i],
                                    rowHeight);
                    }
                    currentX += COL_WIDTHS[i];
                }

                try {
                    totalAmount += Double.parseDouble(item.getRate());
                    totalQty++;
                } catch (Exception e) {
                }

                srNo++;
                y -= rowHeight;
            }
        }

        // 3. Footer Grid Inputs (Boxes, Dims, Weight)
        float gapHeight = 50;

        // Draw vertical lines in the gap
        contentStream.setLineWidth(0.5f);
        float gapX = MARGIN;
        // Left border
        contentStream.moveTo(gapX, y);
        contentStream.lineTo(gapX, y - gapHeight);
        contentStream.stroke();

        for (float w : COL_WIDTHS) {
            gapX += w;
            contentStream.moveTo(gapX, y);
            contentStream.lineTo(gapX, y - gapHeight);
            contentStream.stroke();
        }

        y -= gapHeight; // Add space between table and boxes section

        // These are appended directly
        String[] footerLabels = { "No of Boxes:", "Dimensions", "Dimensions", "Weight" };
        for (String label : footerLabels) {
            float rowH = 15;
            contentStream.setLineWidth(0.5f);
            contentStream.addRect(MARGIN, y - rowH, width, rowH); // Outer
            contentStream.stroke();

            // Draw vertical lines for all cols to maintain grid
            currentX = MARGIN;
            for (int i = 0; i < 15; i++) {
                // Color fill for specific cells
                if (i == 2 && !label.isEmpty()) { // Material Description col triggers yellow?
                    // Actually image shows yellow background mainly for the inputs
                    contentStream.setNonStrokingColor(1f, 1f, 0f); // Yellow
                    // Fill Col 2 and 3? (Desc + UOM?)
                    // Image shows width of ~2 columns.
                    contentStream.addRect(currentX, y - rowH, COL_WIDTHS[i] + COL_WIDTHS[i + 1], rowH);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(0f, 0f, 0f); // Reset
                }
                // Only fill once for index 2, skip other fills

                contentStream.moveTo(currentX + COL_WIDTHS[i], y);
                contentStream.lineTo(currentX + COL_WIDTHS[i], y - rowH);
                contentStream.stroke();

                currentX += COL_WIDTHS[i];
            }

            // Label in 'Material Code' column (Index 1) - actually spans Sr + Code
            drawText(contentStream, label, MARGIN + COL_WIDTHS[0] + 5, y - 10, true, 8);

            // Input values
            if (label.equals("No of Boxes:")) {
                drawText(contentStream, request.getBoxes(), MARGIN + COL_WIDTHS[0] + COL_WIDTHS[1] + 5, y - 10, false,
                        8);
            } else if (label.equals("Dimensions")) {
                if (label.equals(footerLabels[1])) // First dim row
                    drawText(contentStream, request.getDimensions(), MARGIN + COL_WIDTHS[0] + COL_WIDTHS[1] + 5, y - 10,
                            false, 8);
            } else if (label.equals("Weight")) {
                drawText(contentStream, request.getWeight(), MARGIN + COL_WIDTHS[0] + COL_WIDTHS[1] + 5, y - 10, false,
                        8);
            }

            y -= rowH;
        }

        // 4. Total Row (Qty / Amount)
        float totalRowHeight = 20;
        contentStream.setLineWidth(1.0f);
        contentStream.addRect(MARGIN, y - totalRowHeight, width, totalRowHeight);
        contentStream.stroke();

        currentX = MARGIN;
        for (int i = 0; i < 15; i++) {
            contentStream.moveTo(currentX + COL_WIDTHS[i], y);
            contentStream.lineTo(currentX + COL_WIDTHS[i], y - totalRowHeight);
            contentStream.stroke();
            currentX += COL_WIDTHS[i];
        }

        // Place Totals
        // drawText(contentStream, "Total No of Boxes", MARGIN + COL_WIDTHS[0] +
        // COL_WIDTHS[1] + 5, y - 15, true, 8); // REMOVED
        drawText(contentStream, "Total", MARGIN + width - COL_WIDTHS[14] - 40, y - 15, true, 8); // Index 14 is Value
                                                                                                 // value?

        // Values
        /*
         * float qtyX = MARGIN + COL_WIDTHS[0] + COL_WIDTHS[1] + COL_WIDTHS[2] +
         * COL_WIDTHS[3];
         * drawTextCenteredInBox(contentStream, String.valueOf(totalQty), qtyX, y -
         * totalRowHeight, COL_WIDTHS[4],
         * totalRowHeight);
         */

        float valX = MARGIN + width - COL_WIDTHS[14];
        contentStream.setNonStrokingColor(1f, 1f, 1f); // White/Transparent
        contentStream.addRect(valX, y - totalRowHeight, COL_WIDTHS[14], totalRowHeight); // Index 14 is Value
        contentStream.fill();
        contentStream.setNonStrokingColor(0f, 0f, 0f); // Reset
        // Redraw border
        contentStream.addRect(valX, y - totalRowHeight, COL_WIDTHS[14], totalRowHeight);
        contentStream.stroke();

        drawTextCenteredInBox(contentStream, formatCurrency(String.valueOf(totalAmount)), valX, y - totalRowHeight,
                COL_WIDTHS[14], totalRowHeight);

        y -= totalRowHeight;

        // 5. Amount In Words Row
        float wordsRowHeight = 20;
        contentStream.setNonStrokingColor(1f, 1f, 1f); // White
        contentStream.addRect(MARGIN, y - wordsRowHeight, width, wordsRowHeight);
        contentStream.fill();
        contentStream.setNonStrokingColor(0f, 0f, 0f);
        contentStream.addRect(MARGIN, y - wordsRowHeight, width, wordsRowHeight);
        contentStream.stroke();

        String amountInWords = convertAmountToWords((long) totalAmount);
        drawText(contentStream, "Total Value of Goods in Words : " + amountInWords + " Only", MARGIN + 5, y - 14, true,
                8);

        return y - wordsRowHeight;
    }

    private void drawFooterSection(PDPageContentStream contentStream, DeliveryChallanRequest request, float yStart,
            float width) throws IOException {
        float y = yStart - 10;

        drawText(contentStream,
                "Remarks:- Sending Material for Job work Returns purpose only, Not for Sale \"No Sale transaction involve in this transaction\",",
                MARGIN, y, true, 7);
        y -= 10;
        drawText(contentStream, "Hence no commercial value involved. Value mentioned for transportation purpose only.",
                MARGIN, y, true, 7);

        y -= 30;

        // Bottom Box
        float bottomH = 80;
        contentStream.addRect(MARGIN, y - bottomH, width, bottomH);
        contentStream.stroke();

        // Left: Address
        float textY = y - 10;
        drawText(contentStream, "Motorola Solutions India Pvt. Ltd.,", MARGIN + 5, textY, true, 8);
        textY -= 10;
        drawText(contentStream,
                "Regd. Office Address:- 5th Floor, Tower A, Building 8, DLF Cyber City, Gurgaon - 122002", MARGIN + 5,
                textY, false, 7);
        textY -= 10;
        drawText(contentStream, "Haryana, India Ph: +91-124-4192000, 91-124-4191000", MARGIN + 5, textY, false, 7);
        textY -= 10;
        drawText(contentStream, "GST No. 06AAACM9343D1ZQ", MARGIN + 5, textY, false, 7);
        textY -= 10;
        drawText(contentStream, "www.motorolasolutions.com", MARGIN + 5, textY, false, 7);

        // Right: Signatory
        drawText(contentStream, "For Motorola Solutions India Private Limited", MARGIN + width - 200, y - 10, true, 8);
        drawText(contentStream, "Authorised Signatory", MARGIN + width - 200, y - bottomH + 10, true, 8);
    }

    // Calculate centered x for text in a box
    private void drawTextCenteredInBox(PDPageContentStream contentStream, String text, float x, float y, float w,
            float h) throws IOException {
        if (text == null)
            text = "";
        PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        float fontSize = 7;
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float textHeight = font.getFontDescriptor().getCapHeight() / 1000 * fontSize;

        float cx = x + (w - textWidth) / 2;
        float cy = y + (h - textHeight) / 2;

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(cx, cy);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawText(PDPageContentStream contentStream, String text, float x, float y, boolean bold, int fontSize)
            throws IOException {
        text = text != null ? text.replaceAll("[\\r\\n]", " ") : "";
        contentStream.beginText();
        contentStream.setFont(
                new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA),
                fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawTextCentered(PDPageContentStream contentStream, String text, float cx, float cy, boolean bold,
            int fontSize) throws IOException {
        text = text != null ? text : "";
        PDFont font = new PDType1Font(
                bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA);
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(cx - (textWidth / 2), cy);
        contentStream.showText(text);
        contentStream.endText();
    }

    private String formatCurrency(String val) {
        try {
            double d = Double.parseDouble(val);
            return String.format("%,.2f", d);
        } catch (Exception e) {
            return val;
        }
    }

    // Text Wrapping Helper
    private List<String> wrapText(String text, float width, PDFont font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null)
            return lines;

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String check = line.length() > 0 ? line + " " + word : word;
            float w = font.getStringWidth(check) / 1000 * fontSize;
            if (w > width) {
                if (line.length() > 0)
                    lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line.append(line.length() > 0 ? " " : "").append(word);
            }
        }
        if (line.length() > 0)
            lines.add(line.toString());
        return lines;
    }

    // Amount to Words (Indian Format - Lakhs/Crores)
    private static final String[] units = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen",
            "Nineteen" };
    private static final String[] tens = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty",
            "Ninety" };

    public String convertAmountToWords(long n) {
        if (n == 0)
            return "Zero";
        return convert(n);
    }

    private String convert(long n) {
        if (n < 0)
            return "Minus " + convert(-n);
        if (n < 20)
            return units[(int) n];
        if (n < 100)
            return tens[(int) n / 10] + ((n % 10 != 0) ? " " : "") + units[(int) n % 10];
        if (n < 1000)
            return units[(int) n / 100] + " Hundred" + ((n % 100 != 0) ? " " : "") + convert(n % 100);
        if (n < 100000)
            return convert(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " : "") + convert(n % 1000);
        if (n < 10000000)
            return convert(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " : "") + convert(n % 100000);
        return convert(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " : "") + convert(n % 10000000);
    }

    private void drawHeaderText(PDPageContentStream contentStream, int i, float currentX, float y, float h, float midY,
            float w) throws IOException {
        if (i < 8) {
            // 0-7: Sr to Amount
            drawTextCenteredInBox(contentStream, HEADERS_MAIN[i], currentX, y - h, w, h);
        } else if (i == 14) {
            // 14: Value -> HEADERS_MAIN[11]
            drawTextCenteredInBox(contentStream, HEADERS_MAIN[11], currentX, y - h, w, h);
        } else {
            // Tax columns
            if (i == 8 || i == 10 || i == 12) {
                int mainIdx = 8 + (i - 8) / 2;
                float subW = w + COL_WIDTHS[i + 1];
                drawTextCenteredInBox(contentStream, HEADERS_MAIN[mainIdx], currentX, midY, subW, h / 2);
            }
            drawTextCenteredInBox(contentStream, HEADERS_SUB[i], currentX, y - h, w, h / 2);
        }
    }
}
