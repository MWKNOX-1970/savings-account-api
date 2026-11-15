package com.example.bank.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.bank.persistence.OffensiveNickNameRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffensiveNickNameCheckerTest {

    @Mock OffensiveNickNameRepository repository;

    OffensiveNickNameChecker checker;

    @BeforeEach
    void setUp() {
        checker = new OffensiveNickNameChecker(repository, 5);
    }

    @Test
    void acceptsCleanNickName() {
        when(repository.findAllWords()).thenReturn(List.of("badword", "idiot"));
        assertThat(checker.isOffensive("Holiday fund")).isFalse();
    }

    @Test
    void acceptsNullWithoutTouchingTheDatabase() {
        assertThat(checker.isOffensive(null)).isFalse();
        verify(repository, times(0)).findAllWords();
    }

    @Test
    void rejectsListedTermIgnoringCase() {
        when(repository.findAllWords()).thenReturn(List.of("badword"));
        assertThat(checker.isOffensive("BadWord")).isTrue();
    }

    @Test
    void rejectsTermEmbeddedInLongerNickName() {
        when(repository.findAllWords()).thenReturn(List.of("idiot"));
        assertThat(checker.isOffensive("my idiot account")).isTrue();
    }

    @Test
    void rejectsCommonSubstitutionsAndPunctuation() {
        when(repository.findAllWords()).thenReturn(List.of("badword", "idiot"));
        assertThat(checker.isOffensive("b4d-w0rd")).isTrue();
        assertThat(checker.isOffensive("1d10t")).isTrue();
    }

    @Test
    void loadsTermsOnceWithinTheRefreshWindow() {
        when(repository.findAllWords()).thenReturn(List.of("badword"));

        checker.isOffensive("first nick");
        checker.isOffensive("second nick");

        verify(repository, times(1)).findAllWords();
    }
}
