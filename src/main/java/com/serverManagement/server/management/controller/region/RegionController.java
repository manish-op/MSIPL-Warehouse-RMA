package com.serverManagement.server.management.controller.region;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.request.region.UpdateEmployeeRegionRequest;
import com.serverManagement.server.management.request.region.UpdateRegionRequest;
import com.serverManagement.server.management.service.region.RegionService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/region")
public class RegionController {

	@Autowired
	private RegionService regionService;
	
	@GetMapping("/getList")
	public ResponseEntity<?> getRegionList(){
		
		return regionService.getRegionList();
	}
	
	@PostMapping
	public ResponseEntity<?> addRegion(HttpServletRequest request, @RequestBody String region){
		
		return regionService.addRegion(request, region);
	}
	
	@PutMapping
	public ResponseEntity<?> updateRegion(HttpServletRequest request, @RequestBody UpdateRegionRequest region){
	
		return regionService.updateRegion(request, region);
	}
	
	@PutMapping("/update/employeeRegion")
	public ResponseEntity<?> updateEmployeeRegion(HttpServletRequest request, @RequestBody UpdateEmployeeRegionRequest updateEmployeeRegion){
		
		return regionService.updateEmployeeRegion(request, updateEmployeeRegion);
	}
	
//	@DeleteMapping
//	public ResponseEntity<?> deleteRegion(HttpServletRequest request, @RequestBody String region){
//		return regionService.deleteRegion(request, region);
//	}
}
