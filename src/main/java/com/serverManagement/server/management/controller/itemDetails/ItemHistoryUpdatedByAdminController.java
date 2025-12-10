package com.serverManagement.server.management.controller.itemDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.service.itemDetails.ItemHistoryUpdatedByAdminService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/componentDetails/history")
public class ItemHistoryUpdatedByAdminController {

	@Autowired
	private ItemHistoryUpdatedByAdminService itemHistoryDetailsService;
	
	
	@PostMapping()
	public ResponseEntity<?> getItemHistory(HttpServletRequest request, @RequestBody String serialNo){
		
		return itemHistoryDetailsService.getHistory(request, serialNo);
	}

}
