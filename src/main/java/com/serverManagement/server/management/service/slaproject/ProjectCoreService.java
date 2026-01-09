package com.serverManagement.server.management.service.slaproject;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.slaproject.ProjectCoreDAO;
import com.serverManagement.server.management.entity.slaproject.ProjectCore;

@Service
public class ProjectCoreService {

    @Autowired
    private ProjectCoreDAO projectCoreDAO;

    public ProjectCore saveProjectCore(ProjectCore projectCore) {
        return projectCoreDAO.save(projectCore);
    }

    public List<ProjectCore> getAllProjectCores() {
        return projectCoreDAO.findAll();
    }
}
