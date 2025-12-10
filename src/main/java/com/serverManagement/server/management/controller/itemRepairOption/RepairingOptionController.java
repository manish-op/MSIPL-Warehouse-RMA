package com.serverManagement.server.management.controller.itemRepairOption;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.service.itemRepairOption.RepairingOptionService;

@RestController
@RequestMapping("/api/repairing/option")
public class RepairingOptionController {
	
	@Autowired
	private RepairingOptionService repairingOptionService;
	
	@GetMapping("/item/repairing/status/option")
	public ResponseEntity<?> getAllRepairingOption(){
		return repairingOptionService.getAllRepairingOption();
	}
	
	
	@GetMapping("/warranty/status/option")
	public ResponseEntity<?> getAllWarrantyOption(){
		return repairingOptionService.getAllWarrantyOption();
	}
	
	@GetMapping("/technician/status")
	public ResponseEntity<?> getAllTechnicianStatusOption(){
		return repairingOptionService.getAllTechnicianStatusOption();
	}

}
