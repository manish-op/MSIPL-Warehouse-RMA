package com.serverManagement.server.management.service.rma;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
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

            float margin = 30;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float[] colWidths = { 25, 70, 130, 30, 25, 45, 45, 30, 30, 30, 50 };
            String[] headersArr = { "Sr", "Material Code", "Description", "UOM", "Qty", "Rate", "Amount", "CGST",
                    "SGST", "IGST", "Value" };

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // 1. Draw Company Header on First Page
            float currentY = drawCompanyHeader(document, contentStream, request, margin, tableWidth);

            // 2. Draw Items Table
            currentY -= 20; // Margin before table
            drawTableHeader(contentStream, margin, tableWidth, currentY, colWidths, headersArr);
            currentY -= 20;

            double totalAmount = 0;
            int totalQty = 0;
            int srNo = 1;

            if (request.getItems() != null) {
                for (DeliveryChallanRequest.DcItemDto item : request.getItems()) {
                    // Page break check (Space for one row + some margin)
                    if (currentY < 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        currentY = 780;
                        drawTableHeader(contentStream, margin, tableWidth, currentY, colWidths, headersArr);
                        currentY -= 20;
                    }

                    float rowTopY = currentY;
                    currentY -= 20;

                    // Row Content
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);

                    // Vertical lines and Text for each column
                    float tempX = margin;
                    drawRowCell(contentStream, String.valueOf(srNo++), tempX, rowTopY, colWidths[0], 20, false);
                    tempX += colWidths[0];
                    drawRowCell(contentStream, item.getSerialNo() != null ? item.getSerialNo() : "", tempX, rowTopY,
                            colWidths[1], 20, false);
                    tempX += colWidths[1];
                    String desc = (item.getProduct() != null ? item.getProduct() : "")
                            + (item.getModel() != null ? " - " + item.getModel() : "");
                    if (desc.length() > 28)
                        desc = desc.substring(0, 25) + "...";
                    drawRowCell(contentStream, desc, tempX, rowTopY, colWidths[2], 20, false);
                    tempX += colWidths[2];
                    drawRowCell(contentStream, "each", tempX, rowTopY, colWidths[3], 20, false);
                    tempX += colWidths[3];
                    drawRowCell(contentStream, "1", tempX, rowTopY, colWidths[4], 20, false);
                    totalQty++;
                    tempX += colWidths[4];
                    double rate = 0;
                    try {
                        rate = Double.parseDouble(item.getRate());
                    } catch (Exception e) {
                    }
                    drawRowCell(contentStream, String.format("%.2f", rate), tempX, rowTopY, colWidths[5], 20, false);
                    tempX += colWidths[5];
                    drawRowCell(contentStream, String.format("%.2f", rate), tempX, rowTopY, colWidths[6], 20, false);
                    tempX += colWidths[6];
                    drawRowCell(contentStream, "0%", tempX, rowTopY, colWidths[7], 20, false);
                    tempX += colWidths[7];
                    drawRowCell(contentStream, "0%", tempX, rowTopY, colWidths[8], 20, false);
                    tempX += colWidths[8];
                    drawRowCell(contentStream, "0%", tempX, rowTopY, colWidths[9], 20, false);
                    tempX += colWidths[9];
                    drawRowCell(contentStream, String.format("%.2f", rate), tempX, rowTopY, colWidths[10], 20, false);

                    totalAmount += rate;

                    // Draw horizontal line after each row as requested
                    drawTableScanline(contentStream, margin, currentY, tableWidth);
                }
            }

            // 3. Draw Footer (Check for space)
            float footerHeight = 280;
            if (currentY < footerHeight) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                currentY = 780;
            } else {
                // Pin footer to bottom or follow items?
                // Using fixed bottom positioning as it looks better in DC designs
                currentY = 280;
            }

            drawFooter(contentStream, request, margin, tableWidth, currentY, totalQty, totalAmount);

            contentStream.close();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private float drawCompanyHeader(PDDocument document, PDPageContentStream contentStream,
            DeliveryChallanRequest request, float margin, float tableWidth) throws IOException {
        float yPosition = 780;

        // Load Logo & Title
        try (InputStream imageStream = getClass().getResourceAsStream("/images/companyLogo.png")) {
            if (imageStream != null) {
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageStream.readAllBytes(),
                        "logo");
                contentStream.drawImage(pdImage, margin, yPosition, 30, 30);
                drawText(contentStream, "MOTOROLA SOLUTIONS", margin + 40, yPosition + 8, true);
            } else {
                drawText(contentStream, "MOTOROLA SOLUTIONS", margin, yPosition, true);
            }
        } catch (Exception e) {
        }

        drawText(contentStream, "Delivery Challan", tableWidth - 50, yPosition, true);
        yPosition -= 30;

        // Consignor / Consignee Block
        float headerHeight = 280;
        contentStream.setLineWidth(1f);
        contentStream.addRect(margin, yPosition - headerHeight, tableWidth, headerHeight);
        contentStream.stroke();

        float splitX = margin + (tableWidth * 0.60f);
        contentStream.moveTo(splitX, yPosition);
        contentStream.lineTo(splitX, yPosition - headerHeight);
        contentStream.stroke();

        float rowHeight = headerHeight / 2;
        contentStream.moveTo(margin, yPosition - rowHeight);
        contentStream.lineTo(splitX, yPosition - rowHeight);
        contentStream.stroke();

        // Left Side Content... (Consignor/Consignee logic)
        // [Condensed for brevity but keeping all labels]
        float leftX = margin + 5;
        float textY = yPosition - 15;
        drawText(contentStream, "Consignor", leftX, textY, true);
        float detailsX = leftX + 70;
        drawText(contentStream, "Motorola Solutions", detailsX, textY, true);
        drawText(contentStream, "5th Floor, Tower A, Gurgaon - 122002", detailsX, textY - 12, false);
        drawText(contentStream, "Haryana, India | 9821859076", detailsX, textY - 24, false);
        drawText(contentStream, "GST IN: 06AAACM9343D1ZO", margin + 5, yPosition - rowHeight + 5, false);

        textY = yPosition - rowHeight - 15;
        drawText(contentStream, "Consignee", leftX, textY, true);
        String cName = request.getConsigneeName() != null ? request.getConsigneeName() : "MOTOROLA SOLUTIONS INDIA";
        drawText(contentStream, cName, detailsX, textY, true);
        String cAddr = request.getConsigneeAddress() != null ? request.getConsigneeAddress() : "Bangalore";
        drawText(contentStream, cAddr, detailsX, textY - 12, false);
        if (request.getGstIn() != null)
            drawText(contentStream, "GST # " + request.getGstIn(), detailsX, textY - 48, false);

        // Right Side Content (DC info)
        float rightX = splitX + 5;
        float rightY = yPosition - 15;
        float rightRowH = rowHeight / 4;

        // Use auto-incremented or provided DC No
        String dcNo = request.getDcNo();
        if (dcNo == null || dcNo.isEmpty() || "1".equals(dcNo)) {
            try {
                long nextId = depotDispatchDAO.findAll().stream().mapToLong(d -> {
                    try {
                        return Long.parseLong(d.getDcNo());
                    } catch (Exception e) {
                        return 0L;
                    }
                }).max().orElse(0L) + 1;
                dcNo = String.valueOf(nextId);
            } catch (Exception e) {
            }
        }

        drawText(contentStream, "MSIPL/2025", rightX, rightY, true);
        drawText(contentStream, dcNo != null ? dcNo : "1", rightX + 100, rightY, false);

        for (int i = 1; i <= 3; i++) {
            contentStream.moveTo(splitX, yPosition - (rightRowH * i));
            contentStream.lineTo(margin + tableWidth, yPosition - (rightRowH * i));
            contentStream.stroke();
        }

        drawText(contentStream, "Date", rightX, rightY - rightRowH, true);
        drawText(contentStream, LocalDate.now().toString(), rightX + 110, rightY - rightRowH, false);
        drawText(contentStream, "Transporter ID", rightX, rightY - (rightRowH * 2), true);
        drawText(contentStream, request.getTransporterId() != null ? request.getTransporterId() : "", rightX + 110,
                rightY - (rightRowH * 2), false);
        drawText(contentStream, "Mode of Shipment", rightX, rightY - (rightRowH * 3), true);
        drawText(contentStream, request.getModeOfShipment() != null ? request.getModeOfShipment() : "ROAD",
                rightX + 110, rightY - (rightRowH * 3), false);

        // Remarks in Right Bottom
        drawText(contentStream, "Remarks:- Special Shipment", rightX, yPosition - rowHeight - 15, true);

        return yPosition - headerHeight;
    }

    private void drawTableHeader(PDPageContentStream contentStream, float margin, float tableWidth, float tableY,
            float[] colWidths, String[] headers) throws IOException {
        drawTableScanline(contentStream, margin, tableY, tableWidth);
        float currentY = tableY - 20;
        float currentX = margin;

        contentStream.moveTo(margin, tableY);
        contentStream.lineTo(margin, currentY);
        contentStream.stroke();

        for (int i = 0; i < headers.length; i++) {
            drawText(contentStream, headers[i], currentX + 2, currentY + 6, true);
            currentX += colWidths[i];
            contentStream.moveTo(currentX, tableY);
            contentStream.lineTo(currentX, currentY);
            contentStream.stroke();
        }
        drawTableScanline(contentStream, margin, currentY, tableWidth);
    }

    private void drawFooter(PDPageContentStream contentStream, DeliveryChallanRequest request, float margin,
            float tableWidth, float currentY, int totalQty, double totalAmount) throws IOException {
        // 1. Shipment Info Box
        contentStream.addRect(margin, currentY - 60, tableWidth, 60);
        contentStream.stroke();
        float splitX = margin + 150;
        contentStream.moveTo(splitX, currentY);
        contentStream.lineTo(splitX, currentY - 60);
        contentStream.stroke();

        drawText(contentStream, "No of Boxes:", margin + 5, currentY - 15, true);
        drawText(contentStream, request.getBoxes() != null ? request.getBoxes() : "-", splitX + 5, currentY - 15,
                false);
        drawText(contentStream, "Dimensions:", margin + 5, currentY - 30, true);
        drawText(contentStream, request.getDimensions() != null ? request.getDimensions() : "-", splitX + 5,
                currentY - 30, false);
        drawText(contentStream, "Weight:", margin + 5, currentY - 45, true);
        drawText(contentStream, request.getWeight() != null ? request.getWeight() : "-", splitX + 5, currentY - 45,
                false);
        currentY -= 60;

        // 2. Totals Row
        contentStream.addRect(margin, currentY - 20, tableWidth, 20);
        contentStream.stroke();
        drawText(contentStream, "Total No of Boxes:  " + totalQty, margin + 200, currentY - 15, true);
        drawText(contentStream, "Total Amount:  " + String.format("%.2f", totalAmount), margin + 400, currentY - 15,
                true);
        currentY -= 20;

        // 3. Amount in Words Row
        contentStream.addRect(margin, currentY - 20, tableWidth, 20);
        contentStream.stroke();
        String amountInWords = convertToWords(totalAmount);
        drawText(contentStream, "Total Value of Goods in Words: Rupees " + amountInWords + " only", margin + 5,
                currentY - 15, true);
        currentY -= 20;

        // 4. Remarks & Signatory Split Box
        float boxH = 150;
        contentStream.addRect(margin, currentY - boxH, tableWidth, boxH);
        contentStream.stroke();
        float midX = margin + (tableWidth * 0.55f);
        contentStream.moveTo(midX, currentY);
        contentStream.lineTo(midX, currentY - boxH);
        contentStream.stroke();

        // Left: Address & GST
        float tx = margin + 5;
        float ty = currentY - 15;
        drawText(contentStream, "Motorola Solutions India Pvt. Ltd.,", tx, ty, true);
        drawText(contentStream, "Regd Office: 5th Floor, Tower A, DLF Cyber City,", tx, ty - 12, false);
        drawText(contentStream, "Gurgaon - 122002, Haryana, India", tx, ty - 24, false);
        drawText(contentStream, "GST: 06AAACM9343D1ZO", tx, ty - 48, false);

        // Right: Signatory
        drawText(contentStream, "For Motorola Solutions India Private Limited", midX + 5, ty, true);
        drawText(contentStream, "Authorised Signatory", midX + 5, currentY - 130, true);
    }

    private String convertToWords(double amount) {
        long number = (long) amount;
        long paisa = Math.round((amount - number) * 100);

        if (number == 0)
            return "Zero";

        String result = convert(number);
        if (paisa > 0) {
            result += " and " + convert(paisa) + " Paisa";
        }
        return result;
    }

    private static final String[] units = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen",
            "Nineteen" };
    private static final String[] tens = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty",
            "Ninety" };

    private String convert(long n) {
        if (n < 20)
            return units[(int) n];
        if (n < 100)
            return tens[(int) (n / 10)] + ((n % 10 != 0) ? " " + units[(int) (n % 10)] : "");
        if (n < 1000)
            return units[(int) (n / 100)] + " Hundred" + ((n % 100 != 0) ? " " + convert(n % 100) : "");
        if (n < 100000)
            return convert(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " + convert(n % 1000) : "");
        if (n < 10000000)
            return convert(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " + convert(n % 100000) : "");
        return convert(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " + convert(n % 10000000) : "");
    }

    private void drawRowCell(PDPageContentStream contentStream, String text, float x, float y, float w, float h,
            boolean bold) throws IOException {
        contentStream.moveTo(x + w, y);
        contentStream.lineTo(x + w, y - h);
        contentStream.stroke();
        drawText(contentStream, text, x + 2, y - h + 5, bold);
    }

    private void drawText(PDPageContentStream contentStream, String text, float x, float y, boolean bold)
            throws IOException {
        if (text == null)
            text = "";
        contentStream.beginText();
        contentStream.setFont(
                new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA),
                9);
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
