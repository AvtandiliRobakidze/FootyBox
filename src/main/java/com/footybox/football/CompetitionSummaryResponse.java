package com.footybox.football;

public record CompetitionSummaryResponse(
        Long id,
        String code,
        String name,
        String country,
        long matchCount
) {
}
