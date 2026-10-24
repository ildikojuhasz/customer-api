package com.example.customerapi.model;

/**
 * Allowed customer lifecycle states.
 * Unknown values in JSON requests fail deserialization with a 400 response.
 */
public enum CustomerStatus {
    ACTIVE,
    INACTIVE,
    PENDING
}
