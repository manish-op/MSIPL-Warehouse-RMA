package com.serverManagement.server.management.dao.rma;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serverManagement.server.management.entity.rma.RmaRequestEntity;
import java.time.ZonedDateTime;

@Repository
public interface RmaRequestDAO extends JpaRepository<RmaRequestEntity, Long> {

    // Custom query to fetch all RMA requests with their items (eager loading)
    @Query("SELECT DISTINCT r FROM RmaRequestEntity r LEFT JOIN FETCH r.items")
    List<RmaRequestEntity> findAllWithItems();

    // Find RMA request by request number
    // Find RMA request by request number
    RmaRequestEntity findByRequestNumber(String requestNumber);

    // Find RMA request by manual RMA number
    RmaRequestEntity findByRmaNo(String rmaNo);

    // Find requests by created date range
    List<RmaRequestEntity> findByCreatedDateBetween(ZonedDateTime startDate, ZonedDateTime endDate);
}
