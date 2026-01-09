package com.serverManagement.server.management.dao.rma.common;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.serverManagement.server.management.entity.rma.common.CustomerEntity;

@Repository
public interface CustomerDAO extends JpaRepository<CustomerEntity, Long> {

        // Find customer by company name and email (for matching existing customers)
        Optional<CustomerEntity> findByCompanyNameAndEmail(String companyName, String email);

        // Search customers by company name (case-insensitive, for auto-complete)
        List<CustomerEntity> findByCompanyNameContainingIgnoreCaseOrderByCompanyNameAsc(String companyName);

        // Search by email (case-insensitive)
        List<CustomerEntity> findByEmailContainingIgnoreCaseOrderByCompanyNameAsc(String email);

        // Get all customers ordered by company name
        List<CustomerEntity> findAllByOrderByCompanyNameAsc();

        // Search by company name or email
        @Query("SELECT c FROM CustomerEntity c WHERE " +
                        "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "ORDER BY c.companyName ASC")
        List<CustomerEntity> searchByCompanyNameOrEmail(@Param("searchTerm") String searchTerm);

        // Find existing customer for RMA (by company name and email combination)
        @Query("SELECT c FROM CustomerEntity c WHERE " +
                        "LOWER(c.companyName) = LOWER(:companyName) AND " +
                        "LOWER(c.email) = LOWER(:email)")
        Optional<CustomerEntity> findExistingCustomer(
                        @Param("companyName") String companyName,
                        @Param("email") String email);
}
