package com.footybox.diary;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;

public record LogMatchRequest(
        @NotNull Long matchId,
        @Min(1) @Max(5) Integer rating,
        @Size(max = 4000) String reviewText,
        Boolean seenInStadium,
        Boolean containsSpoilers,
        @Size(max = 160) String playerOfMatchName,
        @PastOrPresent Instant watchedAt
) {
}
