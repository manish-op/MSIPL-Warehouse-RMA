package com.serverManagement.server.management.controller.option;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.request.option.UpdateItemStatusOtionRequest;
import com.serverManagement.server.management.service.option.ItemStatusOptionService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/option/item-status")
public class ItemStatusOptionController {

	@Autowired
	private ItemStatusOptionService itemStatusOptionService;
	
	@GetMapping()
	public ResponseEntity<?> getItemStatusOptions(){
		
		return itemStatusOptionService.getItemStatusOptions();
	}
	
	@PostMapping()
	public ResponseEntity<?> addItemStatusOptions(HttpServletRequest request, @RequestBody String optionName){
		
		return itemStatusOptionService.addItemStatusOptions(request, optionName);
	}
	
	@PutMapping()
	public ResponseEntity<?> updateItemStatusOptions(HttpServletRequest request, @RequestBody UpdateItemStatusOtionRequest updateStatusOption){
		
		return itemStatusOptionService.updateItemStatusOptions(request, updateStatusOption);
	}
}
