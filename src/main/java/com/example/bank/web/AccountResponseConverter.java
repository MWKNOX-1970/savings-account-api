package com.example.bank.web;

import com.example.bank.model.Account;
import org.springframework.stereotype.Component;

/** Converts the {@link Account} returned by the actions into the API response body. */
@Component
public class AccountResponseConverter {

    public AccountResponse convert(Account account) {
        return new AccountResponse(
                account.id(),
                account.accountNumber(),
                account.customerName(),
                account.nickName());
    }
}
