package com.footybox.football;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findByCompetitionAndLabel(Competition competition, String label);
}
