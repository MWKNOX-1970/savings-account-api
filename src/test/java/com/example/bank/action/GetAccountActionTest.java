package com.example.bank.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.bank.model.Account;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.persistence.AccountReader;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAccountActionTest {

    @Mock AccountReader accountReader;
    @InjectMocks GetAccountAction action;

    @Test
    void returnsExistingAccount() {
        Account account = new Account(9L, "123456789012", "Jane Doe", "My savings");
        when(accountReader.findByAccountNumber("123456789012")).thenReturn(Optional.of(account));

        assertThat(action.execute("123456789012")).isEqualTo(account);
    }

    @Test
    void throwsWhenAccountDoesNotExist() {
        when(accountReader.findByAccountNumber("123456789012")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> action.execute("123456789012"))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
