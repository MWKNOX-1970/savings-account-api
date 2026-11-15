package com.example.bank.support;

import com.example.bank.persistence.OffensiveNickNameRepository;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Checks a nick name against the offensive terms stored in the database.
 * The terms are kept in memory and refreshed every few minutes, so a create request
 * does not pay for an extra query each time and edits to the table take effect without a redeploy.
 *
 * <p>TODO (multi-language): terms and normalisation are English/Latin only today. See
 * {@code V2__create_offensive_nicknames.sql} for the planned language column.
 *
 * <p>TODO: simple substring matching can produce false positives (the "Scunthorpe problem").
 * In a real system use a maintained word list / moderation service and an allow-list.
 */
@Component
public class OffensiveNickNameChecker {

    private final OffensiveNickNameRepository repository;
    private final long refreshNanos;

    private volatile Set<String> terms;
    private volatile long loadedAtNanos;

    public OffensiveNickNameChecker(OffensiveNickNameRepository repository,
                                    @Value("${app.offensive-nicknames.refresh-minutes:5}") long refreshMinutes) {
        this.repository = repository;
        this.refreshNanos = TimeUnit.MINUTES.toNanos(refreshMinutes);
    }

    public boolean isOffensive(String nickName) {
        if (nickName == null) {
            return false;
        }
        String normalized = normalize(nickName);
        return currentTerms().stream().anyMatch(normalized::contains);
    }

    private Set<String> currentTerms() {
        if (terms == null || System.nanoTime() - loadedAtNanos > refreshNanos) {
            synchronized (this) {
                if (terms == null || System.nanoTime() - loadedAtNanos > refreshNanos) {
                    terms = repository.findAllWords().stream()
                            .map(OffensiveNickNameChecker::normalize)
                            .filter(term -> !term.isEmpty())
                            .collect(Collectors.toUnmodifiableSet());
                    loadedAtNanos = System.nanoTime();
                }
            }
        }
        return terms;
    }

    /**
     * Lower-cases, maps common look-alike characters to letters (so "b4dw0rd" is caught as
     * "badword") and drops everything that is not a letter (spaces, dots, dashes, ...).
     */
    static String normalize(String value) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toLowerCase(Locale.ROOT).toCharArray()) {
            switch (c) {
                case '0' -> sb.append('o');
                case '1', '!' -> sb.append('i');
                case '3' -> sb.append('e');
                case '4', '@' -> sb.append('a');
                case '5', '$' -> sb.append('s');
                case '7' -> sb.append('t');
                default -> {
                    if (Character.isLetter(c)) {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
