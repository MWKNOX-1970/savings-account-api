package com.example.bank.action;

import com.example.bank.model.Account;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.persistence.AccountReader;
import org.springframework.stereotype.Component;

/**
 * Fetches a savings account by its account number.
 */
@Component
public class GetAccountAction {

    private final AccountReader accountReader;

    public GetAccountAction(AccountReader accountReader) {
        this.accountReader = accountReader;
    }

    /**
     * Returns the account, or throws AccountNotFoundException if it doesn't exist.
     * TODO: accounts are looked up by account number. Consider replacing this with the account id.
     */
    public Account execute(String accountNumber) {
        return accountReader.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }
}
