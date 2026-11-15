package com.example.bank.persistence;

import com.example.bank.model.Account;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Cached read path for accounts. Accounts are immutable today, so entries are
 * only expired by TTL.
 *
 * TODO: evict the entry here if update/close operations are added.
 */
@Component
public class AccountReader {

    public static final String CACHE_NAME = "accounts";

    private final AccountRepository repository;
    private final AccountEntityConverter converter;

    public AccountReader(AccountRepository repository, AccountEntityConverter converter) {
        this.repository = repository;
        this.converter = converter;
    }

    /** Misses (empty results) are never cached. */
    @Cacheable(cacheNames = CACHE_NAME, key = "#accountNumber", unless = "#result == null")
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber).map(converter::convert);
    }
}
