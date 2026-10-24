package com.example.customerapi.controller;

import com.example.customerapi.repository.CustomerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void cleanStore() {
        customerRepository.clear();
    }

    @Test
    void createCustomer_returns201AndBody() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCustomerJson("Jane", "Doe", "jane.doe@example.com", "ACTIVE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getCustomerById_returnsExistingCustomer() throws Exception {
        String id = createCustomerAndReturnId("John", "Smith", "john.smith@example.com");

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("john.smith@example.com"));
    }

    @Test
    void listCustomers_returnsAll() throws Exception {
        createCustomerAndReturnId("A", "One", "a@example.com");
        createCustomerAndReturnId("B", "Two", "b@example.com");

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", hasItem("a@example.com")))
                .andExpect(jsonPath("$[*].email", hasItem("b@example.com")));
    }

    @Test
    void listCustomers_returnsEmptyArrayWhenNoneExist() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void updateCustomer_returnsUpdatedResource() throws Exception {
        String id = createCustomerAndReturnId("Jane", "Doe", "jane@example.com");

        mockMvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCustomerJson("Janet", "Doe", "janet@example.com", "INACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.firstName").value("Janet"))
                .andExpect(jsonPath("$.email").value("janet@example.com"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void deleteCustomer_returns204AndThenNotFound() throws Exception {
        String id = createCustomerAndReturnId("Temp", "User", "temp@example.com");

        mockMvc.perform(delete("/api/customers/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Customer not found with id: " + id));
    }

    @Test
    void getUpdateDelete_unknownId_return404() throws Exception {
        String missingId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/customers/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(put("/api/customers/{id}", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCustomerJson("X", "Y", "x@example.com", "ACTIVE")))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/customers/{id}", missingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withBlankName_returns400WithDetails() throws Exception {
        String body = """
                {
                  "firstName": " ",
                  "lastName": "Doe",
                  "email": "blank@example.com",
                  "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value(org.hamcrest.Matchers.containsString("firstName")));
    }

    @Test
    void create_withInvalidEmail_returns400() throws Exception {
        String body = """
                {
                  "firstName": "Jane",
                  "lastName": "Doe",
                  "email": "not-an-email",
                  "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value(org.hamcrest.Matchers.containsString("email")));
    }

    @Test
    void create_withUnknownStatus_returns400() throws Exception {
        String body = """
                {
                  "firstName": "Jane",
                  "lastName": "Doe",
                  "email": "jane@example.com",
                  "status": "DELETED"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_withDuplicateEmail_returns409() throws Exception {
        createCustomerAndReturnId("Jane", "Doe", "dup@example.com");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCustomerJson("John", "Doe", "DUP@example.com", "PENDING")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("dup@example.com")));
    }

    @Test
    void get_withMalformedUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private String createCustomerAndReturnId(
            String firstName,
            String lastName,
            String email
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCustomerJson(firstName, lastName, email, "ACTIVE")))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = body.get("id").asText();
        assertThat(id).isNotBlank();
        return id;
    }

    private String validCustomerJson(String firstName, String lastName, String email, String status) {
        return """
                {
                  "firstName": "%s",
                  "lastName": "%s",
                  "email": "%s",
                  "status": "%s"
                }
                """.formatted(firstName, lastName, email, status);
    }
}
