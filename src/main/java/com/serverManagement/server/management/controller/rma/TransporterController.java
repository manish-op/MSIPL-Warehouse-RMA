package com.serverManagement.server.management.controller.rma;

import com.serverManagement.server.management.dao.rma.TransporterDAO;
import com.serverManagement.server.management.entity.rma.Transporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transporters")
@CrossOrigin(origins = "*")
public class TransporterController {

    @Autowired
    private TransporterDAO transporterDAO;

    @GetMapping
    public List<Transporter> getAllTransporters() {
        return transporterDAO.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createTransporter(@RequestBody Transporter transporter) {
        if (transporterDAO.findByName(transporter.getName()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Transporter name already exists"));
        }
        return ResponseEntity.ok(transporterDAO.save(transporter));
    }
}
