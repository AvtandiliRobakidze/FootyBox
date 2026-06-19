package com.footybox.importjob;

import static org.assertj.core.api.Assertions.assertThat;

import com.footybox.football.Competition;
import com.footybox.football.CompetitionRepository;
import com.footybox.football.FootballMatch;
import com.footybox.football.FootballMatchRepository;
import com.footybox.football.SeasonRepository;
import com.footybox.football.Team;
import com.footybox.football.TeamRepository;
import com.footybox.provider.ProviderMatch;
import com.footybox.provider.ProviderTeam;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(FootballImportWriter.class)
@ActiveProfiles("test")
class FootballImportWriterTest {

    @Autowired
    private FootballImportWriter writer;

    @Autowired
    private CompetitionRepository competitions;

    @Autowired
    private SeasonRepository seasons;

    @Autowired
    private TeamRepository teams;

    @Autowired
    private FootballMatchRepository matches;

    @Test
    void repeatedImportMergesSampleRecordsAndUpdatesCrests() {
        Competition competition = new Competition();
        competition.setName("Premier League");
        competition.setCode("PL");
        competition.setExternalProvider("sample");
        competition.setExternalId("sample-pl");
        competition = competitions.save(competition);
        Team home = sampleTeam("Arsenal FC", "ARS");
        Team away = sampleTeam("Liverpool FC", "LIV");
        FootballMatch sample = new FootballMatch();
        sample.setCompetition(competition);
        sample.setHomeTeam(home);
        sample.setAwayTeam(away);
        sample.setUtcDate(Instant.parse("2024-01-01T12:00:00Z"));
        sample.setStatus("FINISHED");
        sample.setExternalProvider("sample");
        sample.setExternalId("sample-match");
        sample = matches.save(sample);
        Long preservedMatchId = sample.getId();

        ProviderMatch providerMatch = providerMatch();
        writer.write("football-data", List.of(providerMatch));
        writer.write("football-data", List.of(providerMatch));

        assertThat(competitions.count()).isEqualTo(1);
        assertThat(seasons.count()).isEqualTo(1);
        assertThat(teams.count()).isEqualTo(2);
        assertThat(matches.count()).isEqualTo(1);
        FootballMatch importedMatch = matches.findAll().get(0);
        assertThat(importedMatch.getId()).isEqualTo(preservedMatchId);
        assertThat(importedMatch.getExternalProvider()).isEqualTo("football-data");
        assertThat(importedMatch.getExternalId()).isEqualTo("1001");
        assertThat(importedMatch.getHomeScore()).isEqualTo(2);
        assertThat(importedMatch.getAwayScore()).isEqualTo(1);
        Team importedHome = teams.findFirstByTlaIgnoreCase("ARS").orElseThrow();
        assertThat(importedHome.getCrestUrl()).isEqualTo("https://provider.example/arsenal.svg");
        assertThat(importedHome.getExternalProvider()).isEqualTo("football-data");
    }

    private Team sampleTeam(String name, String tla) {
        Team team = new Team();
        team.setName(name);
        team.setShortName(name.replace(" FC", ""));
        team.setTla(tla);
        team.setExternalProvider("sample");
        team.setExternalId("sample-" + tla.toLowerCase());
        return teams.save(team);
    }

    private ProviderMatch providerMatch() {
        return new ProviderMatch(
                "1001",
                "2021",
                "PL",
                "Premier League",
                "England",
                "2001",
                2023,
                2024,
                "2023/24",
                new ProviderTeam("57", "Arsenal FC", "Arsenal", "ARS", "https://provider.example/arsenal.svg"),
                new ProviderTeam("64", "Liverpool FC", "Liverpool", "LIV", "https://provider.example/liverpool.svg"),
                Instant.parse("2024-01-01T12:00:00Z"),
                "FINISHED",
                20,
                2,
                1,
                "Test Stadium",
                "{\"id\":1001}"
        );
    }
}
