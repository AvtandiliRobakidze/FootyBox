package com.footybox.user;

import com.footybox.football.Team;

public record FavoriteTeamResponse(Long id, String name, String crestUrl) {
    static FavoriteTeamResponse from(Team team) {
        return team == null ? null : new FavoriteTeamResponse(team.getId(), team.getName(), team.getCrestUrl());
    }
}
