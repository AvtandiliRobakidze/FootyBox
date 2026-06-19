package com.footybox.diary;

import java.time.Instant;
import java.util.List;

public record MatchLogResponse(
        Long id,
        Long matchId,
        String matchTitle,
        String competition,
        String username,
        String displayName,
        String avatarKey,
        String avatarUrl,
        Integer rating,
        String reviewText,
        boolean reviewHidden,
        boolean seenInStadium,
        boolean containsSpoilers,
        String playerOfMatchName,
        Instant watchedAt,
        List<CommentResponse> comments
) {
    static MatchLogResponse from(MatchLog log, List<CommentResponse> comments) {
        return from(log, comments, false);
    }

    static MatchLogResponse from(MatchLog log, List<CommentResponse> comments, boolean spoilerFree) {
        String matchTitle = log.getMatch().getHomeTeam().getName() + " vs " + log.getMatch().getAwayTeam().getName();
        boolean hidden = spoilerFree && log.isContainsSpoilers() && log.getReviewText() != null;
        return new MatchLogResponse(
                log.getId(),
                log.getMatch().getId(),
                matchTitle,
                log.getMatch().getCompetition().getName(),
                log.getUser().getUsername(),
                log.getUser().getDisplayName(),
                log.getUser().getAvatarKey(),
                "/api/users/" + log.getUser().getId() + "/avatar",
                log.getRating(),
                hidden ? null : log.getReviewText(),
                hidden,
                log.isSeenInStadium(),
                log.isContainsSpoilers(),
                log.getPlayerOfMatchName(),
                log.getWatchedAt(),
                comments
        );
    }
}
