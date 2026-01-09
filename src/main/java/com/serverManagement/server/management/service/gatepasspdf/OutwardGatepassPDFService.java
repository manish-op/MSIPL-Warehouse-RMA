package com.serverManagement.server.management.service.gatepasspdf;

import java.awt.Color;
import java.awt.image.BufferedImage; // Logo loading still needs this
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream; // Logo loading still needs this
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO; // Logo loading still needs this

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
// --- START: FONT CHANGES ---
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
// --- END: FONT CHANGES ---
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.krysalis.barcode4j.TextAlignment;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.serverManagement.server.management.entity.gatePass.ItemListViaGatePassOutwardEntity;
import com.serverManagement.server.management.entity.gatePass.OutwardGatepassEntity;

@Service
public class OutwardGatepassPDFService {

    // PDF Dimensions (A4)
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN_LEFT = 36; // 0.5 inch
    private static final float MARGIN_RIGHT = 36;
    private static final float MARGIN_TOP = 36;
    private static final float MARGIN_BOTTOM = 36;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    // Font variables (loaded once)
    // --- Use the base PDFont interface ---
    private PDFont robotoRegular;
    private PDFont robotoBold;
    // --- Removed emojiFont variable ---

    public ResponseEntity<byte[]> generateInvoicePdf(OutwardGatepassEntity outwardGatepass)
            throws Exception, IOException {
        PDDocument document = new PDDocument();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Load standard built-in fonts. These require no files.
        robotoRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        robotoBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        // --- Page 1: Invoice Details ---
        generateInvoicePage(document, outwardGatepass);

        document.save(outputStream);
        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "OutGatepass_"
                + outwardGatepass.getRegionDetails().getCity()
                + "-" + outwardGatepass.getId()
                + ".pdf";

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(filename)
                .build();
        headers.setContentDisposition(contentDisposition);

        headers.setContentLength(outputStream.toByteArray().length);

        return ResponseEntity.ok().headers(headers).body(outputStream.toByteArray());
    }

    private void generateInvoicePage(PDDocument document, OutwardGatepassEntity outwardGatepass) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        float yPosition = PAGE_HEIGHT - MARGIN_TOP;

        PDImageXObject companyLogo = null;
        try (InputStream logoStream = getClass().getClassLoader().getResourceAsStream("images/companyLogo.png")) {
            if (logoStream == null) {
                throw new IOException("Cannot find logo 'images/companyLogo.png' in classpath.");
            }
            BufferedImage awtImage = ImageIO.read(logoStream);
            companyLogo = LosslessFactory.createFromImage(document, awtImage);

            float logoWidth = 60;
            float logoHeight = 60;
            contentStream.drawImage(companyLogo, MARGIN_LEFT + 5, yPosition - logoHeight, logoWidth, logoHeight);
        } catch (IOException e) {
            System.err.println("Error loading company logo: " + e.getMessage());
            // Optionally draw a placeholder text if logo fails to load
            drawText(contentStream, robotoBold, 18, "Motorola", MARGIN_LEFT + 5, yPosition - 20, TextAlignment.TA_LEFT);
        }

        // Company Name
        drawText(contentStream, robotoBold, 24, "Motorola Solutions India Pvt. Ltd.", MARGIN_LEFT + 70, yPosition - 40,
                TextAlignment.TA_LEFT);
        yPosition -= 100; // Adjusted for logo height and company name

        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(1);
        contentStream.moveTo(MARGIN_LEFT, PAGE_HEIGHT - MARGIN_TOP - 70);
        contentStream.lineTo(PAGE_WIDTH - MARGIN_RIGHT, PAGE_HEIGHT - MARGIN_TOP - 70);
        contentStream.stroke();

        yPosition += 10;
        String inwardText = "Outward Gate Pass";
        drawText(contentStream, robotoBold, 17, inwardText, MARGIN_LEFT + 250, yPosition, TextAlignment.TA_CENTER);
        // --- Gatepass Number & Date (Top Right) ---
        yPosition -= 30;
        String passDetails = "GatePass No#:- " + outwardGatepass.getRegionDetails().getCity() + "-"
                + outwardGatepass.getId();
        drawText(contentStream, robotoBold, 12, passDetails, PAGE_WIDTH - MARGIN_RIGHT, yPosition,
                TextAlignment.TA_RIGHT);

        yPosition -= 20;
        String passDetails2 = " Date:- "
                + outwardGatepass.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        drawText(contentStream, robotoBold, 12, passDetails2, PAGE_WIDTH - MARGIN_RIGHT, yPosition,
                TextAlignment.TA_RIGHT);

        // --- Party Details (Left) ---
        yPosition += 25;
        drawText(contentStream, robotoBold, 12, "Customer:- " + capitalizeFirstLetter(outwardGatepass.getPartyName()),
                MARGIN_LEFT, yPosition, TextAlignment.TA_LEFT);
        yPosition -= 20;
        float addressTextHeight = drawWrappedText(contentStream, robotoRegular, 12,
                "Address:- " + outwardGatepass.getPartyAddress(), MARGIN_LEFT, yPosition, 250, TextAlignment.TA_LEFT);
        yPosition -= 10 + addressTextHeight;
        // Customer Mobile
        drawText(contentStream, robotoRegular, 12, "Mobile:- " + outwardGatepass.getPartyContact(), MARGIN_LEFT,
                yPosition, TextAlignment.TA_LEFT); // Removed emoji symbol

        // --- Invoice Items Table ---
        yPosition -= 25;
        float tableStartY = yPosition;
        float[] columnWidths = { 0.5f, 3.5f, 1f, 1f, 2f }; // S.No, Details, Quantity, Total Quantity, Remark
        float[] actualColumnWidths = new float[columnWidths.length];
        float totalRelativeWidth = 0;
        for (float w : columnWidths) {
            totalRelativeWidth += w;
        }
        for (int i = 0; i < columnWidths.length; i++) {
            actualColumnWidths[i] = (columnWidths[i] / totalRelativeWidth) * CONTENT_WIDTH;
        }

        float tableHeaderHeight = 25;
        float tableRowHeight = 20; // Base row height

        // Draw Table Headers
        drawTableHeader(contentStream, robotoBold, 10,
                new String[] { "S.No", "Details", "Quantity", "Total Quantity", "Remark" }, actualColumnWidths,
                MARGIN_LEFT, tableStartY, tableHeaderHeight);
        yPosition -= tableHeaderHeight;

        int itemCounter = 1;
        for (ItemListViaGatePassOutwardEntity item : outwardGatepass.getItemList()) {
            // Check for page break before drawing a new row
            if (yPosition < MARGIN_BOTTOM + tableRowHeight + 50) { // +50 for some footer space
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                yPosition = PAGE_HEIGHT - MARGIN_TOP; // Reset Y position for new page
                // Re-draw header on new page
                drawTableHeader(contentStream, robotoBold, 10,
                        new String[] { "S.No", "Details", "Quantity", "Total Quantity", "Remark" }, actualColumnWidths,
                        MARGIN_LEFT, yPosition, tableHeaderHeight);
                yPosition -= tableHeaderHeight;
            }

            // Draw table row data
            float currentX = MARGIN_LEFT;
            float cellY = yPosition;
            float rowMaxHeight = tableRowHeight; // Will adjust if text wraps

            // S.No
            drawCell(contentStream, robotoRegular, 10, String.valueOf(itemCounter), currentX, cellY,
                    actualColumnWidths[0], rowMaxHeight, TextAlignment.TA_CENTER);
            currentX += actualColumnWidths[0];

            // Details
            rowMaxHeight = drawMultiLineCell(contentStream, robotoRegular, 10,
                    item.getSerialNo().toString() + "  " + capitalizeFirstLetter(item.getKeywordName()), currentX,
                    cellY - 3, actualColumnWidths[1], tableRowHeight, TextAlignment.TA_LEFT);
            currentX += actualColumnWidths[1];

            // Quantity
            drawCell(contentStream, robotoRegular, 10, String.valueOf(1), currentX, cellY, actualColumnWidths[2],
                    rowMaxHeight, TextAlignment.TA_CENTER);
            currentX += actualColumnWidths[2];

            // Total Quantity
            drawCell(contentStream, robotoRegular, 10, String.valueOf(1), currentX, cellY, actualColumnWidths[3],
                    rowMaxHeight, TextAlignment.TA_CENTER);
            currentX += actualColumnWidths[3];

            // Remark
            float remarkHeight = drawMultiLineCell(contentStream, robotoRegular, 10, item.getRemark(), currentX,
                    cellY - 3, actualColumnWidths[4], tableRowHeight, TextAlignment.TA_LEFT);
            rowMaxHeight = Math.max(rowMaxHeight, remarkHeight);

            // Draw horizontal line below the row
            contentStream.setLineWidth(1f);
            contentStream.moveTo(MARGIN_LEFT, yPosition - rowMaxHeight);
            contentStream.lineTo(PAGE_WIDTH - MARGIN_RIGHT, yPosition - rowMaxHeight);
            contentStream.stroke();

            float verticalLineYTop = cellY; // Top of the current row
            float verticalLineYBottom = cellY - rowMaxHeight; // Bottom of the current row

            float currentVerticalLineX = MARGIN_LEFT;
            for (int i = 0; i < actualColumnWidths.length; i++) {
                // Draw left border of the current cell
                contentStream.moveTo(currentVerticalLineX, verticalLineYTop);
                contentStream.lineTo(currentVerticalLineX, verticalLineYBottom);
                contentStream.stroke(); // Draw the vertical line

                currentVerticalLineX += actualColumnWidths[i];
            }
            // Draw the very rightmost border of the last cell
            contentStream.moveTo(currentVerticalLineX, verticalLineYTop);
            contentStream.lineTo(currentVerticalLineX, verticalLineYBottom);
            contentStream.stroke();

            // --- Adjust Y position for the next row ---
            yPosition -= rowMaxHeight; // Move down by the actual height of the current row
            itemCounter++;
        }

        // --- Footer ---
        yPosition -= 50; // Space before footer

        // Left side: Received By
        String receivedByText = "Issued By: " + outwardGatepass.getCreatedBy();
        String deptText = "Department: Service";
        String signLine = "_________________________";

        if (yPosition < MARGIN_BOTTOM + 80) { // Check if new page is needed for footer
            contentStream.close();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            yPosition = PAGE_HEIGHT - MARGIN_TOP; // Reset Y position for new page
        }

        float footerLeftX = MARGIN_LEFT;
        float footerRightX = PAGE_WIDTH - MARGIN_RIGHT - 120; // Adjusted for stamp size

        drawText(contentStream, robotoBold, 12, receivedByText, footerLeftX, yPosition, TextAlignment.TA_LEFT);
        yPosition -= 20;
        drawText(contentStream, robotoRegular, 12, deptText, footerLeftX, yPosition, TextAlignment.TA_LEFT);
        yPosition -= 25;
        drawText(contentStream, robotoRegular, 12, "Signature:" + signLine, footerLeftX, yPosition,
                TextAlignment.TA_LEFT);

        PDImageXObject companyStamp = null;
        try (InputStream stampStream = getClass().getClassLoader().getResourceAsStream("images/companyLogo.png")) {
            if (stampStream == null) {
                throw new IOException("Cannot find stamp 'images/companyLogo.png' in classpath.");
            }
            BufferedImage awtImage = ImageIO.read(stampStream);
            companyStamp = LosslessFactory.createFromImage(document, awtImage);

            float stampWidth = 30;
            float stampHeight = 30;
            contentStream.drawImage(companyStamp, footerRightX, yPosition, stampWidth, stampHeight); // Position above
            // signature
            // line
        } catch (IOException e) {
            System.err.println("Error loading company stamp: " + e.getMessage());
            // Optionally draw a placeholder box for the stamp if image fails to load
            drawText(contentStream, robotoRegular, 10, "COMPANY STAMP", footerRightX + 50, yPosition + 30,
                    TextAlignment.TA_CENTER);
            contentStream.setStrokingColor(Color.BLACK);
            contentStream.setLineWidth(1);
            contentStream.addRect(footerRightX, yPosition - 20, 100, 100);
            contentStream.stroke();
        }

        contentStream.close();
    }

    // --- Helper Methods ---
    // --- START: Changed all PDType0Font to PDFont ---

    // Generic method to draw text with alignment
    private void drawText(PDPageContentStream contentStream, PDFont font, float fontSize, String text, float x,
            float y, TextAlignment alignment) throws IOException {
        String sanitized = sanitizeText(text);
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.setNonStrokingColor(Color.BLACK);
        // contentStream.fillAndStroke(); // Border color for all cells
        contentStream.setLineWidth(1);
        float textWidth = font.getStringWidth(sanitized) / 1000 * fontSize;

        float startX;
        if (alignment == TextAlignment.TA_LEFT) {
            startX = x;
        } else if (alignment == TextAlignment.TA_RIGHT) {
            startX = x - textWidth;
        } else { // CENTER
            startX = x - (textWidth / 2);
        }
        contentStream.newLineAtOffset(startX, y);
        contentStream.showText(sanitized);
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

    // Helper to draw a table header row
    private void drawTableHeader(PDPageContentStream contentStream, PDFont font, float fontSize, String[] headers,
            float[] columnWidths, float startX, float startY, float rowHeight) throws IOException {

        // Set general border style for cells
        contentStream.setStrokingColor(Color.BLACK); // Border color for all cells
        contentStream.setLineWidth(1); // Border thickness

        float currentX = startX;

        for (int i = 0; i < headers.length; i++) {
            contentStream.setNonStrokingColor(new Color(231, 227, 240)); // Light grey/purple background for header
            contentStream.addRect(currentX, startY - rowHeight, columnWidths[i], rowHeight);
            contentStream.fillAndStroke(); // Fills the background and draws the border for the cell

            drawText(contentStream, font, fontSize, headers[i], currentX + columnWidths[i] / 2, // X for centering
                    startY - rowHeight / 2 - (getTextHeight(font, fontSize) / 2), // Y for vertical centering
                    TextAlignment.TA_CENTER); // Text color: Black
            currentX += columnWidths[i];
        }
    }

    // Helper to draw a single cell with basic content
    private void drawCell(PDPageContentStream contentStream, PDFont font, float fontSize, String text, float x,
            float y, float width, float height, TextAlignment alignment) throws IOException {
        // contentStream.addRect(currentX, startY - rowHeight, columnWidths[i],
        // rowHeight);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.fillAndStroke();
        drawText(contentStream, font, fontSize, text, x + (width / 2),
                y - (height / 2) - (getTextHeight(font, fontSize) / 2), alignment);
    }

    // Helper for multi-line text drawing within a cell
    private float drawMultiLineCell(PDPageContentStream contentStream, PDFont font, float fontSize, String text,
            float x, float y, float width, float minHeight, TextAlignment alignment) throws IOException {
        List<String> lines = splitTextIntoLines(font, fontSize, text, width - 10); // 10 for padding
        float currentY = y;
        float lineHeight = getTextHeight(font, fontSize) + 2; // Font height + small padding

        for (String line : lines) {
            drawText(contentStream, font, fontSize, line, x + 5, currentY - lineHeight, TextAlignment.TA_LEFT);
            currentY -= lineHeight;
        }
        float actualHeight = y - currentY;
        return Math.max(minHeight, actualHeight + 5); // Return actual height including padding, or minHeight
    }

    // Helper to split text into lines based on max width
    private List<String> splitTextIntoLines(PDFont font, float fontSize, String text, float maxWidth)
            throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        // First split by actual newline characters
        String[] manualLines = text.split("\\r?\\n");

        for (String manualLine : manualLines) {
            String[] words = manualLine.split(" ");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                if (font.getStringWidth(currentLine + " " + word) / 1000 * fontSize < maxWidth) {
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                } else {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        // Word itself is longer than maxWidth, just add it
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

    // Helper to get text height
    private float getTextHeight(PDFont font, float fontSize) {
        return font.getFontDescriptor().getCapHeight() / 1000 * fontSize;
    }

    private float drawWrappedText(PDPageContentStream contentStream, PDFont font, float fontSize, String text,
            float x, float y, float maxWidth, TextAlignment alignment) throws IOException {
        List<String> lines = splitTextIntoLines(font, fontSize, text, maxWidth); // Use your existing split function
        float currentY = y;
        float lineHeight = getTextHeight(font, fontSize) + 5; // Adjust line spacing as needed (fontSize + padding)

        for (String line : lines) {
            drawText(contentStream, font, fontSize, line, x, currentY, alignment);
            currentY -= lineHeight; // Move down for the next line
        }
        // Return the total height consumed by the wrapped text
        return y - currentY;
    }
    // --- END: Changed all PDType0Font to PDFont ---

    // capitalize first letter
    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}