package com.footybox.importjob;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(ImportJobTracker.class)
@ActiveProfiles("test")
class ImportJobTrackerTest {

    @Autowired
    private ImportJobTracker tracker;

    @Autowired
    private ImportJobRepository jobs;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void failedSeasonCanResumeAndSucceedWithoutCreatingAnotherJob() {
        tracker.started("football-data", "PL", 2010);
        tracker.failed("football-data", "PL", 2010, "temporary failure");

        ImportJob failed = job(2010);
        Long jobId = failed.getId();
        assertThat(failed.getStatus()).isEqualTo("FAILED");
        assertThat(failed.getAttempts()).isEqualTo(1);
        assertThat(failed.getLastError()).isEqualTo("temporary failure");
        assertThat(failed.getFinishedAt()).isNotNull();

        tracker.started("football-data", "PL", 2010);
        tracker.succeeded("football-data", "PL", 2010, 380);
        entityManager.clear();

        ImportJob succeeded = job(2010);
        assertThat(succeeded.getId()).isEqualTo(jobId);
        assertThat(succeeded.getStatus()).isEqualTo("SUCCEEDED");
        assertThat(succeeded.getAttempts()).isEqualTo(2);
        assertThat(succeeded.getCheckpoint()).isEqualTo("matches=380");
        assertThat(succeeded.getLastError()).isNull();
        assertThat(succeeded.getStartedAt()).isNotNull();
        assertThat(succeeded.getFinishedAt()).isNotNull();
    }

    @Test
    void unavailableSeasonIsRecordedAsSkipped() {
        tracker.started("football-data", "PL", 2011);
        tracker.skipped("football-data", "PL", 2011, "not included in plan");

        ImportJob skipped = job(2011);
        assertThat(skipped.getStatus()).isEqualTo("SKIPPED");
        assertThat(skipped.getLastError()).isEqualTo("not included in plan");
    }

    private ImportJob job(int seasonYear) {
        return jobs.findByProviderAndJobTypeAndCompetitionCodeAndSeasonYear(
                "football-data",
                "COMPETITION_SEASON_MATCHES",
                "PL",
                seasonYear
        ).orElseThrow();
    }
}
