package com.example.bank.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * TODO (multi-language): add a language column (BCP-47 code, e.g. "en", "fr") so each
 * term belongs to a language, and look terms up by the language(s) relevant to the request.
 */
@Entity
@Table(name = "offensive_nicknames")
public class OffensiveNickNameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word", nullable = false, length = 50)
    private String word;

    protected OffensiveNickNameEntity() {
        // required by JPA
    }

    public Long getId() {
        return id;
    }

    public String getWord() {
        return word;
    }
}
