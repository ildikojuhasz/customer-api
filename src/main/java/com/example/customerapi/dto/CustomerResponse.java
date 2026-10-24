package com.example.customerapi.dto;

import com.example.customerapi.model.Customer;
import com.example.customerapi.model.CustomerStatus;

import java.util.UUID;

/**
 * Stable API representation of a customer.
 */
public class CustomerResponse {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final CustomerStatus status;

    public CustomerResponse(UUID id, String firstName, String lastName, String email, CustomerStatus status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = status;
    }

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getStatus()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public CustomerStatus getStatus() {
        return status;
    }
}
