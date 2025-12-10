package com.serverManagement.server.management.controller.itemDetails;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.serverManagement.server.management.service.itemDetails.CSVConverterService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/itemList/CSVFile")
public class UploadViaCsvFileController {


    private CSVConverterService csvConverterService;

    public UploadViaCsvFileController(CSVConverterService csvConverterService) {
        super();
        this.csvConverterService = csvConverterService;
    }


    // This method for uploading is fine as a POST
    @PostMapping
    public ResponseEntity<?> addItemViaCSV(HttpServletRequest request, @RequestParam("file")
    MultipartFile csvFile) throws IOException, Exception {

        return csvConverterService.addItemsViaCSV(request, csvFile);
    }


    /**
     * Handles downloading the CSV file.
     * 1. Changed to @GetMapping - this is the standard for downloading/retrieving data.
     * 2. Changed @RequestBody to @RequestParam(required = false) - this allows the
     * region to be optional and passed in the URL (e.g., /export?region=NewYork)
     */


    @GetMapping("/export")
    public ResponseEntity<?> getRegionItemCsv(
            HttpServletRequest request,
            @RequestParam(name = "region", required = false) String regionName
    ) throws IOException, Exception {

        // Your service file already handles the logic for when regionName is null
        return csvConverterService.getRegionItemCsv(request, regionName);
    }

}