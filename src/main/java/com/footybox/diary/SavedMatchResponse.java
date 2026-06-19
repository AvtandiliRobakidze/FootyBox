package com.footybox.diary;

import java.time.Instant;

public record SavedMatchResponse(
        Long favoriteId,
        Long matchId,
        String matchTitle,
        String competition,
        Instant utcDate,
        String homeTeamCrestUrl,
        String awayTeamCrestUrl,
        Instant savedAt
) {
    static SavedMatchResponse from(FavoriteMatch favorite) {
        var match = favorite.getMatch();
        return new SavedMatchResponse(
                favorite.getId(),
                match.getId(),
                match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName(),
                match.getCompetition().getName(),
                match.getUtcDate(),
                match.getHomeTeam().getCrestUrl(),
                match.getAwayTeam().getCrestUrl(),
                favorite.getCreatedAt()
        );
    }
}
