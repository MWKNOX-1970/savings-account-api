package com.example.bank.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end test against a real Postgres (Flyway migration included).
 * Skipped automatically when Docker is not available. Redis caching is switched off here;
 * TODO: add a Redis-backed test (Testcontainers) that asserts the second GET is a cache hit.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = "app.cache.enabled=false")
class AccountApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired MockMvc mockMvc;

    private String createAccount(String customer, String nickName) throws Exception {
        String body = nickName == null
                ? "{\"customerName\":\"" + customer + "\"}"
                : "{\"customerName\":\"" + customer + "\",\"accountNickName\":\"" + nickName + "\"}";
        return mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void createThenGetRoundTrip() throws Exception {
        String customer = "Customer-" + UUID.randomUUID();
        String created = createAccount(customer, "Holiday fund");
        String accountNumber = JsonPath.read(created, "$.accountNumber");

        mockMvc.perform(get("/api/v1/accounts/" + accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value(customer))
                .andExpect(jsonPath("$.accountNickName").value("Holiday fund"))
                .andExpect(jsonPath("$.accountNumber").value(accountNumber));
    }

    @Test
    void sixthAccountForSameCustomerIsRejected() throws Exception {
        String customer = "Limit-" + UUID.randomUUID();
        for (int i = 0; i < 5; i++) {
            createAccount(customer, null);
        }

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"" + customer + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void offensiveNickNameIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Jane Doe\",\"accountNickName\":\"b4dword\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void unknownAccountReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/999999999999"))
                .andExpect(status().isNotFound());
    }
}
