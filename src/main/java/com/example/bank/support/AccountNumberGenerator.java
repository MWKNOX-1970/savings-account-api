package com.example.bank.support;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class AccountNumberGenerator {

    private static final int LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a 12 digit account number (first digit is never 0).
     * TODO: a real bank would add a branch/sort code and a check digit.
     */
    public String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        sb.append(1 + random.nextInt(9));
        for (int i = 1; i < LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
