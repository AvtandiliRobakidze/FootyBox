package com.footybox.importjob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "footybox.import", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(FootballImportProperties.class)
public class FootballImportRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FootballImportRunner.class);

    private final FootballImportProperties properties;
    private final FootballImportService imports;
    private final ConfigurableApplicationContext context;

    public FootballImportRunner(
            FootballImportProperties properties,
            FootballImportService imports,
            ConfigurableApplicationContext context
    ) {
        this.properties = properties;
        this.imports = imports;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.getFromYear() > properties.getToYear()) {
            throw new IllegalArgumentException("footybox.import.from-year must not exceed to-year.");
        }
        log.info(
                "Starting football import for {} from {} through {}.",
                properties.getCompetitionCodes(),
                properties.getFromYear(),
                properties.getToYear()
        );
        for (String rawCode : properties.getCompetitionCodes()) {
            String code = rawCode.trim().toUpperCase();
            if (code.isBlank()) {
                continue;
            }
            for (int year = properties.getFromYear(); year <= properties.getToYear(); year++) {
                FootballImportService.ImportSeasonResult result = imports.importSeason(code, year);
                log.info("Import {} {}: {}, {} matches.", code, year, result.status(), result.imported());
            }
        }
        log.info("Football import finished.");
        if (properties.isExitAfterRun()) {
            int exitCode = SpringApplication.exit(context, () -> 0);
            System.exit(exitCode);
        }
    }
}
