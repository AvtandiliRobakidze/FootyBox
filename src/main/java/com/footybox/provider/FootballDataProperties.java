package com.footybox.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "footybox.football-data")
public record FootballDataProperties(
        String baseUrl,
        String token,
        long rateLimitDelayMs
) {
}
