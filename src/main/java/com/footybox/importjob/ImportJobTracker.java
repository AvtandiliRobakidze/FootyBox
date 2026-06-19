package com.footybox.importjob;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportJobTracker {

    private static final String JOB_TYPE = "COMPETITION_SEASON_MATCHES";

    private final ImportJobRepository jobs;

    public ImportJobTracker(ImportJobRepository jobs) {
        this.jobs = jobs;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void started(String provider, String competitionCode, int seasonYear) {
        ImportJob job = job(provider, competitionCode, seasonYear);
        job.setStatus("RUNNING");
        job.setAttempts(job.getAttempts() == null ? 1 : job.getAttempts() + 1);
        job.setStartedAt(Instant.now());
        job.setFinishedAt(null);
        job.setLastError(null);
        jobs.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void succeeded(String provider, String competitionCode, int seasonYear, int imported) {
        ImportJob job = job(provider, competitionCode, seasonYear);
        job.setStatus("SUCCEEDED");
        job.setCheckpoint("matches=" + imported);
        job.setFinishedAt(Instant.now());
        job.setLastError(null);
        jobs.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void skipped(String provider, String competitionCode, int seasonYear, String reason) {
        finish(provider, competitionCode, seasonYear, "SKIPPED", reason);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failed(String provider, String competitionCode, int seasonYear, String reason) {
        finish(provider, competitionCode, seasonYear, "FAILED", reason);
    }

    private void finish(String provider, String competitionCode, int seasonYear, String status, String reason) {
        ImportJob job = job(provider, competitionCode, seasonYear);
        job.setStatus(status);
        job.setFinishedAt(Instant.now());
        job.setLastError(reason);
        jobs.save(job);
    }

    private ImportJob job(String provider, String competitionCode, int seasonYear) {
        return jobs.findByProviderAndJobTypeAndCompetitionCodeAndSeasonYear(
                        provider,
                        JOB_TYPE,
                        competitionCode,
                        seasonYear
                )
                .orElseGet(() -> {
                    ImportJob job = new ImportJob();
                    job.setProvider(provider);
                    job.setJobType(JOB_TYPE);
                    job.setCompetitionCode(competitionCode);
                    job.setSeasonYear(seasonYear);
                    job.setStatus("PENDING");
                    return job;
                });
    }
}
