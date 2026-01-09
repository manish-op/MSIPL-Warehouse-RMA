package com.serverManagement.server.management.controller.employeeList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.service.employeeList.GetEmployeeListService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class GetEmployeeListController {

    @Autowired
    private GetEmployeeListService getEmployeeListService;

    @GetMapping("/api/all-users")
    public ResponseEntity<?> getEmployeeList(HttpServletRequest httpRequest,
            @RequestParam(name = "regionName", required = false) String regionName) {
        return getEmployeeListService.getEmployeeList(httpRequest, regionName);
    }
}
