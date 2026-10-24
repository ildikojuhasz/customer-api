package com.example.customerapi.repository;

import com.example.customerapi.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory persistence for customers.
 * Thread-safe via ConcurrentHashMap; data is lost when the process stops.
 *
 * Trade-off vs H2/JPA: simpler to run and reason about for this exercise,
 * but no durability across restarts and no SQL query capabilities.
 */
@Repository
public class CustomerRepository {

    private final ConcurrentHashMap<UUID, Customer> customersById = new ConcurrentHashMap<>();

    public Customer save(Customer customer) {
        customersById.put(customer.getId(), customer);
        return customer;
    }

    public Optional<Customer> findById(UUID id) {
        return Optional.ofNullable(customersById.get(id));
    }

    public List<Customer> findAll() {
        return new ArrayList<>(customersById.values());
    }

    public boolean deleteById(UUID id) {
        return customersById.remove(id) != null;
    }

    public boolean existsByEmailIgnoreCase(String email) {
        String normalized = normalizeEmail(email);
        return customersById.values().stream()
                .anyMatch(customer -> normalizeEmail(customer.getEmail()).equals(normalized));
    }

    public boolean existsByEmailIgnoreCaseExcludingId(String email, UUID excludedId) {
        String normalized = normalizeEmail(email);
        return customersById.values().stream()
                .anyMatch(customer -> !customer.getId().equals(excludedId)
                        && normalizeEmail(customer.getEmail()).equals(normalized));
    }

    public void clear() {
        customersById.clear();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
