package com.serverManagement.server.management.controller.itemRepairingDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.request.itemRepairingDetails.AssignTechnicianRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.EngineerUpdateTicketStatusRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.FruTicketRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.GetAssignTicketDetailsRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.ItemRepairingRequest;
import com.serverManagement.server.management.request.itemRepairingDetails.ManagerGetTicketRequest;
import com.serverManagement.server.management.service.itemRepairingDetails.ItemRepairingService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/item_details/repairing")
public class ItemRepairingController {
	
	@Autowired
	private ItemRepairingService itemRepairingService;

	
	//assign technician for particular ticket
	@PostMapping("/ticket/assignEngineer/forTicket")
	public ResponseEntity<?> assignTechnician(HttpServletRequest httpRequest,@RequestBody AssignTechnicianRequest repairingRequest)throws Exception{
			
			
			return itemRepairingService.assignTechnician(httpRequest, repairingRequest);
			
		}
//	
//	
	//update ticket details
	@PostMapping("/ticket/update/forEngineer")
	public ResponseEntity<?> updateAssignTicketDetailsViaId(HttpServletRequest httpRequest,@RequestBody EngineerUpdateTicketStatusRequest updateDetails )throws Exception{
			
			//call service class for generate request
		return itemRepairingService.updateAssignTicketDetailsViaId(httpRequest, updateDetails);
		}
	
	
	
	// get ticket via date by default show all requests
	@PostMapping("/dynamic/repair/list")
	public ResponseEntity<?> getRepairingTicket(HttpServletRequest httpRequest, @RequestBody ManagerGetTicketRequest tickedDetailsRequest)throws Exception{
			
			//call service class for generate request
			return itemRepairingService.getRepairingTicket(httpRequest,tickedDetailsRequest);
		}
	
	@PostMapping("/ticket/details/byId")
	public ResponseEntity<?> getTicketDetailsViaId(HttpServletRequest httpRequest, @RequestBody Long ticketId)throws Exception{
			
			//call service class for generate request
			return itemRepairingService.getTicketDetailsViaId(httpRequest,ticketId);
		}
	
	
	@PostMapping("/assign/ticket/details/byId")
	public ResponseEntity<?> getAssignTicketDetailsViaId(HttpServletRequest httpRequest, @RequestBody Long ticketId)throws Exception{
			
			//call service class for generate request
			return itemRepairingService.getAssignTicketDetailsViaId(httpRequest,ticketId);
		}
	
//	// get assign ticket to technician on date basis by default show all
	@PostMapping("get/assign/ticketList")
	public ResponseEntity<?>  getRepairingRequest(HttpServletRequest request, @RequestBody GetAssignTicketDetailsRequest ticketDetailsRequest)throws Exception{
			
			return  itemRepairingService.getRepairingRequest(request,ticketDetailsRequest);
		}
//	
//	
//	//get list of assign ticket by starting and end date and also ticket status resolve or pending
//	@GetMapping
//	public ResponseEntity<?> getRepairingRequest(HttpServletRequest httpRequest)throws Exception{
//			
//			//call service class for generate request
//			return null;
//		}
	
}
