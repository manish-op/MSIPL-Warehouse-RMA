package com.serverManagement.server.management.dao.gatepass;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serverManagement.server.management.entity.gatePass.OutwardGatepassEntity;

public interface OutwardGatepassDAO extends JpaRepository<OutwardGatepassEntity, Long>{

}
