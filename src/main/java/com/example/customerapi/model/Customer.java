package com.example.customerapi.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain / persistence representation of a customer.
 * Kept separate from API DTOs so the HTTP contract can evolve independently.
 */
public class Customer {

    private final UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private CustomerStatus status;

    public Customer(UUID id, String firstName, String lastName, String email, CustomerStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName = Objects.requireNonNull(lastName, "lastName");
        this.email = Objects.requireNonNull(email, "email");
        this.status = Objects.requireNonNull(status, "status");
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = Objects.requireNonNull(firstName, "firstName");
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = Objects.requireNonNull(lastName, "lastName");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "email");
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }
}
