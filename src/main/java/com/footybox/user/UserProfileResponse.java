package com.footybox.user;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String bio,
        String avatarKey,
        String avatarUrl,
        String bannerUrl,
        FavoriteTeamResponse favoriteTeam,
        Instant createdAt
) {
    public static UserProfileResponse from(AppUser user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarKey(),
                "/api/users/" + user.getId() + "/avatar",
                "/api/users/" + user.getId() + "/banner",
                FavoriteTeamResponse.from(user.getFavoriteTeam()),
                user.getCreatedAt()
        );
    }
}
