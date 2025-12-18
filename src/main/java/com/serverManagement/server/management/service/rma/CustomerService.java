package com.serverManagement.server.management.service.rma;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serverManagement.server.management.dao.rma.CustomerDAO;
import com.serverManagement.server.management.entity.rma.CustomerEntity;

@Service
public class CustomerService {

    @Autowired
    private CustomerDAO customerDAO;

    /**
     * Find or create a customer based on company name and email.
     * If customer exists, return existing. Otherwise, create new.
     */
    @Transactional
    public CustomerEntity findOrCreateCustomer(String companyName, String contactName, String email,
            String telephone, String mobile, String address, Integer tat) {

        // Try to find existing customer by company name + email combination
        Optional<CustomerEntity> existingCustomer = customerDAO.findExistingCustomer(companyName, email);

        if (existingCustomer.isPresent()) {
            // Update TAT if provided and customer exists
            CustomerEntity existing = existingCustomer.get();
            if (tat != null) {
                existing.setTat(tat);
                existing.setUpdatedDate(ZonedDateTime.now());
                customerDAO.save(existing);
            }
            return existing;
        }

        // Create new customer
        CustomerEntity newCustomer = new CustomerEntity();
        newCustomer.setCompanyName(companyName);
        newCustomer.setContactName(contactName);
        newCustomer.setEmail(email);
        newCustomer.setTelephone(telephone);
        newCustomer.setMobile(mobile);
        newCustomer.setAddress(address);
        newCustomer.setTat(tat);
        newCustomer.setCreatedDate(ZonedDateTime.now());
        newCustomer.setUpdatedDate(ZonedDateTime.now());

        return customerDAO.save(newCustomer);
    }

    /**
     * Get all customers for dropdown list
     */
    public ResponseEntity<?> getAllCustomers() {
        try {
            List<CustomerEntity> customers = customerDAO.findAllByOrderByCompanyNameAsc();
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch customers: " + e.getMessage());
        }
    }

    /**
     * Search customers by company name or email (for auto-complete)
     */
    public ResponseEntity<?> searchCustomers(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.ok(customerDAO.findAllByOrderByCompanyNameAsc());
            }
            List<CustomerEntity> customers = customerDAO.searchByCompanyNameOrEmail(searchTerm.trim());
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to search customers: " + e.getMessage());
        }
    }

    /**
     * Get customer by ID
     */
    public ResponseEntity<?> getCustomerById(Long id) {
        try {
            Optional<CustomerEntity> customer = customerDAO.findById(id);
            if (customer.isPresent()) {
                return ResponseEntity.ok(customer.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch customer: " + e.getMessage());
        }
    }

    /**
     * Update existing customer
     */
    @Transactional
    public ResponseEntity<?> updateCustomer(Long id, CustomerEntity updatedCustomer) {
        try {
            Optional<CustomerEntity> existingOpt = customerDAO.findById(id);
            if (!existingOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
            }

            CustomerEntity existing = existingOpt.get();
            existing.setCompanyName(updatedCustomer.getCompanyName());
            existing.setContactName(updatedCustomer.getContactName());
            existing.setEmail(updatedCustomer.getEmail());
            existing.setTelephone(updatedCustomer.getTelephone());
            existing.setMobile(updatedCustomer.getMobile());
            existing.setAddress(updatedCustomer.getAddress());
            existing.setUpdatedDate(ZonedDateTime.now());

            CustomerEntity saved = customerDAO.save(existing);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update customer: " + e.getMessage());
        }
    }
}
