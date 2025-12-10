package com.serverManagement.server.management.controller.option;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.request.option.UpdateAvailableStatusRequest;
import com.serverManagement.server.management.service.option.ItemAvailableStatusOptionService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/option/item-availability")
public class ItemAvailableStatusOptionController {

	@Autowired
	private ItemAvailableStatusOptionService itemAvailableStatusOptionService;
	
	@GetMapping()
	public ResponseEntity<?> getAvailableOptions(){
		
		return itemAvailableStatusOptionService.getAvailableOptions();
	}

	@PostMapping()
	public ResponseEntity<?> addAvailableOptions(HttpServletRequest request, @RequestBody String optionName){
		
		return itemAvailableStatusOptionService.addAvailableOptions(request, optionName);
	}
	
	@PutMapping()
	public ResponseEntity<?> updateAvailableOptions(HttpServletRequest request, @RequestBody UpdateAvailableStatusRequest updateOptionRequest){
		
		return itemAvailableStatusOptionService.updateAvailableOptions(request, updateOptionRequest);
	}
}
