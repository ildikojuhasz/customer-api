package com.example.customerapi.service;

import com.example.customerapi.dto.CustomerRequest;
import com.example.customerapi.dto.CustomerResponse;
import com.example.customerapi.exception.CustomerNotFoundException;
import com.example.customerapi.exception.DuplicateEmailException;
import com.example.customerapi.model.Customer;
import com.example.customerapi.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse create(CustomerRequest request) {
        String email = normalizeEmail(request.getEmail());
        ensureEmailAvailable(email, null);

        Customer customer = new Customer(
                UUID.randomUUID(),
                request.getFirstName().trim(),
                request.getLastName().trim(),
                email,
                request.getStatus()
        );
        return CustomerResponse.from(customerRepository.save(customer));
    }

    public CustomerResponse getById(UUID id) {
        return CustomerResponse.from(requireCustomer(id));
    }

    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from)
                .toList();
    }

    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer existing = requireCustomer(id);
        String email = normalizeEmail(request.getEmail());
        ensureEmailAvailable(email, id);

        existing.setFirstName(request.getFirstName().trim());
        existing.setLastName(request.getLastName().trim());
        existing.setEmail(email);
        existing.setStatus(request.getStatus());

        return CustomerResponse.from(customerRepository.save(existing));
    }

    public void delete(UUID id) {
        if (!customerRepository.deleteById(id)) {
            throw new CustomerNotFoundException(id);
        }
    }

    private Customer requireCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    private void ensureEmailAvailable(String email, UUID excludedId) {
        boolean taken = excludedId == null
                ? customerRepository.existsByEmailIgnoreCase(email)
                : customerRepository.existsByEmailIgnoreCaseExcludingId(email, excludedId);
        if (taken) {
            throw new DuplicateEmailException(email);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
