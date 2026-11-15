package com.example.bank.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OffensiveNickNameRepository extends JpaRepository<OffensiveNickNameEntity, Long> {

    /**
     * Returns every offensive word. The list is small, so it's loaded in one go and
     * kept in memory by OffensiveNickNameChecker.
     *
     * TODO: performance. Fine for a small list, but a real one would be large. Loading it all
     * and checking every word on each create won't scale, so it needs a faster approach.
     */
    @Query("select o.word from OffensiveNickNameEntity o")
    List<String> findAllWords();
}
