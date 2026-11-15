package com.example.bank.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.bank.model.Account;
import com.example.bank.exception.AccountLimitExceededException;
import com.example.bank.exception.AccountNumberGenerationException;
import com.example.bank.exception.OffensiveNickNameException;
import com.example.bank.persistence.AccountEntity;
import com.example.bank.persistence.AccountEntityConverter;
import com.example.bank.persistence.AccountRepository;
import com.example.bank.support.AccountNumberGenerator;
import com.example.bank.support.OffensiveNickNameChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CreateAccountActionTest {

    @Mock AccountRepository repository;
    @Mock OffensiveNickNameChecker checker;
    @Mock AccountNumberGenerator generator;

    CreateAccountAction action;

    @BeforeEach
    void setUp() {
        action = new CreateAccountAction(repository, new AccountEntityConverter(), checker, generator, 5);
    }

    private void stubSaveAssigningId(long id) {
        when(repository.save(any(AccountEntity.class))).thenAnswer(inv -> {
            AccountEntity entity = inv.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", id);
            return entity;
        });
    }

    @Test
    void createsAccountAndReturnsIt() {
        when(repository.countByCustomerNameIgnoreCase("Jane Doe")).thenReturn(0L);
        when(generator.generate()).thenReturn("123456789012");
        stubSaveAssigningId(42L);

        Account result = action.execute("  Jane Doe ", "My savings");

        assertThat(result).isEqualTo(new Account(42L, "123456789012", "Jane Doe", "My savings"));
    }

    @Test
    void createsAccountWithoutNickNameWithoutCheckingIt() {
        when(repository.countByCustomerNameIgnoreCase("Jane Doe")).thenReturn(0L);
        when(generator.generate()).thenReturn("123456789012");
        stubSaveAssigningId(7L);

        Account result = action.execute("Jane Doe", null);

        assertThat(result).isEqualTo(new Account(7L, "123456789012", "Jane Doe", null));
        verifyNoInteractions(checker);
    }

    @Test
    void rejectsWhenCustomerAlreadyHasMaximumAccounts() {
        when(repository.countByCustomerNameIgnoreCase("Jane Doe")).thenReturn(5L);

        assertThatThrownBy(() -> action.execute("Jane Doe", "My savings"))
                .isInstanceOf(AccountLimitExceededException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsOffensiveNickName() {
        when(checker.isOffensive("badword")).thenReturn(true);

        assertThatThrownBy(() -> action.execute("Jane Doe", "badword"))
                .isInstanceOf(OffensiveNickNameException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void retriesWhenGeneratedAccountNumberAlreadyExists() {
        when(repository.countByCustomerNameIgnoreCase("Jane Doe")).thenReturn(0L);
        when(generator.generate()).thenReturn("111111111111", "222222222222");
        when(repository.existsByAccountNumber("111111111111")).thenReturn(true);
        stubSaveAssigningId(1L);

        Account result = action.execute("Jane Doe", null);

        assertThat(result.accountNumber()).isEqualTo("222222222222");
    }

    @Test
    void failsWhenNoUniqueAccountNumberCanBeGenerated() {
        when(repository.countByCustomerNameIgnoreCase("Jane Doe")).thenReturn(0L);
        when(generator.generate()).thenReturn("111111111111");
        when(repository.existsByAccountNumber("111111111111")).thenReturn(true);

        assertThatThrownBy(() -> action.execute("Jane Doe", null))
                .isInstanceOf(AccountNumberGenerationException.class);

        verify(repository, never()).save(any());
    }
}
