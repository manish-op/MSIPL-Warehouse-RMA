package com.serverManagement.server.management.controller.slaproject;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.entity.slaproject.ProjectCore;
import com.serverManagement.server.management.service.slaproject.ProjectCoreService;

@RestController
@RequestMapping("/api/project-core")
public class ProjectCoreController {

    @Autowired
    private ProjectCoreService projectCoreService;

    @PostMapping("/create")
    public ResponseEntity<ProjectCore> createProjectCore(@RequestBody ProjectCore projectCore) {
        // Ensure bidirectional relationship is set if details are provided
        if (projectCore.getServiceSlaDetails() != null) {
            projectCore.getServiceSlaDetails().setProjectCore(projectCore);
        }
        ProjectCore savedProject = projectCoreService.saveProjectCore(projectCore);
        return ResponseEntity.ok(savedProject);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProjectCore>> getAllProjectCores() {
        List<ProjectCore> projects = projectCoreService.getAllProjectCores();
        return ResponseEntity.ok(projects);
    }
}
