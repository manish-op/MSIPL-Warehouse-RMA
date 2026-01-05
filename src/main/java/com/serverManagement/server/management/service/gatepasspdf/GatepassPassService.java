package com.serverManagement.server.management.service.gatepasspdf;

// ... (existing imports remain the same)
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.serverManagement.server.management.entity.gatePass.InwardGatePassEntity;
import com.serverManagement.server.management.entity.gatePass.ItemListViaGatePassInward;
import com.serverManagement.server.management.entity.itemRepairDetails.FruEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@Service
public class GatepassPassService {

    // --- Page Layout Constants (A4 Size) ---
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 40;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    // --- Color Palette: Changed to B/W/Red to match the paper form ---
    private static final Color COLOR_TITLE = Color.BLACK;
    private static final Color COLOR_TEXT_BODY = Color.BLACK;
    private static final Color COLOR_SL_NO = new Color(204, 0, 0); // Red for the Serial Number
    private static final Color COLOR_BORDER = Color.BLACK;
    private static final Color TABLE_ROW_LIGHT = Color.WHITE;

    // --- Font Variables ---
    private PDType0Font fontRegular;
    private PDType0Font fontBold;

    public ResponseEntity<byte[]> generateInvoicePdf(InwardGatePassEntity inwardGatepass, List<FruEntity> fruList)
            throws IOException {
        try (PDDocument document = new PDDocument()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // === FONT LOADING ===
            try (InputStream fontRegularStream = getClass().getClassLoader()
                    .getResourceAsStream("font/Roboto/Roboto-Regular.ttf");
                    InputStream fontBoldStream = getClass().getClassLoader()
                            .getResourceAsStream("font/Roboto/Roboto-Bold.ttf")) {

                if (fontRegularStream == null || fontBoldStream == null) {
                    fontRegular = PDType0Font.load(document,
                            getClass().getClassLoader().getResourceAsStream("font/Helvetica.ttf"), true);
                    fontBold = PDType0Font.load(document,
                            getClass().getClassLoader().getResourceAsStream("font/Helvetica-Bold.ttf"), true);
                } else {
                    fontRegular = PDType0Font.load(document, fontRegularStream, true);
                    fontBold = PDType0Font.load(document, fontBoldStream, true);
                }
            } catch (Exception e) {
                throw new IOException("Failed to load fonts for PDF generation: " + e.getMessage(), e);
            }

            // --- Page 1: Paper Form Layout ---
            generateInvoicePage(document, inwardGatepass);

            if (fruList != null && !fruList.isEmpty()) {
                generateGatePassStickersPage(document, fruList);
            }

            document.save(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "Gatepass_" + inwardGatepass.getRegionDetails().getCity() + "-" + inwardGatepass.getId()
                    + ".pdf";
            ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(filename).build();
            headers.setContentDisposition(contentDisposition);

            return ResponseEntity.ok().headers(headers).body(outputStream.toByteArray());
        }
    }

    private void generateInvoicePage(PDDocument document, InwardGatePassEntity inwardGatepass) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float yPosition = PAGE_HEIGHT - MARGIN;

            // --- Header Section ---
            yPosition = drawHeader(document, contentStream, inwardGatepass, yPosition);
            yPosition -= 20;

            // --- Items Table (KEPT AS IS) ---
            yPosition = drawItemsTable(document, page, contentStream, inwardGatepass.getItemList(), yPosition);

            // --- Footer Section (Position Fixed to bottom) ---
            drawFooter(contentStream, MARGIN);
        }
    }

    private float drawHeader(PDDocument document, PDPageContentStream contentStream,
            InwardGatePassEntity inwardGatepass, float y) throws IOException {
        float startY = y;
        final float LINE_HEIGHT = 18;
        final float TITLE_Y = startY;

        // 1. Logo and Company Name (LEFT)
        try (InputStream logoStream = getClass().getClassLoader().getResourceAsStream("images/companyLogo.png")) {
            if (logoStream != null) {
                BufferedImage awtImage = javax.imageio.ImageIO.read(logoStream);
                PDImageXObject companyLogo = LosslessFactory.createFromImage(document, awtImage);
                contentStream.drawImage(companyLogo, MARGIN, TITLE_Y - 17, 30, 30);
            }
        } catch (IOException e) {
            System.err.println("Error loading company logo: " + e.getMessage());
            drawText(contentStream, fontBold, 10, "MS", MARGIN + 10, TITLE_Y + 5, COLOR_TITLE);
        }

        drawText(contentStream, fontBold, 10, "MOTOROLA SOLUTIONS", MARGIN + 40, TITLE_Y, COLOR_TITLE);

        // Address Block: Wrapped Text Implementation
        String address = "Dlf Cyber City, Tower 8 5th Floor, Sector 24, Gurugram, Haryana 122002, India";
        float addressStartX = MARGIN + 40;
        float addressStartY = TITLE_Y - 10;
        float addressMaxWidth = 150; // Limit address width to 200 points

        // Draw wrapped address and capture its height
        float addressHeight = drawWrappedText(contentStream, fontRegular, 7, address, addressStartX, addressStartY,
                addressMaxWidth, COLOR_TEXT_BODY);

        // Calculate the adjusted start Y for the details box, based on address height
        float verticalSpaceBelowAddress = 10;
        float detailBoxTopY = startY - 40 - addressHeight + verticalSpaceBelowAddress;

        // 2. Main Title (CENTER)
        drawText(contentStream, fontBold, 14, "INWARD GATE PASS", PAGE_WIDTH / 2, TITLE_Y, COLOR_TITLE, "CENTER");

        // 3. Serial Number (RIGHT)
        String gatePassId = inwardGatepass.getId().toString();
        drawText(contentStream, fontRegular, 10, "Sl. No.", PAGE_WIDTH - MARGIN - 70, TITLE_Y + 5, COLOR_TITLE);
        drawText(contentStream, fontBold, 12, gatePassId, PAGE_WIDTH - MARGIN - 5, TITLE_Y + 5, COLOR_SL_NO, "RIGHT");

        // --- Draw Details Section (Under the header, with bounding boxes) ---

        // Draw bounding box for the entire details section
        float detailBoxHeight = LINE_HEIGHT * 4;
        float detailBoxBottomY = detailBoxTopY - detailBoxHeight;
        contentStream.setStrokingColor(COLOR_BORDER);
        contentStream.setLineWidth(1f);
        contentStream.addRect(MARGIN, detailBoxBottomY, CONTENT_WIDTH, detailBoxHeight);
        contentStream.stroke();

        // Vertical Divider
        float splitX = PAGE_WIDTH / 2 - 20;
        contentStream.moveTo(splitX, detailBoxBottomY);
        contentStream.lineTo(splitX, detailBoxTopY);
        contentStream.stroke();

        // Horizontal Dividers (3 lines for 4 rows)
        contentStream.moveTo(MARGIN, detailBoxTopY - LINE_HEIGHT * 1);
        contentStream.lineTo(PAGE_WIDTH - MARGIN, detailBoxTopY - LINE_HEIGHT * 1);
        contentStream.stroke();

        contentStream.moveTo(MARGIN, detailBoxTopY - LINE_HEIGHT * 2);
        contentStream.lineTo(PAGE_WIDTH - MARGIN, detailBoxTopY - LINE_HEIGHT * 2);
        contentStream.stroke();

        contentStream.moveTo(MARGIN, detailBoxTopY - LINE_HEIGHT * 3);
        contentStream.lineTo(PAGE_WIDTH - MARGIN, detailBoxTopY - LINE_HEIGHT * 3);
        contentStream.stroke();

        // Vertical offset to center text in the 18pt high row
        final float VERTICAL_OFFSET = 11;

        // Row 1
        drawDetailField(contentStream, fontBold, fontRegular, "CUSTOMER NAME",
                capitalizeFirstLetter(inwardGatepass.getPartyName()),
                MARGIN + 5, detailBoxTopY - LINE_HEIGHT + VERTICAL_OFFSET, splitX - MARGIN - 5);

        drawDetailField(contentStream, fontBold, fontRegular, "DATE",
                inwardGatepass.getCreatedDate().format(DateTimeFormatter.ofPattern("dd / MM / yyyy")),
                splitX + 5, detailBoxTopY - LINE_HEIGHT + VERTICAL_OFFSET, PAGE_WIDTH - MARGIN - splitX - 5);

        // Row 2
        String partyAddress = inwardGatepass.getPartyAddress();
        drawDetailField(contentStream, fontBold, fontRegular, "ADDRESS", partyAddress,
                MARGIN + 5, detailBoxTopY - LINE_HEIGHT * 2 + VERTICAL_OFFSET, splitX - MARGIN - 5);

        String dcInvoiceNo = inwardGatepass.getRegionDetails().getCity() + "-" + inwardGatepass.getId();
        drawDetailField(contentStream, fontBold, fontRegular, " INVOICE NO", dcInvoiceNo,
                splitX + 5, detailBoxTopY - LINE_HEIGHT * 2 + VERTICAL_OFFSET, PAGE_WIDTH - MARGIN - splitX - 5);

        // Rows 3 and 4 are empty lines for manual entries.

        return detailBoxBottomY; // New Y position below the details box
    }

    /**
     * Helper to draw the label and the value in the details box.
     */
    private void drawDetailField(PDPageContentStream cs, PDType0Font labelFont, PDType0Font valueFont, String label,
            String value, float x, float y, float width) throws IOException {
        // Draw Label
        drawText(cs, labelFont, 8, label, x, y, COLOR_TITLE);

        // Calculate where the value starts
        float labelWidth = labelFont.getStringWidth(label) / 1000 * 8;
        float valueX = x + labelWidth + 5;

        // Draw Value
        drawText(cs, valueFont, 10, value, valueX, y, COLOR_TEXT_BODY);

        // Underline logic remains removed as requested.
    }

    // --- drawItemsTable (UPDATED) ---
    private float drawItemsTable(PDDocument document, PDPage currentPage, PDPageContentStream currentContentStream,
            List<ItemListViaGatePassInward> items, float y) throws IOException {
        // Adjusted column widths and headers (Unit column removed)
        final float[] columnWidths = { 40, 320, 60, 100 }; // Sl. No., Description (Increased width), Quantity, Remarks
        final String[] headers = { "Sl. No.", "Description of Items", "Quantity", "Remarks" };
        final float tableTopY = y;
        float headerHeight = 20;

        drawTableHeader(currentContentStream, headers, columnWidths, tableTopY, headerHeight);
        y -= headerHeight;

        int itemCounter = 1;
        for (ItemListViaGatePassInward item : items) {
            String serialNo = item.getSerialNo() != null ? item.getSerialNo() : "";
            String details = capitalizeFirstLetter(item.getKeywordName());
            String description = details + (serialNo.isEmpty() ? "" : " (S.No: " + serialNo + ")");

            String remark = item.getFaultDescription();

            // Column indices for wrapped text calculation: Description (1), Remark (3)
            float requiredHeight = calculateRowHeight(description, remark, columnWidths[1], columnWidths[3], 10f);
            if (requiredHeight < 20)
                requiredHeight = 20;

            // CHECK: If space is left for row AND the entire footer section (approx 100
            // points)
            if (y - requiredHeight < MARGIN + 100) {
                currentContentStream.close();
                currentPage = new PDPage(PDRectangle.A4);
                document.addPage(currentPage);
                currentContentStream = new PDPageContentStream(document, currentPage);
                y = PAGE_HEIGHT - MARGIN;
                drawTableHeader(currentContentStream, headers, columnWidths, y, headerHeight);
                y -= headerHeight;
            }

            // Data array (4 elements)
            String[] rowData = {
                    String.valueOf(itemCounter),
                    description,
                    "1", // Quantity is index 2
                    remark // Remark is index 3
            };
            drawTableRow(currentContentStream, rowData, columnWidths, y, requiredHeight, false);
            y -= requiredHeight;
            itemCounter++;
        }

        int maxVisibleRows = 5;
        int drawnRows = items.size();
        for (int i = drawnRows; i < maxVisibleRows; i++) {
            float rowHeight = 20;
            // CHECK: If space is left for row AND the entire footer section (approx 100
            // points)
            if (y - rowHeight < MARGIN + 100)
                break;

            // Empty data array (4 elements)
            String[] emptyRowData = { "", "", "", "" };
            drawTableRow(currentContentStream, emptyRowData, columnWidths, y, rowHeight, false);
            y -= rowHeight;
        }

        currentContentStream.setStrokingColor(COLOR_BORDER);
        currentContentStream.setLineWidth(1f);
        float x = MARGIN;
        for (float width : columnWidths) {
            currentContentStream.moveTo(x, y);
            currentContentStream.lineTo(x, tableTopY);
            currentContentStream.stroke();
            x += width;
        }
        currentContentStream.moveTo(x, y);
        currentContentStream.lineTo(x, tableTopY);
        currentContentStream.stroke();

        return y;
    }

    // --- drawTableHeader (UPDATED) ---
    private void drawTableHeader(PDPageContentStream stream, String[] headers, float[] colWidths, float y, float height)
            throws IOException {
        stream.setNonStrokingColor(Color.WHITE);
        stream.addRect(MARGIN, y - height, CONTENT_WIDTH, height);
        stream.fill();

        stream.setStrokingColor(COLOR_BORDER);
        stream.setLineWidth(1f);
        stream.addRect(MARGIN, y - height, CONTENT_WIDTH, height);
        stream.stroke();

        float x = MARGIN;
        float textYOffset = y - (height / 2) - 3;

        for (int i = 0; i < headers.length; i++) {
            if (i > 0) {
                stream.moveTo(x, y);
                stream.lineTo(x, y - height);
                stream.stroke();
            }
            drawText(stream, fontBold, 10, headers[i], x + 5, textYOffset, COLOR_TITLE);
            x += colWidths[i];
        }
    }

    // --- drawTableRow (UPDATED) ---
    private void drawTableRow(PDPageContentStream stream, String[] data, float[] colWidths, float y, float height,
            boolean isOddRow) throws IOException {
        stream.setNonStrokingColor(Color.WHITE);
        stream.addRect(MARGIN, y - height, CONTENT_WIDTH, height);
        stream.fill();

        stream.setStrokingColor(COLOR_BORDER);
        stream.setLineWidth(1f);
        stream.moveTo(MARGIN, y - height);
        stream.lineTo(MARGIN + CONTENT_WIDTH, y - height);
        stream.stroke();

        float x = MARGIN;
        float textYOffset = y - (height / 2) - 4;

        for (int i = 0; i < data.length; i++) {
            // Description (index 1), Remark (index 3)
            if (i == 1 || i == 3) {
                drawWrappedText(stream, fontRegular, 10, data[i], x + 5, y - 8, colWidths[i] - 10, COLOR_TEXT_BODY);
            } else {
                drawText(stream, fontRegular, 10, data[i], x + 5, textYOffset, COLOR_TEXT_BODY);
            }
            x += colWidths[i];
        }
    }

    private float calculateRowHeight(String details, String remark, float detailsWidth, float remarkWidth,
            float fontSize) throws IOException {
        List<String> detailsLines = splitTextIntoLines(details, detailsWidth - 10, fontSize);
        List<String> remarkLines = splitTextIntoLines(remark, remarkWidth - 10, fontSize);

        int maxLines = Math.max(detailsLines.size(), remarkLines.size());
        if (maxLines == 0)
            maxLines = 1;

        float lineHeight = fontRegular.getFontDescriptor().getCapHeight() / 1000 * fontSize + 6;
        return Math.max(20, maxLines * lineHeight + 10);
    }

    private void drawFooter(PDPageContentStream contentStream, float bottomMargin) throws IOException {

        // Footer fixed to bottom of the page
        float footerBlockHeight = 90;
        float startY = bottomMargin + footerBlockHeight;

        // 1. Separator line
        contentStream.setStrokingColor(COLOR_BORDER);
        contentStream.setLineWidth(1f);
        contentStream.moveTo(MARGIN, startY);
        contentStream.lineTo(PAGE_WIDTH - MARGIN, startY);
        contentStream.stroke();

        float footerY = startY - 10;
        final float LEFT_COL_X = MARGIN;

        // --- Left Side: RECEIVED BY, DEPARTMENT, NAME ---

        // RECEIVED BY
        drawText(contentStream, fontBold, 10, "RECEIVED BY", LEFT_COL_X, footerY, COLOR_TITLE);

        footerY -= 20;
        // DEPARTMENT
        drawText(contentStream, fontBold, 10, "DEPARTMENT", LEFT_COL_X, footerY, COLOR_TITLE);
        drawText(contentStream, fontRegular, 10, "________________________", LEFT_COL_X, footerY - 5, COLOR_TEXT_BODY);

        footerY -= 25;
        // NAME
        drawText(contentStream, fontBold, 10, "NAME", LEFT_COL_X, footerY, COLOR_TITLE);
        drawText(contentStream, fontRegular, 10, "________________________", LEFT_COL_X, footerY - 5, COLOR_TEXT_BODY);

        // --- Right Side: SECURITY SEAL & SIGNATURE ---
        float signatureY = startY - 10;

        // Text alignment to the right edge of the content area
        drawText(contentStream, fontBold, 10, "SECURITY SEAL & SIGNATURE", PAGE_WIDTH - MARGIN, signatureY, COLOR_TITLE,
                "RIGHT");
        // Underline alignment to the right edge of the content area
        drawText(contentStream, fontRegular, 10, "________________________", PAGE_WIDTH - MARGIN, signatureY - 5,
                COLOR_TEXT_BODY, "RIGHT");
    }

    // --- UTILITY & HELPER METHODS (KEPT AS IS) ---

    private void drawText(PDPageContentStream cs, PDType0Font font, float fontSize, String text, float x, float y,
            Color color, String align) throws IOException {
        String sanitized = sanitizeText(text);
        float textWidth = font.getStringWidth(sanitized) / 1000 * fontSize;
        float startX = x;
        if ("RIGHT".equals(align)) {
            startX = x - textWidth;
        } else if ("CENTER".equals(align)) {
            startX = x - textWidth / 2;
        }

        cs.beginText();
        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(startX, y);
        cs.showText(sanitized);
        cs.endText();
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

    private void drawText(PDPageContentStream cs, PDType0Font font, float fontSize, String text, float x, float y,
            Color color) throws IOException {
        drawText(cs, font, fontSize, text, x, y, color, "LEFT");
    }

    private float drawWrappedText(PDPageContentStream cs, PDType0Font font, float fontSize, String text, float x,
            float y, float maxWidth, Color color) throws IOException {
        if (text == null)
            text = "";
        // Use the regular font for splitting and wrapping
        List<String> lines = splitTextIntoLines(text, maxWidth, fontSize);
        // Calculate line height based on font metrics and desired spacing
        float lineHeight = font.getFontDescriptor().getCapHeight() / 1000 * fontSize + 2;
        float currentY = y;
        for (String line : lines) {
            drawText(cs, font, fontSize, line, x, currentY, color);
            currentY -= lineHeight;
        }
        // Return total height used, adjusted back from the starting point
        return y - currentY;
    }

    private List<String> splitTextIntoLines(String text, float maxWidth, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        // First split by actual newline characters
        String[] manualLines = text.split("\\r?\\n");

        // Since this utility is used for address wrapping, ensure fontRegular is used
        // for measurement
        PDType0Font measurementFont = fontRegular;

        for (String manualLine : manualLines) {
            String[] words = manualLine.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                float width = measurementFont.getStringWidth(currentLine + (currentLine.length() > 0 ? " " : "") + word)
                        / 1000 * fontSize;
                if (width < maxWidth) {
                    if (currentLine.length() > 0)
                        currentLine.append(" ");
                    currentLine.append(word);
                } else {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        // Word itself is longer than maxWidth, just add it (or could force character
                        // split)
                        lines.add(word);
                        currentLine = new StringBuilder();
                    }
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        return lines;
    }

    private BufferedImage generateQRCodeImage(String data) {
        try {
            int size = 200;
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            System.err.println("Error generating QR code image: " + e.getMessage());
            return null;
        }
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    // --- Sticker Page Generation (KEPT AS IS) ---
    private void generateGatePassStickersPage(PDDocument document, List<FruEntity> fruList) throws IOException {
        float stickerWidth = 250;
        float stickerHeight = 180;
        float padding = 8;
        int stickersPerRow = 2;
        float horizontalSpacing = (PAGE_WIDTH - (2 * MARGIN) - (stickersPerRow * stickerWidth)) / (stickersPerRow - 1);
        float verticalSpacing = 20;

        int maxRowsPerPage = (int) ((PAGE_HEIGHT - (2 * MARGIN)) / (stickerHeight + verticalSpacing));
        PDPageContentStream contentStream = null;

        for (int i = 0; i < fruList.size(); i++) {
            if (i % (stickersPerRow * maxRowsPerPage) == 0) {
                if (contentStream != null) {
                    contentStream.close();
                }
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
            }

            int row = (i / stickersPerRow) % maxRowsPerPage;
            int col = i % stickersPerRow;
            float x = MARGIN + col * (stickerWidth + horizontalSpacing);
            float y = PAGE_HEIGHT - MARGIN - (row + 1) * stickerHeight - row * verticalSpacing;

            drawSingleSticker(document, contentStream, fruList.get(i), x, y, stickerWidth, stickerHeight, padding);
        }

        if (contentStream != null) {
            contentStream.close();
        }
    }

    private void drawSingleSticker(PDDocument document, PDPageContentStream contentStream, FruEntity fru, float x,
            float y, float width, float height, float padding) throws IOException {
        // Sticker Border
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(1);
        contentStream.addRect(x, y, width, height);
        contentStream.stroke();

        float innerX = x + padding;
        float innerWidth = width - 2 * padding;
        float currentY = y + height - padding;

        // Header
        drawText(contentStream, fontBold, 9, "Motorola Solutions", innerX, currentY - 5, COLOR_TEXT_BODY);
        contentStream.setStrokingColor(COLOR_BORDER);
        contentStream.moveTo(x, currentY - 15);
        contentStream.lineTo(x + width, currentY - 15);
        contentStream.stroke();
        currentY -= 25;

        // Content
        String serialNo = fru.getRepairingIdList().getSerialNo() != null ? fru.getRepairingIdList().getSerialNo()
                : "N/A";
        String ticketNo = fru.getRepairingIdList().getId() != null ? fru.getRepairingIdList().getId().toString()
                : "N/A";

        drawText(contentStream, fontBold, 8, "S.No: " + serialNo, innerX, currentY, COLOR_TEXT_BODY);
        drawText(contentStream, fontRegular, 8, "Ticket: " + ticketNo, x + width - padding, currentY, COLOR_TEXT_BODY,
                "RIGHT");
        currentY -= 12;

        String rmaNo = fru.getRmaNo() != null ? fru.getRmaNo() : "N/A";
        drawText(contentStream, fontRegular, 8, "RMA No: " + rmaNo, innerX, currentY, COLOR_TEXT_BODY);
        currentY -= 12;

        String customer = fru.getInGatepassID().getPartyName() != null
                ? capitalizeFirstLetter(fru.getInGatepassID().getPartyName())
                : "N/A";
        drawText(contentStream, fontRegular, 8, "Customer: " + customer, innerX, currentY, COLOR_TEXT_BODY);
        currentY -= 12;

        String fault = fru.getRepairingIdList().getFaultDetails() != null ? fru.getRepairingIdList().getFaultDetails()
                : "N/A";
        currentY -= drawWrappedText(contentStream, fontRegular, 8, "Fault: " + fault, innerX, currentY, innerWidth,
                COLOR_TEXT_BODY);
        currentY -= 10;

        String date = fru.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yy"));
        drawText(contentStream, fontRegular, 8, "Date: " + date, innerX, currentY, COLOR_TEXT_BODY);

        // QR Code
        String qrData = String.format("TicketNo:%s|SerialNo:%s|RmaNo:%s|Customer:%s", ticketNo, serialNo, rmaNo,
                customer);
        BufferedImage qrImage = generateQRCodeImage(qrData);
        if (qrImage != null) {
            PDImageXObject pdQrImage = LosslessFactory.createFromImage(document, qrImage);
            float qrSize = 60;
            contentStream.drawImage(pdQrImage, x + (width - qrSize) / 2, y + padding, qrSize, qrSize);
        }
    }
}