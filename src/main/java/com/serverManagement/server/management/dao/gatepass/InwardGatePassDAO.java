package com.serverManagement.server.management.dao.gatepass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serverManagement.server.management.entity.gatePass.InwardGatePassEntity;

public interface InwardGatePassDAO extends JpaRepository<InwardGatePassEntity, Long> {

    @Query("SELECT um FROM InwardGatePassEntity um WHERE um.id=(:id)")
    public InwardGatePassEntity getById(@Param("id") Long id);

    @Query("SELECT um FROM InwardGatePassEntity um JOIN um.itemList item WHERE item.serialNo = (:serialNo) ORDER BY um.createdDate DESC LIMIT 1")
    public InwardGatePassEntity getInwardPassBySerial(@Param("serialNo") String serialNo);
}
