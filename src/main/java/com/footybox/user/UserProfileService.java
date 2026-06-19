package com.footybox.user;

import com.footybox.common.NotFoundException;
import com.footybox.football.Team;
import com.footybox.football.TeamRepository;
import com.footybox.security.CurrentUserService;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    public static final Set<String> AVATAR_KEYS = Set.of("pitch", "trophy", "floodlights", "classic");

    private final CurrentUserService currentUserService;
    private final AppUserRepository users;
    private final TeamRepository teams;

    public UserProfileService(CurrentUserService currentUserService, AppUserRepository users, TeamRepository teams) {
        this.currentUserService = currentUserService;
        this.users = users;
        this.teams = teams;
    }

    @Transactional
    public UserProfileResponse update(UpdateProfileRequest request) {
        AppUser user = currentUserService.currentUser();
        String avatarKey = blankToNull(request.avatarKey());
        if (avatarKey != null && !AVATAR_KEYS.contains(avatarKey)) {
            throw new IllegalArgumentException("Avatar selection is invalid.");
        }
        Team favoriteTeam = request.favoriteTeamId() == null
                ? null
                : teams.findById(request.favoriteTeamId())
                        .orElseThrow(() -> new NotFoundException("Favourite team not found."));

        user.setDisplayName(request.displayName().trim());
        user.setBio(blankToNull(request.bio()));
        user.setAvatarKey(avatarKey);
        user.setFavoriteTeam(favoriteTeam);
        return UserProfileResponse.from(users.save(user));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
