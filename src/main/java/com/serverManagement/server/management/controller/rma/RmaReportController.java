package com.serverManagement.server.management.controller.rma;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.service.rma.RmaReportService;

@RestController
@RequestMapping("/api/rma/reports")
public class RmaReportController {

    @Autowired
    private RmaReportService rmaReportService;

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Generate RMA Report by Creator
     * GET /api/rma/reports/by-creator?startDate=...&endDate=...
     */
    @GetMapping("/by-creator")
    public ResponseEntity<byte[]> getReportByCreator(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            ZonedDateTime start = parseDate(startDate);
            ZonedDateTime end = parseDate(endDate);

            byte[] pdfBytes = rmaReportService.generateByCreatorReport(start, end);
            return buildPdfResponse(pdfBytes, "rma_report_by_creator.pdf");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate RMA Report by Customer
     * GET /api/rma/reports/by-customer?startDate=...&endDate=...
     */
    @GetMapping("/by-customer")
    public ResponseEntity<byte[]> getReportByCustomer(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            ZonedDateTime start = parseDate(startDate);
            ZonedDateTime end = parseDate(endDate);

            byte[] pdfBytes = rmaReportService.generateByCustomerReport(start, end);
            return buildPdfResponse(pdfBytes, "rma_report_by_customer.pdf");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate RMA Report by Status
     * GET /api/rma/reports/by-status?startDate=...&endDate=...
     */
    @GetMapping("/by-status")
    public ResponseEntity<byte[]> getReportByStatus(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            ZonedDateTime start = parseDate(startDate);
            ZonedDateTime end = parseDate(endDate);

            byte[] pdfBytes = rmaReportService.generateByStatusReport(start, end);
            return buildPdfResponse(pdfBytes, "rma_report_by_status.pdf");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate RMA Report by Repair Type (LOCAL vs DEPOT)
     * GET /api/rma/reports/by-repair-type?startDate=...&endDate=...
     */
    @GetMapping("/by-repair-type")
    public ResponseEntity<byte[]> getReportByRepairType(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            ZonedDateTime start = parseDate(startDate);
            ZonedDateTime end = parseDate(endDate);

            byte[] pdfBytes = rmaReportService.generateByRepairTypeReport(start, end);
            return buildPdfResponse(pdfBytes, "rma_report_by_repair_type.pdf");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate RMA Report by Technician
     * GET /api/rma/reports/by-technician?startDate=...&endDate=...
     */
    @GetMapping("/by-technician")
    public ResponseEntity<byte[]> getReportByTechnician(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            ZonedDateTime start = parseDate(startDate);
            ZonedDateTime end = parseDate(endDate);

            byte[] pdfBytes = rmaReportService.generateByTechnicianReport(start, end);
            return buildPdfResponse(pdfBytes, "rma_report_by_technician.pdf");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate TAT Compliance Report
     * GET /api/rma/reports/tat-compliance?startDate=...&endDate=...
     */
    @GetMapping("/tat-compliance")
    public ResponseEntity<byte[]> getTatComplianceReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            ZonedDateTime start = parseDate(startDate);
            ZonedDateTime end = parseDate(endDate);

            byte[] pdfBytes = rmaReportService.generateTatComplianceReport(start, end);
            return buildPdfResponse(pdfBytes, "rma_tat_compliance_report.pdf");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate Monthly Summary Report
     * GET /api/rma/reports/monthly-summary?startDate=...&endDate=...
     */
    @GetMapping("/monthly-summary")
    public ResponseEntity<byte[]> getMonthlySummaryReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            ZonedDateTime start = parseDate(startDate);
            ZonedDateTime end = parseDate(endDate);

            byte[] pdfBytes = rmaReportService.generateMonthlySummaryReport(start, end);
            return buildPdfResponse(pdfBytes, "rma_monthly_summary_report.pdf");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Helper Methods ====================

    private ZonedDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(dateStr, ISO_DATE_FORMATTER);
        } catch (Exception e) {
            // Try parsing as ISO date without time
            try {
                return ZonedDateTime.parse(dateStr + "T00:00:00Z");
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdfBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
