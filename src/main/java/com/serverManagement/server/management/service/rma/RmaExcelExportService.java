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
 * Service for exporting RMA data to Excel using the original template
 */
@Service
public class RmaExcelExportService {

    private static final String TEMPLATE_PATH = "templates/RMA_Request_Template.xlsx";

    public ByteArrayResource exportToExcel(Map<String, Object> formData, List<Map<String, Object>> items)
            throws Exception {

        System.out.println("=== RMA EXCEL EXPORT DEBUG ===");
        System.out.println("Items received: " + items.size());
        if (!items.isEmpty()) {
            System.out.println("First item: " + items.get(0));
        }

        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);

        try (InputStream templateStream = resource.getInputStream();
                Workbook workbook = new XSSFWorkbook(templateStream);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Find the India Repair Request Form sheet
            Sheet sheet = null;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String name = workbook.getSheetName(i).toLowerCase();
                if (name.contains("india") || name.contains("repair") || name.contains("form")) {
                    sheet = workbook.getSheetAt(i);
                    System.out.println("Using sheet: " + workbook.getSheetName(i));
                    break;
                }
            }
            if (sheet == null && workbook.getNumberOfSheets() > 1) {
                sheet = workbook.getSheetAt(1); // Try second sheet
            }
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            // Based on the actual template screenshot:
            // Row 4 (0-indexed): MODE OF TRANSPORT section
            // Col E (4): "Motorola Courier Service" etc - value for mode of transport
            // Col K area: DPL LICENSE: | value | DATE: | 2025-12-09

            // Row 5: RETURNING SHIPMENT SHIPPING METHOD...
            // Row 6: RETURNING SHIPMENT COURIER COMPANY NAME...

            // Row 8: RETURN ADDRESS DETAILS header
            // Row 9: COMPANY NAME * | value | EMAIL ADDRESS * | value
            // Row 10: CONTACT NAME * | value | TELEPHONE NUMBER * | value | MOBILE NUMBER *
            // | value
            // Row 11: RETURN ADDRESS | value (spans multiple columns)

            // Row 12: INVOICE ADDRESS DETAILS header
            // Row 13: Same structure as return address
            // Row 14: Same structure
            // Row 15: INVOICE ADDRESS

            // Row 19: FAULT DETAILS header
            // Row 20: Column headers: ITEM NO | PRODUCT* | MODEL NO./PART NO.* | SERIAL
            // NO.* | RMA NO. | FAULT DESC | CODEPLUG | FLASH CODE | STATUS | INVOICE NO |
            // DATE CODE | FM/UL/ATEX | ENCRYPTION | FIRMWARE | LOWER FIRMWARE | REMARKS
            // Row 21+: Data rows

            // Fill header fields
            // Mode of Transport - Row 4, Column D-E area
            setCellValue(sheet, 6, 3, getString(formData, "modeOfTransport"));

            // DPL LICENSE and DATE - Row 4, right side
            setCellValue(sheet, 6, 14, getString(formData, "dplLicense"));
            setCellValue(sheet, 7, 14, getString(formData, "date"));

            // Shipping Method - Row 5
            setCellValue(sheet, 6, 3, getString(formData, "shippingMethod"));

            // Courier Company - Row 6
            setCellValue(sheet, 7, 8, getString(formData, "courierCompanyName"));

            // RETURN ADDRESS DETAILS (Row 9-11)
            setCellValue(sheet, 11, 3, getString(formData, "companyName"));
            setCellValue(sheet, 11, 7, getString(formData, "email"));
            setCellValue(sheet, 12, 3, getString(formData, "contactName"));
            setCellValue(sheet, 12, 7, getString(formData, "telephone"));
            setCellValue(sheet, 12, 10, getString(formData, "mobile"));
            setCellValue(sheet, 13, 3, getString(formData, "returnAddress"));

            // INVOICE ADDRESS DETAILS (Row 13-15)
            setCellValue(sheet, 13, 1, getString(formData, "invoiceCompanyName"));
            setCellValue(sheet, 13, 4, getString(formData, "invoiceEmail"));
            setCellValue(sheet, 14, 1, getString(formData, "invoiceContactName"));
            setCellValue(sheet, 14, 4, getString(formData, "invoiceTelephone"));
            setCellValue(sheet, 14, 7, getString(formData, "invoiceMobile"));
            setCellValue(sheet, 15, 1, getString(formData, "invoiceAddress"));

            // FAULT DETAILS - Items start from Row 21 (0-indexed = 20, after header row 20)
            int itemStartRow = 21;

            // If there are more than 1 item, shift existing rows down to make room
            // Row 22 onwards contains PARTIAL SHIPMENT, Terms & Conditions, etc.
            int additionalItemsCount = items.size() - 1;
            if (additionalItemsCount > 0) {
                // Shift rows starting from row 22 (after first item row) down by the number of
                // additional items
                int shiftStartRow = itemStartRow + 1; // Row 22
                int lastRowNum = sheet.getLastRowNum();
                sheet.shiftRows(shiftStartRow, lastRowNum, additionalItemsCount);
                System.out.println(
                        "Shifted rows " + shiftStartRow + " to " + lastRowNum + " down by " + additionalItemsCount);
            }

            // Get the template row's height for consistent sizing
            Row templateRow = sheet.getRow(itemStartRow);
            short templateRowHeight = templateRow != null ? templateRow.getHeight() : 400; // Default 20pt

            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> item = items.get(i);
                int rowNum = itemStartRow + i;

                // Get or create row
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    row = sheet.createRow(rowNum);
                }

                // Set consistent row height for all item rows
                row.setHeight(templateRowHeight);

                // Columns based on screenshot header row:
                // A=ITEM NO, B=PRODUCT, C=MODEL NO./PART NO., D=SERIAL NO., E=RMA NO.,
                // F=FAULT DESCRIPTION, G=CODEPLUG, H=FLASH CODE, I=STATUS, J=INVOICE NO,
                // K=DATE CODE, L=FM/UL/ATEX, M=ENCRYPTION, N=FIRMWARE, O=LOWER FIRMWARE,
                // P=REMARKS

                setCellValue(sheet, rowNum, 0, String.valueOf(i + 1)); // Col A: ITEM NO
                setCellValue(sheet, rowNum, 1, getString(item, "product")); // Col B: PRODUCT
                setCellValue(sheet, rowNum, 2, getString(item, "model")); // Col C: MODEL NO./PART NO.
                setCellValue(sheet, rowNum, 3, getString(item, "serialNo")); // Col D: SERIAL NO.
                setCellValue(sheet, rowNum, 4, getString(item, "rmaNo")); // Col E: RMA NO.
                setCellValue(sheet, rowNum, 5, getString(item, "faultDescription")); // Col F: FAULT DESCRIPTION
                setCellValue(sheet, rowNum, 6, getString(item, "codeplug")); // Col G: CODEPLUG
                setCellValue(sheet, rowNum, 7, getString(item, "flashCode")); // Col H: FLASH CODE
                setCellValue(sheet, rowNum, 8, getString(item, "repairStatus")); // Col I: STATUS
                setCellValue(sheet, rowNum, 9, getString(item, "invoiceNo")); // Col J: INVOICE NO
                setCellValue(sheet, rowNum, 10, getString(item, "dateCode")); // Col K: DATE CODE
                setCellValue(sheet, rowNum, 11, getString(item, "fmUlatex")); // Col L: FM/UL/ATEX
                setCellValue(sheet, rowNum, 12, getString(item, "encryption")); // Col M: ENCRYPTION
                setCellValue(sheet, rowNum, 13, getString(item, "firmwareVersion")); // Col N: FIRMWARE
                setCellValue(sheet, rowNum, 14, getString(item, "lowerFirmwareVersion")); // Col O: LOWER FIRMWARE
                setCellValue(sheet, rowNum, 15, getString(item, "remarks")); // Col P: REMARKS

                System.out.println("Filled item row " + rowNum + ": " + getString(item, "product") + " / "
                        + getString(item, "serialNo"));
            }

            workbook.setForceFormulaRecalculation(true);
            workbook.write(outputStream);
            System.out.println("Excel export completed successfully");

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

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
}
