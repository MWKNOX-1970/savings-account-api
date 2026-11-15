package com.example.bank.exception;

public class AccountLimitExceededException extends RuntimeException {

    public AccountLimitExceededException(int maxAccounts) {
        super("A customer cannot have more than " + maxAccounts + " accounts");
    }
}
