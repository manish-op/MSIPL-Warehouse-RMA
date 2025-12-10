package com.serverManagement.server.management.controller.itemDetails;

import com.serverManagement.server.management.entity.itemDetails.RegionUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.request.itemDetails.AddItemRequest;
import com.serverManagement.server.management.request.itemDetails.AssignItemDetailsRequest;
import com.serverManagement.server.management.request.itemDetails.GetItemByKeywordRequest;
import com.serverManagement.server.management.service.itemDetails.ItemDetailsService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/componentDetails")
public class ItemDetailsController {

	@Autowired
	private ItemDetailsService itemDetailsService;
	
	
	@PostMapping("/add")
	public ResponseEntity<?> addComponent(HttpServletRequest request, @RequestBody AddItemRequest addComponent){
		
		return itemDetailsService.addComponent(request, addComponent);
	}
	
	@PutMapping("/update/item")
	public ResponseEntity<?> issueComponent(HttpServletRequest request, @RequestBody AssignItemDetailsRequest assignItemDetails){
		
		return itemDetailsService.issueItems(request, assignItemDetails);
	}
	
	@PostMapping("/serialno")
	public ResponseEntity<?> getItemDetailsBySerialNo(HttpServletRequest request, @RequestBody String serialNo){
		
		return itemDetailsService.getItemDetailsBySerialNo(request, serialNo);
	}
	
	@PostMapping("/keyword")
	public ResponseEntity<?> getItemDetailsByKeyword(HttpServletRequest request, @RequestBody GetItemByKeywordRequest requestDetails){
		
		return itemDetailsService.getItemDetailsByKeyword(request, requestDetails);
	}

    @PutMapping("/update-region-only")
    public ResponseEntity<?> updateRegionOnly(HttpServletRequest request, @RequestBody RegionUpdateDTO regionDto) {
        // Call the service method we just created
        return itemDetailsService.updateItemRegionOnly(request, regionDto);
    }

}
