package com.serverManagement.server.management.controller.rma.common;

import com.serverManagement.server.management.dao.rma.common.TransporterDAO;
import com.serverManagement.server.management.entity.rma.common.TransporterEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transporters")
public class TransporterController {

    @Autowired
    private TransporterDAO transporterDAO;

    @GetMapping
    public List<TransporterEntity> getAllTransporters() {
        return transporterDAO.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createTransporter(@RequestBody TransporterEntity transporter) {
        if (transporterDAO.existsByName(transporter.getName())) {
            return ResponseEntity.badRequest().body("Transporter with this name already exists");
        }
        TransporterEntity saved = transporterDAO.save(transporter);
        return ResponseEntity.ok(saved);
    }
}
