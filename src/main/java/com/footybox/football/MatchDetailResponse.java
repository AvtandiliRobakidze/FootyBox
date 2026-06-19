package com.footybox.football;

import java.time.Instant;

public record MatchDetailResponse(
        Long id,
        Long competitionId,
        String competition,
        String season,
        Long homeTeamId,
        String homeTeam,
        String homeTeamCrestUrl,
        Long awayTeamId,
        String awayTeam,
        String awayTeamCrestUrl,
        Instant utcDate,
        String status,
        Integer matchday,
        Integer homeScore,
        Integer awayScore,
        String venue,
        String provider,
        boolean spoilerFree
) {
    static MatchDetailResponse from(FootballMatch match, boolean spoilerFree) {
        return new MatchDetailResponse(
                match.getId(),
                match.getCompetition().getId(),
                match.getCompetition().getName(),
                match.getSeason() == null ? null : match.getSeason().getLabel(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getHomeTeam().getCrestUrl(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getAwayTeam().getCrestUrl(),
                match.getUtcDate(),
                match.getStatus(),
                match.getMatchday(),
                spoilerFree ? null : match.getHomeScore(),
                spoilerFree ? null : match.getAwayScore(),
                match.getVenue(),
                match.getExternalProvider(),
                spoilerFree
        );
    }
}
