package com.serverManagement.server.management.dao.itemRepairDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.serverManagement.server.management.entity.itemRepairDetails.ItemRepairingEntity;
import com.serverManagement.server.management.entity.itemRepairOption.RepairingOptionEntity;
import com.serverManagement.server.management.entity.itemRepairOption.TechnicianStatusEntity;
import com.serverManagement.server.management.entity.region.RegionEntity;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

@Component
public class ItemRepairingDetilsDynamicQueryForManager {

    public Specification<ItemRepairingEntity> getItemRepairingDetailsQuery(
            RegionEntity region,
            LocalDate statingDate,
            LocalDate endDate,
            TechnicianStatusEntity technicianStatus,
            RepairingOptionEntity repairStatus) {
        return (root, query, criteriaBuilder) -> {
        	
			List<Predicate> predicates=new ArrayList<Predicate>();

            if (region == null || region.getId()==null) {
                throw new IllegalArgumentException("Region is required.");
            } else {
                predicates.add(criteriaBuilder.equal(root.get("region"), region)); // Use rootTuple
            }

            if (statingDate != null && endDate != null) {
                Predicate greaterOrEqualPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("generatedDate"), statingDate); // Use rootTuple
                Predicate lessOrEqualPredicate = criteriaBuilder.lessThanOrEqualTo(root.get("generatedDate"), endDate);     // Use rootTuple
                predicates.add(criteriaBuilder.and(greaterOrEqualPredicate, lessOrEqualPredicate));
            }

            if (technicianStatus != null && technicianStatus.getId() !=null) {
                predicates.add(criteriaBuilder.equal(root.get("technicianStatus"), technicianStatus)); // Use rootTuple
            }

            if (repairStatus != null && repairStatus.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("repairStatus"), repairStatus));  // Use rootTuple
            }

            if (predicates.isEmpty()) {
                throw new IllegalArgumentException("At least one search parameter is required.");
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            
        };
    }
    
    
    //this is for getting assign ticket details on the basis of 
    public Specification<ItemRepairingEntity> getAssignTicketQuery(
            RegionEntity region,
            LocalDate statingDate,
            LocalDate endDate,
            String empEmail,
            RepairingOptionEntity repairStatus,
            String ticketId,
        	String rmaNo,
        	String serialNo) {
        return (root, query, criteriaBuilder) -> {
        	
			List<Predicate> predicates=new ArrayList<Predicate>();

            if (region == null || region.getId()==null) {
                throw new IllegalArgumentException("Region is required.");
            } else {
                predicates.add(criteriaBuilder.equal(root.get("region"), region)); // Use rootTuple
            }
            
            if (empEmail == null || (empEmail!=null && empEmail.trim().length()<1)) {
                throw new IllegalArgumentException("Employee Email is required.");
            } else {
                predicates.add(criteriaBuilder.equal(root.get("technician_Name"), empEmail.trim().toLowerCase())); // Use rootTuple
            }

            if (statingDate != null && endDate != null) {
                Predicate greaterOrEqualPredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("generatedDate"), statingDate); // Use rootTuple
                Predicate lessOrEqualPredicate = criteriaBuilder.lessThanOrEqualTo(root.get("generatedDate"), endDate);     // Use rootTuple
                predicates.add(criteriaBuilder.and(greaterOrEqualPredicate, lessOrEqualPredicate));
            }


            if (repairStatus != null && repairStatus.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("repairStatus"), repairStatus));  // Use rootTuple
            }
            
            
            if (ticketId != null && ticketId.trim().length()>1) {
                predicates.add(criteriaBuilder.equal(root.get("id"), ticketId));  // Use rootTuple
            }
            
            
            if (rmaNo != null && rmaNo.trim().length()>0) {
                predicates.add(criteriaBuilder.equal(root.get("rmaNo"), rmaNo));  // Use rootTuple
            }
            
            
            if (serialNo != null && serialNo.trim().length()<1) {
                predicates.add(criteriaBuilder.equal(root.get("serialNo"), serialNo));  // Use rootTuple
            }

            if (predicates.isEmpty()) {
                throw new IllegalArgumentException("At least one search parameter is required.");
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            
        };
    }
}
