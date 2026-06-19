package com.footybox.importjob;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
    Optional<ImportJob> findByProviderAndJobTypeAndCompetitionCodeAndSeasonYear(
            String provider,
            String jobType,
            String competitionCode,
            Integer seasonYear
    );
}
