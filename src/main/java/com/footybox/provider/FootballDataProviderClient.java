package com.footybox.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@EnableConfigurationProperties(FootballDataProperties.class)
public class FootballDataProviderClient implements FootballDataProvider {

    private final FootballDataProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public FootballDataProviderClient(
            FootballDataProperties properties,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerName() {
        return "football-data";
    }

    @Override
    public List<ProviderMatch> fetchMatches(String competitionCode, int seasonYear) {
        if (properties.token() == null || properties.token().isBlank()) {
            throw new ProviderFetchException("FOOTBALL_DATA_TOKEN is not configured.", false);
        }

        for (int attempt = 1; attempt <= 3; attempt++) {
            JsonNode response;
            try {
                response = restClient.get()
                        .uri(uri -> uri.path("/competitions/{code}/matches")
                                .queryParam("season", seasonYear)
                                .build(competitionCode))
                        .header("X-Auth-Token", properties.token())
                        .retrieve()
                        .body(JsonNode.class);
            } catch (RestClientResponseException exception) {
                int status = exception.getStatusCode().value();
                if (status == 403 || status == 404) {
                    throw new ProviderFetchException(
                            "Competition " + competitionCode + " season " + seasonYear
                                    + " is unavailable to this provider account (HTTP " + status + ").",
                            true,
                            exception
                    );
                }
                if ((status == 429 || status >= 500) && attempt < 3) {
                    delay(additionalRetryDelay(exception.getResponseHeaders(), attempt));
                    continue;
                }
                throw new ProviderFetchException(
                        "Provider request failed for " + competitionCode + " " + seasonYear
                                + " (HTTP " + status + ").",
                        false,
                        exception
                );
            } catch (RuntimeException exception) {
                if (attempt < 3) {
                    delay(Math.max(0L, attempt * 1000L - properties.rateLimitDelayMs()));
                    continue;
                }
                throw new ProviderFetchException(
                        "Provider request failed for " + competitionCode + " " + seasonYear + ".",
                        false,
                        exception
                );
            } finally {
                // The free provider plan is request-rate limited. Pace every attempt,
                // including rejected seasons, so a historical probe cannot burst.
                delay(properties.rateLimitDelayMs());
            }
            return mapResponse(response, competitionCode, seasonYear);
        }
        throw new ProviderFetchException("Provider retry limit reached.", false);
    }

    private List<ProviderMatch> mapResponse(JsonNode response, String requestedCode, int requestedYear) {
        if (response == null || !response.path("matches").isArray()) {
            throw new ProviderFetchException("Provider returned an invalid matches payload.", false);
        }

        JsonNode rootCompetition = response.path("competition");
        String competitionExternalId = text(rootCompetition, "id");
        String competitionCode = valueOr(text(rootCompetition, "code"), requestedCode);
        String competitionName = valueOr(text(rootCompetition, "name"), competitionCode);
        String competitionCountry = text(rootCompetition.path("area"), "name");
        List<ProviderMatch> matches = new ArrayList<>();

        for (JsonNode match : response.path("matches")) {
            String matchExternalId = requiredText(match, "id", "match id");
            JsonNode season = match.path("season");
            Integer startYear = year(season.path("startDate").asText(null), requestedYear);
            Integer endYear = year(season.path("endDate").asText(null), startYear);
            JsonNode fullTime = match.path("score").path("fullTime");
            ProviderTeam homeTeam = team(match.path("homeTeam"), "home team", matchExternalId);
            ProviderTeam awayTeam = team(match.path("awayTeam"), "away team", matchExternalId);
            String utcDate = requiredText(match, "utcDate", "UTC date for match " + matchExternalId);
            matches.add(new ProviderMatch(
                    matchExternalId,
                    competitionExternalId,
                    competitionCode,
                    competitionName,
                    competitionCountry,
                    text(season, "id"),
                    startYear,
                    endYear,
                    seasonLabel(startYear, endYear),
                    homeTeam,
                    awayTeam,
                    parseInstant(utcDate, matchExternalId),
                    valueOr(text(match, "status"), "SCHEDULED"),
                    integer(match, "matchday"),
                    integer(fullTime, "home"),
                    integer(fullTime, "away"),
                    text(match, "venue"),
                    json(match)
            ));
        }
        return matches;
    }

    private ProviderTeam team(JsonNode node, String label, String matchExternalId) {
        String externalId = text(node, "id");
        String name = text(node, "name");
        if (externalId != null && (name == null || name.isBlank())) {
            throw new ProviderFetchException(
                    "Provider payload is missing " + label + " name for match " + matchExternalId + ".",
                    false
            );
        }
        return new ProviderTeam(
                externalId,
                valueOr(name, "To be determined"),
                text(node, "shortName"),
                text(node, "tla"),
                text(node, "crest")
        );
    }

    private Instant parseInstant(String value, String matchExternalId) {
        try {
            return Instant.parse(value);
        } catch (RuntimeException exception) {
            throw new ProviderFetchException(
                    "Provider returned an invalid UTC date for match " + matchExternalId + ".",
                    false,
                    exception
            );
        }
    }

    private String json(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            throw new ProviderFetchException("Could not preserve provider payload.", false, exception);
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private String requiredText(JsonNode node, String field, String label) {
        String value = text(node, field);
        if (value == null || value.isBlank()) {
            throw new ProviderFetchException("Provider payload is missing " + label + ".", false);
        }
        return value;
    }

    private Integer integer(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asInt();
    }

    private Integer year(String date, int fallback) {
        if (date == null || date.length() < 4) {
            return fallback;
        }
        return Integer.valueOf(date.substring(0, 4));
    }

    private String seasonLabel(int startYear, int endYear) {
        return endYear == startYear
                ? String.valueOf(startYear)
                : startYear + "/" + String.valueOf(endYear).substring(2);
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private long additionalRetryDelay(HttpHeaders headers, int attempt) {
        String retryAfter = headers == null ? null : headers.getFirst(HttpHeaders.RETRY_AFTER);
        long requestedDelay = attempt * 5000L;
        if (retryAfter != null) {
            try {
                requestedDelay = Long.parseLong(retryAfter) * 1000L;
            } catch (NumberFormatException ignored) {
                // Fall back to conservative linear backoff below.
            }
        }
        return Math.max(0L, requestedDelay - properties.rateLimitDelayMs());
    }

    private void delay(long milliseconds) {
        if (milliseconds <= 0) {
            return;
        }
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ProviderFetchException("Provider import was interrupted.", false, exception);
        }
    }
}
