package com.example.bank.action;

import com.example.bank.model.Account;
import com.example.bank.exception.AccountLimitExceededException;
import com.example.bank.exception.AccountNumberGenerationException;
import com.example.bank.exception.OffensiveNickNameException;
import com.example.bank.persistence.AccountEntity;
import com.example.bank.persistence.AccountEntityConverter;
import com.example.bank.persistence.AccountRepository;
import com.example.bank.support.AccountNumberGenerator;
import com.example.bank.support.OffensiveNickNameChecker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a savings account for a customer.
 */
@Component
public class CreateAccountAction {

    private static final int MAX_ACCOUNT_NUMBER_ATTEMPTS = 5;

    private final AccountRepository repository;
    private final AccountEntityConverter accountEntityConverter;
    private final OffensiveNickNameChecker offensiveNickNameChecker;
    private final AccountNumberGenerator accountNumberGenerator;
    private final int maxAccountsPerCustomer;

    public CreateAccountAction(AccountRepository repository, AccountEntityConverter accountEntityConverter,
                               OffensiveNickNameChecker offensiveNickNameChecker, AccountNumberGenerator accountNumberGenerator,
                               @Value("${app.account.max-per-customer:5}") int maxAccountsPerCustomer) {
        this.repository = repository;
        this.accountEntityConverter = accountEntityConverter;
        this.offensiveNickNameChecker = offensiveNickNameChecker;
        this.accountNumberGenerator = accountNumberGenerator;
        this.maxAccountsPerCustomer = maxAccountsPerCustomer;
    }

    /**
     * Creates a savings account and returns it as a {@link Account}.
     *
     * TODO: the customer is identified by name only because the spec has no customer table.
     * TODO: customers with the same name will be treated as the same
     * TODO: no check for a customer reusing the same nick name on another account.
     * TODO: Handle account number collisions during concurrent account creation.
     * @param nickName optional, may be {@code null}
     */
    @Transactional
    public Account execute(String customerName, String nickName) {
        String name = customerName.trim();

        if (nickName != null && offensiveNickNameChecker.isOffensive(nickName)) {
            throw new OffensiveNickNameException();
        }
        if (repository.countByCustomerNameIgnoreCase(name) >= maxAccountsPerCustomer) {
            throw new AccountLimitExceededException(maxAccountsPerCustomer);
        }

        AccountEntity saved = repository.save(new AccountEntity(nextUniqueAccountNumber(), name, nickName));
        return accountEntityConverter.convert(saved);
    }

    /**
     * Picks a random account number that isn't already taken.
     *
     * TODO: not production ready. Needs enhancements, e.g. handling two requests picking the
     * same number at the same time.
     */
    private String nextUniqueAccountNumber() {
        for (int attempt = 1; attempt <= MAX_ACCOUNT_NUMBER_ATTEMPTS; attempt++) {
            String candidate = accountNumberGenerator.generate();
            if (!repository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new AccountNumberGenerationException(MAX_ACCOUNT_NUMBER_ATTEMPTS);
    }
}
