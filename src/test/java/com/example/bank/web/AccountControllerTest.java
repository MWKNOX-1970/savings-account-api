package com.example.bank.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bank.action.CreateAccountAction;
import com.example.bank.action.GetAccountAction;
import com.example.bank.model.Account;
import com.example.bank.exception.AccountLimitExceededException;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.OffensiveNickNameException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.CannotCreateTransactionException;

@WebMvcTest(AccountController.class)
@Import(AccountResponseConverter.class)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean CreateAccountAction createAccountAction;
    @MockBean GetAccountAction getAccountAction;

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(createAccountAction.execute("Jane Doe", "My savings"))
                .thenReturn(new Account(1L, "123456789012", "Jane Doe", "My savings"));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Jane Doe\",\"accountNickName\":\"My savings\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/accounts/123456789012")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("123456789012"))
                .andExpect(jsonPath("$.customerName").value("Jane Doe"))
                .andExpect(jsonPath("$.accountNickName").value("My savings"));
    }

    @Test
    void createAllowsMissingNickName() throws Exception {
        when(createAccountAction.execute(eq("Jane Doe"), any()))
                .thenReturn(new Account(2L, "123456789012", "Jane Doe", null));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Jane Doe\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createRejectsMissingCustomerName() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountNickName\":\"My savings\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("customerName: customerName is mandatory"));
        verifyNoInteractions(createAccountAction);
    }

    @Test
    void createRejectsBlankCustomerName() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"   \"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(createAccountAction);
    }

    @Test
    void createRejectsNickNameShorterThanFive() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Jane Doe\",\"accountNickName\":\"abcd\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(createAccountAction);
    }

    @Test
    void createRejectsNickNameLongerThanThirty() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Jane Doe\",\"accountNickName\":\"" + "a".repeat(31) + "\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(createAccountAction);
    }

    @Test
    void createAcceptsNickNameAtBoundaries() throws Exception {
        when(createAccountAction.execute(eq("Jane Doe"), any()))
                .thenReturn(new Account(3L, "123456789012", "Jane Doe", "aaaaa"));

        for (int length : new int[] {5, 30}) {
            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"customerName\":\"Jane Doe\",\"accountNickName\":\"" + "a".repeat(length) + "\"}"))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    void createRejectsBlankNickName() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Jane Doe\",\"accountNickName\":\"      \"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(createAccountAction);
    }

    @Test
    void createRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturns409WhenAccountLimitReached() throws Exception {
        when(createAccountAction.execute(any(), any())).thenThrow(new AccountLimitExceededException(5));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Jane Doe\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createReturns422ForOffensiveNickName() throws Exception {
        when(createAccountAction.execute(any(), any())).thenThrow(new OffensiveNickNameException());

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Jane Doe\",\"accountNickName\":\"badword\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createReturns503WhenDatabaseIsUnavailable() throws Exception {
        when(createAccountAction.execute(any(), any())).thenThrow(new CannotCreateTransactionException("db down"));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Jane Doe\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void getReturnsAccount() throws Exception {
        when(getAccountAction.execute("123456789012"))
                .thenReturn(new Account(9L, "123456789012", "Jane Doe", "My savings"));

        mockMvc.perform(get("/api/v1/accounts/123456789012"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.accountNumber").value("123456789012"))
                .andExpect(jsonPath("$.accountNickName").value("My savings"));
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        when(getAccountAction.execute("123456789012")).thenThrow(new AccountNotFoundException("123456789012"));

        mockMvc.perform(get("/api/v1/accounts/123456789012"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRejectsMalformedAccountNumber() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/not-a-number"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(getAccountAction);
    }

    @Test
    void getReturns503WhenDatabaseIsUnavailable() throws Exception {
        when(getAccountAction.execute(any())).thenThrow(new CannotCreateTransactionException("db down"));

        mockMvc.perform(get("/api/v1/accounts/123456789012"))
                .andExpect(status().isServiceUnavailable());
    }
}
