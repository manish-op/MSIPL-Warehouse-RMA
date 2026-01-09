package com.serverManagement.server.management.dao.slaproject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serverManagement.server.management.entity.slaproject.ProjectCore;

@Repository
public interface ProjectCoreDAO extends JpaRepository<ProjectCore, Long> {

}
