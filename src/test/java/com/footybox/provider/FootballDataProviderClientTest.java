package com.footybox.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class FootballDataProviderClientTest {

    @Test
    void mapsCompetitionSeasonTeamsScoresAndCrests() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FootballDataProviderClient client = new FootballDataProviderClient(
                new FootballDataProperties("https://provider.example/v4", "private-token", 0),
                builder,
                new ObjectMapper()
        );
        server.expect(requestTo("https://provider.example/v4/competitions/PL/matches?season=2023"))
                .andExpect(header("X-Auth-Token", "private-token"))
                .andRespond(withSuccess(payload(), MediaType.APPLICATION_JSON));

        List<ProviderMatch> matches = client.fetchMatches("PL", 2023);

        assertThat(matches).hasSize(1);
        ProviderMatch match = matches.get(0);
        assertThat(match.competitionCode()).isEqualTo("PL");
        assertThat(match.seasonLabel()).isEqualTo("2023/24");
        assertThat(match.homeTeam().externalId()).isEqualTo("57");
        assertThat(match.homeTeam().crestUrl()).isEqualTo("https://provider.example/arsenal.svg");
        assertThat(match.homeScore()).isEqualTo(2);
        assertThat(match.awayScore()).isEqualTo(1);
        assertThat(match.rawPayload()).contains("Arsenal FC");
        server.verify();
    }

    @Test
    void missingTokenFailsWithoutCallingProvider() {
        FootballDataProviderClient client = new FootballDataProviderClient(
                new FootballDataProperties("https://provider.example/v4", "", 0),
                RestClient.builder(),
                new ObjectMapper()
        );

        assertThatThrownBy(() -> client.fetchMatches("PL", 2023))
                .isInstanceOf(ProviderFetchException.class)
                .hasMessageContaining("FOOTBALL_DATA_TOKEN");
    }

    @Test
    void forbiddenSeasonIsClassifiedAsUnavailable() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FootballDataProviderClient client = new FootballDataProviderClient(
                new FootballDataProperties("https://provider.example/v4", "private-token", 0),
                builder,
                new ObjectMapper()
        );
        server.expect(requestTo("https://provider.example/v4/competitions/PL/matches?season=2010"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.fetchMatches("PL", 2010))
                .isInstanceOfSatisfying(ProviderFetchException.class,
                        exception -> assertThat(exception.isUnavailable()).isTrue());
        server.verify();
    }

    @Test
    void rateLimitedRequestHonorsRetryAndThenSucceeds() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FootballDataProviderClient client = client(builder);
        server.expect(requestTo("https://provider.example/v4/competitions/PL/matches?season=2023"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "0"));
        server.expect(requestTo("https://provider.example/v4/competitions/PL/matches?season=2023"))
                .andRespond(withSuccess(payload(), MediaType.APPLICATION_JSON));

        assertThat(client.fetchMatches("PL", 2023)).hasSize(1);
        server.verify();
    }

    @Test
    void serverFailureRetriesAndThenSucceeds() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FootballDataProviderClient client = client(builder);
        server.expect(requestTo("https://provider.example/v4/competitions/PL/matches?season=2023"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE).header("Retry-After", "0"));
        server.expect(requestTo("https://provider.example/v4/competitions/PL/matches?season=2023"))
                .andRespond(withSuccess(payload(), MediaType.APPLICATION_JSON));

        assertThat(client.fetchMatches("PL", 2023)).hasSize(1);
        server.verify();
    }

    @Test
    void malformedMatchFailsWithoutRetryingTheRequest() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FootballDataProviderClient client = client(builder);
        server.expect(requestTo("https://provider.example/v4/competitions/PL/matches?season=2023"))
                .andRespond(withSuccess(
                        payload().replace("2024-01-01T12:00:00Z", "not-a-date"),
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(() -> client.fetchMatches("PL", 2023))
                .isInstanceOf(ProviderFetchException.class)
                .hasMessageContaining("invalid UTC date");
        server.verify();
    }

    @Test
    void unresolvedTournamentTeamUsesHonestPlaceholder() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FootballDataProviderClient client = client(builder);
        server.expect(requestTo("https://provider.example/v4/competitions/WC/matches?season=2026"))
                .andRespond(withSuccess(
                        payload().replace(
                                "\"id\": 57, \"name\": \"Arsenal FC\"",
                                "\"id\": null, \"name\": null"
                        ),
                        MediaType.APPLICATION_JSON
                ));

        ProviderTeam team = client.fetchMatches("WC", 2026).get(0).homeTeam();

        assertThat(team.externalId()).isNull();
        assertThat(team.name()).isEqualTo("To be determined");
        server.verify();
    }

    @Test
    void emptyMatchesPayloadIsAValidSuccessfulSeason() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FootballDataProviderClient client = client(builder);
        server.expect(requestTo("https://provider.example/v4/competitions/PL/matches?season=2023"))
                .andRespond(withSuccess("{\"competition\":{\"code\":\"PL\"},\"matches\":[]}", MediaType.APPLICATION_JSON));

        assertThat(client.fetchMatches("PL", 2023)).isEmpty();
        server.verify();
    }

    private FootballDataProviderClient client(RestClient.Builder builder) {
        return new FootballDataProviderClient(
                new FootballDataProperties("https://provider.example/v4", "private-token", 0),
                builder,
                new ObjectMapper()
        );
    }

    private String payload() {
        return """
                {
                  "competition": {"id": 2021, "code": "PL", "name": "Premier League", "area": {"name": "England"}},
                  "matches": [{
                    "id": 1001,
                    "season": {"id": 2001, "startDate": "2023-08-11", "endDate": "2024-05-19"},
                    "utcDate": "2024-01-01T12:00:00Z",
                    "status": "FINISHED",
                    "matchday": 20,
                    "venue": "Test Stadium",
                    "homeTeam": {"id": 57, "name": "Arsenal FC", "shortName": "Arsenal", "tla": "ARS", "crest": "https://provider.example/arsenal.svg"},
                    "awayTeam": {"id": 64, "name": "Liverpool FC", "shortName": "Liverpool", "tla": "LIV", "crest": "https://provider.example/liverpool.svg"},
                    "score": {"fullTime": {"home": 2, "away": 1}}
                  }]
                }
                """;
    }
}
