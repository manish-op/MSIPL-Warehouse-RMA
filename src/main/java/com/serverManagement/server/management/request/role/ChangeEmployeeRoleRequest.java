package com.serverManagement.server.management.request.role;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangeEmployeeRoleRequest {

    private String empEmail;
    private String role;

    public ChangeEmployeeRoleRequest(String empEmail, String role) {
        super();
        this.empEmail = empEmail;
        this.role = role;
    }

    public ChangeEmployeeRoleRequest() {
        super();
    }

    public String getEmpEmail() {
        return empEmail;
    }

    public void setEmpEmail(String empEmail) {
        this.empEmail = empEmail;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "ChangeEmployeeRoleRequest [empEmail=" + empEmail + ", role=" + role + "]";
    }

}
