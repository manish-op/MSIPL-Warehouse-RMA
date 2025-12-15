package com.serverManagement.server.management.service.rma;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service for exporting RMA data to Excel using the template
 * Cell positions mapped from RMA_Request_Template.xlsx (Sheet: India Repair
 * Request Form)
 */
@Service
public class RmaExcelExportService {

    private static final String TEMPLATE_PATH = "templates/RMA_Request_Template.xlsx";

    public ByteArrayResource exportToExcel(Map<String, Object> formData, List<Map<String, Object>> items)
            throws Exception {

        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);

        try (InputStream templateStream = resource.getInputStream();
                Workbook workbook = new XSSFWorkbook(templateStream);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Get the India Repair Request Form sheet
            Sheet sheet = workbook.getSheet("India Repair Request Form");
            if (sheet == null) {
                throw new IllegalStateException("Sheet 'India Repair Request Form' not found in template");
            }

            // ===== Transport block (Rows 4-8, 0-indexed) =====
            // Labels are in column A, values go in column G (index 6)
            setCellValue(sheet, 4, 6, getString(formData, "modeOfTransport")); // MODE OF TRANSPORT
            setCellValue(sheet, 5, 6, getString(formData, "shippingMethod")); // SHIPPING METHOD
            setCellValue(sheet, 6, 6, getString(formData, "courierCompanyName")); // COURIER COMPANY NAME
            setCellValue(sheet, 7, 6, getString(formData, "dplLicense")); // DPL LICENSE
            setCellValue(sheet, 8, 6, getString(formData, "date")); // DATE

            // ===== Return Address block (Rows 11-13, 0-indexed) =====
            // Row 11: COMPANY NAME (A) -> value in C(2), EMAIL (F) -> value in G(6)
            // Row 12: CONTACT NAME (A) -> value in C(2), TELEPHONE (F) -> value in G(6),
            // MOBILE (H) -> value in J(9)
            // Row 13: RETURN ADDRESS (A) -> value in C(2)
            setCellValue(sheet, 11, 2, getString(formData, "companyName")); // Col C
            setCellValue(sheet, 11, 6, getString(formData, "email")); // Col G
            setCellValue(sheet, 12, 2, getString(formData, "contactName")); // Col C
            setCellValue(sheet, 12, 6, getString(formData, "telephone")); // Col G
            setCellValue(sheet, 12, 9, getString(formData, "mobile")); // Col J
            setCellValue(sheet, 13, 2, getString(formData, "returnAddress")); // Col C

            // ===== Invoice Address block (Rows 15-17, 0-indexed) =====
            setCellValue(sheet, 15, 2, getString(formData, "invoiceCompanyName")); // Col C
            setCellValue(sheet, 15, 6, getString(formData, "invoiceEmail")); // Col G
            setCellValue(sheet, 16, 2, getString(formData, "invoiceContactName")); // Col C
            setCellValue(sheet, 16, 6, getString(formData, "invoiceTelephone")); // Col G
            setCellValue(sheet, 16, 9, getString(formData, "invoiceMobile")); // Col J
            setCellValue(sheet, 17, 2, getString(formData, "invoiceAddress")); // Col C

            // ===== Fault Details Items (Starting Row 21, 0-indexed) =====
            // Row 20 has column headers, data starts at row 21
            // Columns: A(0)=ITEM NO, B(1)=PRODUCT, C(2)=MODEL, D(3)=SERIAL, E(4)=RMA,
            // F(5)=FAULT, G(6)=CODEPLUG, H(7)=FLASH, I(8)=STATUS, J(9)=INVOICE,
            // K(10)=DATE CODE, L(11)=FM/UL, M(12)=ENCRYPTION, N(13)=FIRMWARE,
            // O(14)=LOWER FIRMWARE, P(15)=REMARKS
            int itemStartRow = 22; // Row 22 (0-indexed) = Excel row 23

            // If there are more than 1 item, shift rows down to make room
            // The template has 1 item row by default. Additional items need space.
            // Row 23 onwards has PARTIAL SHIPMENT, Terms & Conditions, etc.
            int additionalItemsCount = items.size() - 1;
            if (additionalItemsCount > 0) {
                int shiftStartRow = itemStartRow + 1; // Start shifting from row after first item
                int lastRowNum = sheet.getLastRowNum();
                if (lastRowNum >= shiftStartRow) {
                    sheet.shiftRows(shiftStartRow, lastRowNum, additionalItemsCount);
                }
            }

            // Set a consistent shorter row height for all item rows
            // 300 = ~15pt, 400 = ~20pt, 500 = ~25pt (Excel uses 1/20th of a point)
            short itemRowHeight = 300;

            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> item = items.get(i);
                int rowNum = itemStartRow + i;

                // Get or create row
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    row = sheet.createRow(rowNum);
                }

                // Set consistent row height for all item rows
                row.setHeight(itemRowHeight);

                // Get cell style from the first item row template for consistent font
                Row firstItemRow = sheet.getRow(itemStartRow);
                CellStyle templateStyle = null;
                if (firstItemRow != null && firstItemRow.getCell(1) != null) {
                    templateStyle = firstItemRow.getCell(1).getCellStyle();
                }

                // Write each cell value with template style
                setCellValueWithStyle(row, 0, String.valueOf(i + 1), templateStyle); // Col A: ITEM NO.
                setCellValueWithStyle(row, 1, getString(item, "product"), templateStyle); // Col B: PRODUCT *
                setCellValueWithStyle(row, 2, getString(item, "model"), templateStyle); // Col C: MODEL NO./PART NO. *
                setCellValueWithStyle(row, 3, getString(item, "serialNo"), templateStyle); // Col D: SERIAL NO. *
                setCellValueWithStyle(row, 4, getString(item, "rmaNo"), templateStyle); // Col E: RMA NO.
                setCellValueWithStyle(row, 5, getString(item, "faultDescription"), templateStyle); // Col F: FAULT
                                                                                                   // DESCRIPTION *
                setCellValueWithStyle(row, 6, getString(item, "codeplug"), templateStyle); // Col G: CODEPLUG
                                                                                           // PROGRAMMING
                setCellValueWithStyle(row, 7, getString(item, "flashCode"), templateStyle); // Col H: FLASH CODE
                setCellValueWithStyle(row, 8, getString(item, "repairStatus"), templateStyle); // Col I: STATUS
                setCellValueWithStyle(row, 9, getString(item, "invoiceNo"), templateStyle); // Col J: INVOICE NO.
                setCellValueWithStyle(row, 10, getString(item, "dateCode"), templateStyle); // Col K: DATE CODE
                setCellValueWithStyle(row, 11, getString(item, "fmUlatex"), templateStyle); // Col L: FM/UL/ATEX
                setCellValueWithStyle(row, 12, getString(item, "encryption"), templateStyle); // Col M: ENCRYPTION
                setCellValueWithStyle(row, 13, getString(item, "firmwareVersion"), templateStyle); // Col N: FIRMWARE
                                                                                                   // VERSION
                setCellValueWithStyle(row, 14, getString(item, "lowerFirmwareVersion"), templateStyle); // Col O: LOWER
                                                                                                        // FIRMWARE
                setCellValueWithStyle(row, 15, getString(item, "remarks"), templateStyle); // Col P: REMARKS
            }

            // ===== Signature =====
            // Original signature row is 36 (0-indexed), but shifts down when additional
            // items are added
            int signatureRow = 35 + additionalItemsCount;
            setCellValue(sheet, signatureRow, 3, getString(formData, "signature")); // Col D

            workbook.setForceFormulaRecalculation(true);
            workbook.write(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    private void setCellValue(Sheet sheet, int rowNum, int colNum, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        Row row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }

        Cell cell = row.getCell(colNum);
        if (cell == null) {
            cell = row.createCell(colNum, CellType.STRING);
        }

        cell.setCellValue(value);
    }

    // Direct write to row - always creates/updates cell even if value is empty
    private void setCellValueDirect(Row row, int colNum, String value) {
        Cell cell = row.getCell(colNum);
        if (cell == null) {
            cell = row.createCell(colNum, CellType.STRING);
        }
        cell.setCellValue(value != null ? value : "");
    }

    // Write cell value with template style for consistent font formatting
    private void setCellValueWithStyle(Row row, int colNum, String value, CellStyle style) {
        Cell cell = row.getCell(colNum);
        if (cell == null) {
            cell = row.createCell(colNum, CellType.STRING);
        }
        cell.setCellValue(value != null ? value : "");
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
}
