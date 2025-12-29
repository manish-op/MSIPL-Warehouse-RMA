package com.serverManagement.server.management.entity.rma.common;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Table(name = "rma_customers")
@Entity
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fields matching RMA form - RETURN ADDRESS DETAILS
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "email")
    private String email;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "telephone", length = 50)
    private String telephone;

    @Column(name = "mobile", length = 50)
    private String mobile;

    // Full address (single field as in form)
    @Column(name = "address", length = 1000)
    private String address;

    // Turn Around Time in days (for both local and depot repairs)
    @Column(name = "tat")
    private Integer tat;

    // Audit fields
    @Column(name = "created_date")
    private ZonedDateTime createdDate;

    @Column(name = "updated_date")
    private ZonedDateTime updatedDate;

    // Constructors
    public CustomerEntity() {
        super();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getTat() {
        return tat;
    }

    public void setTat(Integer tat) {
        this.tat = tat;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public ZonedDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(ZonedDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {
        return "CustomerEntity [id=" + id + ", companyName=" + companyName + ", email=" + email + "]";
    }
}
