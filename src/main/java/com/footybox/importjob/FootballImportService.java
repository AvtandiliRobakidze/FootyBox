package com.footybox.importjob;

import com.footybox.provider.FootballDataProvider;
import com.footybox.provider.ProviderFetchException;
import com.footybox.provider.ProviderMatch;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FootballImportService {

    private static final Logger log = LoggerFactory.getLogger(FootballImportService.class);

    private final FootballDataProvider provider;
    private final FootballImportWriter writer;
    private final ImportJobTracker jobs;

    public FootballImportService(
            FootballDataProvider provider,
            FootballImportWriter writer,
            ImportJobTracker jobs
    ) {
        this.provider = provider;
        this.writer = writer;
        this.jobs = jobs;
    }

    public ImportSeasonResult importSeason(String competitionCode, int seasonYear) {
        String providerName = provider.providerName();
        jobs.started(providerName, competitionCode, seasonYear);
        try {
            List<ProviderMatch> providerMatches = provider.fetchMatches(competitionCode, seasonYear);
            int imported = writer.write(providerName, providerMatches);
            jobs.succeeded(providerName, competitionCode, seasonYear, imported);
            return new ImportSeasonResult("SUCCEEDED", imported, null);
        } catch (ProviderFetchException exception) {
            if (exception.isUnavailable()) {
                jobs.skipped(providerName, competitionCode, seasonYear, exception.getMessage());
                log.info("Skipped {} {}: {}", competitionCode, seasonYear, exception.getMessage());
                return new ImportSeasonResult("SKIPPED", 0, exception.getMessage());
            }
            jobs.failed(providerName, competitionCode, seasonYear, exception.getMessage());
            log.warn("Failed {} {}: {}", competitionCode, seasonYear, exception.getMessage());
            return new ImportSeasonResult("FAILED", 0, exception.getMessage());
        } catch (RuntimeException exception) {
            jobs.failed(providerName, competitionCode, seasonYear, exception.getMessage());
            log.error("Failed {} {}", competitionCode, seasonYear, exception);
            return new ImportSeasonResult("FAILED", 0, exception.getMessage());
        }
    }

    public record ImportSeasonResult(String status, int imported, String message) {
    }
}
