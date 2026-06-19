package com.footybox.diary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.footybox.auth.TestJson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Sql(statements = {
            "delete from review_comments",
            "delete from favorite_matches",
            "delete from match_logs",
            "delete from football_matches",
            "delete from seasons",
            "delete from teams",
            "delete from competitions",
            "delete from app_users",
            "insert into competitions (id, name, code, country, created_at) values (1, 'UEFA Champions League', 'CL', 'Europe', current_timestamp)",
            "insert into seasons (id, competition_id, start_year, end_year, label, created_at) values (1, 1, 2023, 2024, '2023/24', current_timestamp)",
            "insert into teams (id, name, short_name, tla, country, created_at) values (1, 'Real Madrid CF', 'Real Madrid', 'RMA', 'Spain', current_timestamp)",
            "insert into teams (id, name, short_name, tla, country, created_at) values (2, 'Manchester City FC', 'Manchester City', 'MCI', 'England', current_timestamp)",
            "insert into football_matches (id, competition_id, season_id, home_team_id, away_team_id, utc_date, status, home_score, away_score, venue, external_provider, external_id, created_at, updated_at) values (1, 1, 1, 1, 2, current_timestamp, 'FINISHED', 3, 3, 'Santiago Bernabeu', 'test', 'test-match-1', current_timestamp, current_timestamp)"
    })
    void userCanLogReviewAndReadDiary() throws Exception {
        String token = registerAndToken();

        MvcResult logResult = mockMvc.perform(post("/api/diary/logs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matchId": 1,
                                  "rating": 5,
                                  "reviewText": "Instant classic.",
                                  "seenInStadium": true,
                                  "containsSpoilers": true,
                                  "playerOfMatchName": "Vinicius Junior"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.seenInStadium").value(true))
                .andExpect(jsonPath("$.containsSpoilers").value(true))
                .andReturn();
        long logId = TestJson.longField(logResult, "id");

        mockMvc.perform(post("/api/matches/1/favorite")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true));

        mockMvc.perform(get("/api/favorites/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchId").value(1));

        mockMvc.perform(get("/api/matches/1/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true))
                .andExpect(jsonPath("$.log.id").value(logId));

        mockMvc.perform(post("/api/reviews/{logId}/comments", logId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"What a night.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("What a night."));

        mockMvc.perform(get("/api/diary/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewText").value("Instant classic."));

        mockMvc.perform(get("/api/matches/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playerOfMatchName").value("Vinicius Junior"))
                .andExpect(jsonPath("$[0].comments[0].body").value("What a night."));

        mockMvc.perform(get("/api/matches/1/reviews?spoilerFree=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewHidden").value(true))
                .andExpect(jsonPath("$[0].reviewText").doesNotExist());

        String otherToken = registerAndToken("other-reviewer", "other@example.com");
        mockMvc.perform(delete("/api/diary/logs/{logId}", logId).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/diary/logs/{logId}", logId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"matchId":1,"rating":4,"reviewText":"Updated review.","seenInStadium":false,"containsSpoilers":false,"playerOfMatchName":"Jude Bellingham"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.reviewText").value("Updated review."));

        mockMvc.perform(delete("/api/diary/logs/{logId}", logId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/matches/1/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(false));
    }

    private String registerAndToken() throws Exception {
        return registerAndToken("reviewer", "reviewer@example.com");
    }

    private String registerAndToken(String username, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "password123",
                                  "displayName": "Reviewer"
                                }
                                """.formatted(username, email)))
                .andExpect(status().isOk())
                .andReturn();
        return TestJson.token(result);
    }
}
