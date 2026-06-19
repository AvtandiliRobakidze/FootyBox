package com.footybox.football;

import java.time.Instant;

public record MatchSummaryResponse(
        Long id,
        String competition,
        String season,
        String homeTeam,
        String homeTeamCrestUrl,
        String awayTeam,
        String awayTeamCrestUrl,
        Instant utcDate,
        String status,
        Integer homeScore,
        Integer awayScore,
        String venue,
        boolean spoilerFree
) {
    static MatchSummaryResponse from(FootballMatch match, boolean spoilerFree) {
        return new MatchSummaryResponse(
                match.getId(),
                match.getCompetition().getName(),
                match.getSeason() == null ? null : match.getSeason().getLabel(),
                match.getHomeTeam().getName(),
                match.getHomeTeam().getCrestUrl(),
                match.getAwayTeam().getName(),
                match.getAwayTeam().getCrestUrl(),
                match.getUtcDate(),
                match.getStatus(),
                spoilerFree ? null : match.getHomeScore(),
                spoilerFree ? null : match.getAwayScore(),
                match.getVenue(),
                spoilerFree
        );
    }
}
