package com.example.bank.persistence;

import com.example.bank.model.Account;
import org.springframework.stereotype.Component;

/**
 * Converts an AccountEntity from the database into the domain Account.
 *
 */
@Component
public class AccountEntityConverter {

    /**
     * Copies the entity's fields into a new Account.
     *
     * TODO: the database id is passed through as is, so the API exposes a sequential id that is easy to
     * guess and hints at how many accounts exist. Obfuscate it, or use a separate public id such as a UUID.
     */
    public Account convert(AccountEntity entity) {
        return new Account(entity.getId(), entity.getAccountNumber(),
                entity.getCustomerName(), entity.getAccountNickName());
    }
}
