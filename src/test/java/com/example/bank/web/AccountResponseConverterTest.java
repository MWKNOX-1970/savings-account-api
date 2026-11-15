package com.example.bank.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bank.model.Account;
import org.junit.jupiter.api.Test;

class AccountResponseConverterTest {

    private final AccountResponseConverter converter = new AccountResponseConverter();

    @Test
    void convertsAllFields() {
        AccountResponse response = converter.convert(new Account(1L, "123456789012", "Jane Doe", "My savings"));

        assertThat(response).isEqualTo(new AccountResponse(1L, "123456789012", "Jane Doe", "My savings"));
    }

    @Test
    void keepsMissingNickNameAsNull() {
        AccountResponse response = converter.convert(new Account(1L, "123456789012", "Jane Doe", null));

        assertThat(response.accountNickName()).isNull();
    }
}
