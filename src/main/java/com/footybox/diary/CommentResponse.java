package com.footybox.diary;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String username,
        String displayName,
        String body,
        Instant createdAt
) {
    static CommentResponse from(ReviewComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getUsername(),
                comment.getUser().getDisplayName(),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }
}
