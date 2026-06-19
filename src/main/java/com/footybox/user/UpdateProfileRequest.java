package com.footybox.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 120) String displayName,
        @Size(max = 1000) String bio,
        Long favoriteTeamId,
        @Size(max = 40) String avatarKey
) {
}
