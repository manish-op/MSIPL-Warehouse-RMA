package com.serverManagement.server.management.service.gatepasspdf;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.krysalis.barcode4j.TextAlignment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.serverManagement.server.management.entity.itemRepairDetails.FruEntity;

@Service
public class AfterGatepassFruTicketPringtPDF {
	private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
	private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
	private static final float MARGIN_LEFT = 36; // 0.5 inch
	private static final float MARGIN_RIGHT = 36;
	private static final float MARGIN_TOP = 36;
	private static final float MARGIN_BOTTOM = 36;

	// Font variables (loaded once)
	private PDType0Font robotoRegular;
	private PDType0Font robotoBold;

	public ResponseEntity<byte[]> generatePdf(FruEntity frudetails)
			throws Exception, IOException {
		PDDocument document = new PDDocument();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		// Load fonts once per document
		try {
			robotoRegular = PDType0Font.load(document, new File("src/main/resources/font/Roboto/Roboto-Regular.ttf"));
			robotoBold = PDType0Font.load(document, new File("src/main/resources/font/Roboto/Roboto-Medium.ttf"));
			// Ensure this font contains wide range of unicode characters, including emojis

		} catch (IOException e) {
			throw e;
		}

		// --- Page 1: Invoice Details ---
		

		// --- Page 2: Gate Pass Stickers ---
		generateGatePassStickersPage(document, frudetails);

		document.save(outputStream);
		document.close();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("inline",""+frudetails.getRmaNo() +".pdf");
		headers.setContentLength(outputStream.toByteArray().length);
		return ResponseEntity.ok().headers(headers).body(outputStream.toByteArray());
	}

	
	private void generateGatePassStickersPage(PDDocument document, FruEntity frudetails) throws IOException {
		float stickerWidth = 250;
		float stickerHeight = 180;

		float padding = 5; // Reduced padding for more content space
		// Assuming 2 stickers per row, calculate space evenly
		float spaceBetweenStickersX = (PAGE_WIDTH - (MARGIN_LEFT * 2) - (2 * stickerWidth)) / 1;
		if (spaceBetweenStickersX < 0)
			spaceBetweenStickersX = 10; // Ensure positive if calculations are tight
		float spaceBetweenStickersY = 20;

		// Calculate how many stickers can fit per row/column on a page
		int stickersPerRow = 2;
		// Calculate effective height for stickers per page, considering top margin and
		// some bottom space
		int maxRowsPerPage = (int) ((PAGE_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM)
				/ (stickerHeight + spaceBetweenStickersY));
		if (maxRowsPerPage == 0)
			maxRowsPerPage = 1; // Ensure at least one row can fit

		PDPageContentStream contentStream = null; // Declare once, manage lifecycle

		int stickerCount = 0;
			// --- Manage Page Creation and Content Stream ---
			if (stickerCount % (stickersPerRow * maxRowsPerPage) == 0) {
				// Close previous contentStream if it exists (i.e., not the very first page)
				
				// Add a new page and open its content stream
				PDPage page = new PDPage(PDRectangle.A4);
				document.addPage(page);
				contentStream = new PDPageContentStream(document, page); // Open new stream for the new page
			}

			// --- Calculate Current Sticker Position on Page ---
			int rowOnPage = (stickerCount / stickersPerRow) % maxRowsPerPage;
			int colOnPage = stickerCount % stickersPerRow;

			float x = MARGIN_LEFT + colOnPage * (stickerWidth + spaceBetweenStickersX);
			float y = (PAGE_HEIGHT - MARGIN_TOP) - (rowOnPage * (stickerHeight + spaceBetweenStickersY))
					- stickerHeight; // Calculate top-left Y of the sticker box

			// --- Draw Sticker Box ---
			contentStream.setStrokingColor(Color.BLACK);
			contentStream.setLineWidth(1);
			contentStream.addRect(x, y, stickerWidth, stickerHeight); // x, y are bottom-left, so draw at y (top edge)
			contentStream.stroke();

			// --- Draw Company Logo and Name INSIDE the Sticker Box ---
			// Top-left of the sticker box is (x, y)
			String companyLogoPath = "src/main/resources/images/companyLogo.png"; // Relative to resources/
			PDImageXObject companyLogo = null;
			float height = 20;
			try {
				// Use ClassPathResource for reliable loading
				companyLogo = PDImageXObject.createFromFile(companyLogoPath, document);
				float logoWidth = 20;
				float logoHeight = 20;
				// Position logo relative to sticker box's top-left (x, y)
				contentStream.drawImage(companyLogo, x + padding, y + stickerHeight - logoHeight - padding, logoWidth,
						logoHeight);
			} catch (IOException e) {
				System.err.println("Error loading company logo for sticker page: " + e.getMessage());
				// Fallback text if logo fails, positioned correctly
				drawText(contentStream, robotoBold, 10, "M", x + padding, y + stickerHeight - height - padding,
						TextAlignment.TA_LEFT);
			}
			// Position "Motorola Solutions" text relative to sticker box's top-left
			drawText(contentStream, robotoBold, 10, "Motorola Solutions India Pvt. Ltd.", x + padding + 25,
					y + stickerHeight - 15 - padding, TextAlignment.TA_LEFT);

			float lineY = y + stickerHeight - padding - 25; // Adjust this value to place the line where you want it
			contentStream.setStrokingColor(Color.BLACK); // Use a lighter color for internal lines
			contentStream.setLineWidth(1f); // Thinner line
			contentStream.moveTo(x, lineY); // Start from left padding of sticker
			contentStream.lineTo(x + stickerWidth, lineY); // End at right padding of sticker
			contentStream.stroke();

			// Adjust Y for content within this specific sticker
			float currentStickerContentY = y + stickerHeight - padding; // Start from top-padding inside sticker

			// --- Sticker Content (relative to currentStickerContentY and x) ---
			currentStickerContentY -= 40; // Space after logo/name
			drawText(contentStream, robotoBold, 9,
					"S.No#: " + (frudetails.getRepairingIdList().getSerialNo() != null
							? frudetails.getRepairingIdList().getSerialNo()
							: "N/A"),
					x + padding, currentStickerContentY, TextAlignment.TA_LEFT);

			currentStickerContentY += 0;
			drawText(contentStream, robotoRegular, 9, "Ticket No#: " + frudetails.getRepairingIdList().getId(),
					x + stickerWidth - padding, currentStickerContentY, TextAlignment.TA_RIGHT);

			currentStickerContentY -= 12;
			drawText(contentStream, robotoRegular, 9,
					"RMA No#: " + (frudetails.getRmaNo() != null ? frudetails.getRmaNo() : "N/A"), x + padding,
					currentStickerContentY, TextAlignment.TA_LEFT);
			currentStickerContentY -= 12;

			drawText(contentStream, robotoRegular, 9,
					"Customer#: " + (frudetails.getInGatepassID().getPartyName() != null
							? frudetails.getInGatepassID().getPartyName()
							: "N/A"),
					x + padding, currentStickerContentY, TextAlignment.TA_LEFT);
			currentStickerContentY -= 12;

			float addressTextHeight = drawWrappedText(contentStream, robotoRegular, 9,
					"Fault#: " + (frudetails.getRepairingIdList().getFaultDetails() != null
							? frudetails.getRepairingIdList().getFaultDetails()
							: "N/A"),
					x + padding, currentStickerContentY, 230, TextAlignment.TA_LEFT);
			currentStickerContentY -= 2 + addressTextHeight;

			drawText(contentStream, robotoRegular, 9,
					"Date#: " + frudetails.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/uuuu")),
					x + padding, currentStickerContentY, TextAlignment.TA_LEFT);

			currentStickerContentY -= 20;
			String faultDetails = frudetails.getRepairingIdList().getFaultDetails();
			if (faultDetails != null) {
				if (faultDetails.length() > 20) {
					faultDetails = faultDetails.substring(0, 19);
				}
			}
			// Barcode (using Code128)
			String barcodeData = String.format(
					"SerialNo#:%s| RmaNo#:%s| Customer#:%s| Fault#:%s| Date:%s| TicketNo#:%s",
					(frudetails.getRepairingIdList().getSerialNo() != null
							? frudetails.getRepairingIdList().getSerialNo()
							: "N/A"),
					(frudetails.getRmaNo() != null ? frudetails.getRmaNo() : "N/A"), // rma no
					(frudetails.getInGatepassID().getPartyName() != null ? frudetails.getInGatepassID().getPartyName()
							: "N/A"), // customer name
					(faultDetails != null ? faultDetails : "N/A"), // fault details
					(frudetails.getCreatedDate() != null ? frudetails.getCreatedDate() : "N/A"), // date
					(frudetails.getRepairingIdList().getId() != null ? frudetails.getRepairingIdList().getId()
							: "N/A")); // ticket id

			BufferedImage barcodeImage = generateBarcodeImage(barcodeData);
			if (barcodeImage != null) {
				PDImageXObject pdBarcodeImage = LosslessFactory.createFromImage(document, barcodeImage);
				float barcodeWidth = 240;
				float barcodeHeight = 60;
				float barcodeX = x + (stickerWidth - barcodeWidth) / 2;
				float barcodeY = y + padding; // Position near bottom of sticker, relative to sticker's y (bottom edge)
				contentStream.drawImage(pdBarcodeImage, barcodeX, barcodeY, barcodeWidth, barcodeHeight);
			} else {
				drawText(contentStream, robotoRegular, 8, "Barcode Error", x + stickerWidth / 2, y + padding + 20,
						TextAlignment.TA_CENTER);
			}

			stickerCount++;
		

		// --- FINAL CLOSE ---
		// Ensure the last content stream is closed after the loop finishes
		if (contentStream != null) {
			contentStream.close();
		}
	}

	// --- Helper Methods ---

	// Generic method to draw text with alignment
	private void drawText(PDPageContentStream contentStream, PDType0Font font, float fontSize, String text, float x,
			float y, TextAlignment alignment) throws IOException {
		contentStream.beginText();
		contentStream.setFont(font, fontSize);
		contentStream.setNonStrokingColor(Color.BLACK);
		// Border color for all cells
		contentStream.setLineWidth(1);
		float textWidth = font.getStringWidth(text) / 1000 * fontSize;

		float startX;
		if (alignment == TextAlignment.TA_LEFT) {
			startX = x;
		} else if (alignment == TextAlignment.TA_RIGHT) {
			startX = x - textWidth;
		} else { // CENTER
			startX = x - (textWidth / 2);
		}
		contentStream.newLineAtOffset(startX, y);
		contentStream.showText(text);
		contentStream.endText();
	}



	// Helper to split text into lines based on max width
	private List<String> splitTextIntoLines(PDType0Font font, float fontSize, String text, float maxWidth)
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
				if (currentLine.length() > 0) {
					currentLine.append(" ");
				}
				currentLine.append(word);
			} else {
				lines.add(currentLine.toString());
				currentLine = new StringBuilder(word);
			}
		}
		if (currentLine.length() > 0) {
			lines.add(currentLine.toString());
		}
		return lines;
	}

	// Helper to get text height
	private float getTextHeight(PDType0Font font, float fontSize) {
		return font.getFontDescriptor().getCapHeight() / 1000 * fontSize;
	}


	private BufferedImage generateBarcodeImage(String data) {
		int qrCodeSize = 150; // Initial size (width and height). Adjust for sticker.

		try {
			// Set QR code hints (optional, but good for quality)
			Hashtable<EncodeHintType, ErrorCorrectionLevel> hints = new Hashtable<>();
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // High error correction

			BitMatrix bitMatrix = new MultiFormatWriter().encode(data, // The data to encode in the QR code
					BarcodeFormat.QR_CODE, // Specify QR_CODE format
					qrCodeSize, // Width
					qrCodeSize, // Height
					hints // Encoding hints
			);

			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

			int finalQrImageSizePx = 900; // Generate a higher resolution image
			if (qrCodeSize != finalQrImageSizePx) {
				bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, finalQrImageSizePx,
						finalQrImageSizePx, hints);
				qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
			}

			// Return the generated BufferedImage
			return qrImage;

		} catch (Exception e) { // Catch generic Exception from ZXing as it can throw WriterException
			System.err.println("Error generating QR code image: " + e.getMessage());
			return null;
		}
	}

	private float drawWrappedText(PDPageContentStream contentStream, PDType0Font font, float fontSize, String text,
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

	// capitalize first letter
	public static String capitalizeFirstLetter(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
