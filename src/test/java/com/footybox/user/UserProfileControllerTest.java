package com.footybox.user;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserProfileControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    @Sql(statements = {
            "delete from review_comments",
            "delete from favorite_matches",
            "delete from match_logs",
            "delete from football_matches",
            "delete from seasons",
            "delete from app_users",
            "delete from teams",
            "delete from competitions",
            "insert into teams (id, name, created_at) values (41, 'Liverpool FC', current_timestamp)"
    })
    void profileFieldsPersistAndAvatarIsValidated() throws Exception {
        var registered = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"profileuser\",\"email\":\"profile@example.com\",\"password\":\"password123\",\"displayName\":\"Profile User\"}"))
                .andExpect(status().isOk()).andReturn();
        String token = TestJson.token(registered);

        mockMvc.perform(put("/api/users/me/profile").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"New Name\",\"bio\":\"My football diary\",\"favoriteTeamId\":41,\"avatarKey\":\"trophy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("New Name"))
                .andExpect(jsonPath("$.avatarKey").value("trophy"))
                .andExpect(jsonPath("$.favoriteTeam.name").value("Liverpool FC"));

        mockMvc.perform(put("/api/users/me/profile").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"New Name\",\"avatarKey\":\"../../bad\"}"))
                .andExpect(status().isBadRequest());
    }
}
