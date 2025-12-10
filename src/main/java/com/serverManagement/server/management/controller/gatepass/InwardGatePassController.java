package com.serverManagement.server.management.controller.gatepass;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.request.gatepass.AfterGatePassFruMakingRequest;
import com.serverManagement.server.management.request.gatepass.InwardGatepassItemList;
import com.serverManagement.server.management.request.gatepass.InwardGatepassRequest;
import com.serverManagement.server.management.request.gatepass.OutwardGatepassRequest;
import com.serverManagement.server.management.service.gatepass.FruMakingAfterGatePassService;
import com.serverManagement.server.management.service.gatepass.InwardGatePassService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/gatepass")
public class InwardGatePassController {

	private InwardGatePassService inwardGatePassService;
	private FruMakingAfterGatePassService fruMakingAfterGatePassService;

	public InwardGatePassController(InwardGatePassService inwardGatePassService,
			FruMakingAfterGatePassService fruMakingAfterGatePassService) {
		super();
		this.inwardGatePassService = inwardGatePassService;
		this.fruMakingAfterGatePassService = fruMakingAfterGatePassService;
	}

	@PostMapping("/inwardGatepass")
	public ResponseEntity<?> generateInwardGatePass(HttpServletRequest httpRequest, @RequestBody InwardGatepassRequest passRequest) {
		
		return inwardGatePassService.generateInwardGatePass(httpRequest, passRequest);
	}
	
	@PostMapping("/outwardGatepass")
	public ResponseEntity<?> generatedOutwardGatepass(HttpServletRequest httpRequest, @RequestBody OutwardGatepassRequest outPassRequest){

		return inwardGatePassService.generatedOutwardGatepass(httpRequest, outPassRequest);
	}
	
	
	@PostMapping("/generate/ticket/after/inwardpass")
	public ResponseEntity<?> createTicketAfterGatepass(HttpServletRequest httpRequest, @RequestBody AfterGatePassFruMakingRequest fruRequest){
		return fruMakingAfterGatePassService.createTicketAfterGatepass(httpRequest,  fruRequest);
	}
	
	@PostMapping("/pdf")
	public ResponseEntity<?> testPdf(@RequestParam Long id) throws Exception{

		return inwardGatePassService.testPdf(id);
	}
}
