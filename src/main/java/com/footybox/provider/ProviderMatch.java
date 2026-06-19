package com.footybox.provider;

import java.time.Instant;

public record ProviderMatch(
        String externalId,
        String competitionExternalId,
        String competitionCode,
        String competitionName,
        String competitionCountry,
        String seasonExternalId,
        Integer seasonStartYear,
        Integer seasonEndYear,
        String seasonLabel,
        ProviderTeam homeTeam,
        ProviderTeam awayTeam,
        Instant utcDate,
        String status,
        Integer matchday,
        Integer homeScore,
        Integer awayScore,
        String venue,
        String rawPayload
) {
}
