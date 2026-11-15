package com.example.bank.model;

/**
 * What the actions return to their caller. Deliberately independent of both the JPA
 * entity and the API response object; the web layer converts it to the response.
 *
 * @param nickName may be {@code null}
 */
public record Account(long id, String accountNumber,
                      String customerName, String nickName) {
}
