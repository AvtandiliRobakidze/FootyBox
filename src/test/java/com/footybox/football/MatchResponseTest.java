package com.footybox.football;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class MatchResponseTest {

    @Test
    void summaryCarriesProviderCrestsAndHidesSpoilerScores() {
        FootballMatch match = match();

        MatchSummaryResponse response = MatchSummaryResponse.from(match, true);

        assertThat(response.homeTeamCrestUrl()).isEqualTo("https://provider.example/home.svg");
        assertThat(response.awayTeamCrestUrl()).isEqualTo("https://provider.example/away.svg");
        assertThat(response.homeScore()).isNull();
        assertThat(response.awayScore()).isNull();
    }

    @Test
    void detailCarriesProviderCrestsAndVisibleScores() {
        MatchDetailResponse response = MatchDetailResponse.from(match(), false);

        assertThat(response.homeTeamCrestUrl()).isEqualTo("https://provider.example/home.svg");
        assertThat(response.awayTeamCrestUrl()).isEqualTo("https://provider.example/away.svg");
        assertThat(response.homeScore()).isEqualTo(2);
        assertThat(response.awayScore()).isEqualTo(1);
    }

    private FootballMatch match() {
        Competition competition = new Competition();
        competition.setName("Test Competition");
        Team home = new Team();
        home.setName("Home FC");
        home.setCrestUrl("https://provider.example/home.svg");
        Team away = new Team();
        away.setName("Away FC");
        away.setCrestUrl("https://provider.example/away.svg");

        FootballMatch match = new FootballMatch();
        match.setCompetition(competition);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setUtcDate(Instant.parse("2024-01-01T12:00:00Z"));
        match.setStatus("FINISHED");
        match.setHomeScore(2);
        match.setAwayScore(1);
        match.setExternalProvider("test");
        return match;
    }
}
