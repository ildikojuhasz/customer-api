package com.example.customerapi.service;

import com.example.customerapi.dto.CustomerRequest;
import com.example.customerapi.dto.CustomerResponse;
import com.example.customerapi.exception.CustomerNotFoundException;
import com.example.customerapi.exception.DuplicateEmailException;
import com.example.customerapi.model.CustomerStatus;
import com.example.customerapi.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerServiceTest {

    private CustomerRepository customerRepository;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerRepository = new CustomerRepository();
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void create_assignsIdAndPersistsCustomer() {
        CustomerResponse created = customerService.create(sampleRequest("Ada", "Lovelace", "ada@example.com"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getFirstName()).isEqualTo("Ada");
        assertThat(created.getLastName()).isEqualTo("Lovelace");
        assertThat(created.getEmail()).isEqualTo("ada@example.com");
        assertThat(created.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(customerService.getById(created.getId()).getEmail()).isEqualTo("ada@example.com");
    }

    @Test
    void create_normalizesEmailToLowerCase() {
        CustomerResponse created = customerService.create(
                sampleRequest("Ada", "Lovelace", "Ada@Example.COM")
        );

        assertThat(created.getEmail()).isEqualTo("ada@example.com");
    }

    @Test
    void create_rejectsDuplicateEmailIgnoringCase() {
        customerService.create(sampleRequest("Ada", "Lovelace", "ada@example.com"));

        assertThatThrownBy(() ->
                customerService.create(sampleRequest("Other", "Person", "ADA@example.com"))
        )
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("ada@example.com");
    }

    @Test
    void getById_returnsExistingCustomer() {
        CustomerResponse created = customerService.create(sampleRequest("Grace", "Hopper", "grace@example.com"));

        CustomerResponse found = customerService.getById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getFirstName()).isEqualTo("Grace");
    }

    @Test
    void getById_throwsWhenMissing() {
        UUID missingId = UUID.randomUUID();

        assertThatThrownBy(() -> customerService.getById(missingId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    @Test
    void getAll_returnsAllCustomers() {
        customerService.create(sampleRequest("A", "One", "a@example.com"));
        customerService.create(sampleRequest("B", "Two", "b@example.com"));

        List<CustomerResponse> customers = customerService.getAll();

        assertThat(customers).hasSize(2);
        assertThat(customers).extracting(CustomerResponse::getEmail)
                .containsExactlyInAnyOrder("a@example.com", "b@example.com");
    }

    @Test
    void getAll_returnsEmptyListWhenNoneExist() {
        assertThat(customerService.getAll()).isEmpty();
    }

    @Test
    void update_modifiesExistingCustomer() {
        CustomerResponse created = customerService.create(sampleRequest("Alan", "Turing", "alan@example.com"));

        CustomerRequest update = new CustomerRequest(
                "Alan",
                "Turing",
                "alan.turing@example.com",
                CustomerStatus.INACTIVE
        );

        CustomerResponse updated = customerService.update(created.getId(), update);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getEmail()).isEqualTo("alan.turing@example.com");
        assertThat(updated.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
    }

    @Test
    void update_allowsKeepingSameEmail() {
        CustomerResponse created = customerService.create(sampleRequest("Alan", "Turing", "alan@example.com"));

        CustomerRequest update = new CustomerRequest(
                "Alan Mathison",
                "Turing",
                "alan@example.com",
                CustomerStatus.ACTIVE
        );

        CustomerResponse updated = customerService.update(created.getId(), update);

        assertThat(updated.getFirstName()).isEqualTo("Alan Mathison");
        assertThat(updated.getEmail()).isEqualTo("alan@example.com");
    }

    @Test
    void update_rejectsEmailOwnedByAnotherCustomer() {
        CustomerResponse first = customerService.create(sampleRequest("A", "One", "a@example.com"));
        customerService.create(sampleRequest("B", "Two", "b@example.com"));

        CustomerRequest update = new CustomerRequest("A", "One", "b@example.com", CustomerStatus.ACTIVE);

        assertThatThrownBy(() -> customerService.update(first.getId(), update))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void update_throwsWhenCustomerMissing() {
        UUID missingId = UUID.randomUUID();

        assertThatThrownBy(() -> customerService.update(missingId, sampleRequest("X", "Y", "x@example.com")))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void delete_removesCustomer() {
        CustomerResponse created = customerService.create(sampleRequest("Delete", "Me", "delete@example.com"));

        customerService.delete(created.getId());

        assertThatThrownBy(() -> customerService.getById(created.getId()))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void delete_throwsWhenCustomerMissing() {
        UUID missingId = UUID.randomUUID();

        assertThatThrownBy(() -> customerService.delete(missingId))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    private CustomerRequest sampleRequest(String firstName, String lastName, String email) {
        return new CustomerRequest(firstName, lastName, email, CustomerStatus.ACTIVE);
    }
}
