package com.serverManagement.server.management.service.itemRepairOption;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.itemRepairOption.RepairingOptionDAO;
import com.serverManagement.server.management.dao.itemRepairOption.TechnicianStatusDAO;
import com.serverManagement.server.management.dao.itemRepairOption.WarrantyOptionDAO;

@Service
public class RepairingOptionService {
	
	@Autowired
	private RepairingOptionDAO repairingOptionDAO;
	@Autowired
	private WarrantyOptionDAO warrantyOptionDAO;
	@Autowired
	private TechnicianStatusDAO technicianStatusDAO;
	
	
	
	public ResponseEntity<?> getAllRepairingOption(){
		try {
		List<String> repairOptionList=repairingOptionDAO.getAllRepairingOption();
		return ResponseEntity.status(HttpStatus.OK).body(repairOptionList);
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	
	public ResponseEntity<?> getAllWarrantyOption(){
		try {
		List<String> repairWarrantyList=warrantyOptionDAO.getAllWarrantyOption();
		return ResponseEntity.status(HttpStatus.OK).body(repairWarrantyList);
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	public ResponseEntity<?> getAllTechnicianStatusOption() {
		try {
			List<String> repairWarrantyList=technicianStatusDAO. getAllTechnicianStatusOption();
			return ResponseEntity.status(HttpStatus.OK).body(repairWarrantyList);
			}catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
	}

}
