package com.serverManagement.server.management.controller.rma;

import com.serverManagement.server.management.enums.RepairStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RepairStatusController {

    @GetMapping("/repair-statuses")
    public ResponseEntity<List<Map<String, String>>> getRepairStatuses() {
        List<Map<String, String>> statuses = Arrays.stream(RepairStatus.values())
                .map(status -> Map.of(
                        "value", status.name(),
                        "label", status.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(statuses);
    }
}
