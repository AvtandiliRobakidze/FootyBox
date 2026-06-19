package com.footybox;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StaticResourceAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicPagesAndSharedDesignAssetsLoad() throws Exception {
        String[] paths = {
                "/",
                "/login.html",
                "/register.html",
                "/archive.html",
                "/matches.html",
                "/profile.html",
                "/discover.html",
                "/lists.html",
                "/list.html",
                "/team.html",
                "/player.html",
                "/competition.html",
                "/css/app.css",
                "/css/tokens.css",
                "/css/components.css",
                "/js/app.js",
                "/js/shell.js",
                "/js/platform.js",
                "/assets/asset-manifest.json",
                "/assets/placeholders/match-night.webp",
                "/assets/placeholders/stadium-lights.webp",
                "/assets/placeholders/archive-wall.webp",
                "/assets/editorial/home-messi-world-cup-2022.webp",
                "/assets/editorial/world-cup-2026.webp",
                "/assets/editorial/world-cup-supporters.webp",
                "/assets/placeholders/match-poster.webp",
                "/assets/avatars/pitch.webp",
                "/assets/avatars/trophy.webp",
                "/assets/brand/footybox-logo.png",
                "/assets/img/footybox-logo.png",
                "/assets/css/styles.css",
                "/assets/js/ui.js"
        };

        for (String path : paths) {
            mockMvc.perform(get(path))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void platformNavigationExistsOnCorePages() throws Exception {
        String[] pages = {"/index.html", "/archive.html", "/matches.html", "/profile.html", "/discover.html", "/lists.html"};

        for (String page : pages) {
            mockMvc.perform(get(page))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("assets/js/ui.js")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("/js/app.js")));
        }

        mockMvc.perform(get("/assets/js/ui.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/discover.html")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/archive.html")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/lists.html")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("footybox.token")));
    }

    @Test
    void routeAliasesStayLightweightAndHonest() throws Exception {
        mockMvc.perform(get("/archive.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Search every match")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-match-list")));

        mockMvc.perform(get("/register.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-register-form")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Start your diary")));
    }

    @Test
    void loginPageKeepsAuthFormHooks() throws Exception {
        mockMvc.perform(get("/login.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-login-form")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-auth-message")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/register.html")));
    }

    @Test
    void archivePageKeepsSearchFilterAndSpoilerHooks() throws Exception {
        mockMvc.perform(get("/archive.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Search every match")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-search-form")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-match-query")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-spoiler-free")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-archive-decade")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-archive-competition")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-competition-list")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("World Cup 2026")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-archive-sort")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-archive-count")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-match-list")));
    }

    @Test
    void matchDetailPageKeepsDetailStateHooks() throws Exception {
        mockMvc.perform(get("/matches.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-detail-spoiler-free")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-match-detail")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-review-list")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-diary-state")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-community-rating")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-favourite-state")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-open-log")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-favorite")));
    }

    @Test
    void logDialogKeepsDiaryFormHooks() throws Exception {
        mockMvc.perform(get("/matches.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-log-dialog")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-log-form")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"watchedAt\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"rating\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"reviewText\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"seenInStadium\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"playerOfMatchName\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"containsSpoilers\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-log-submit")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-log-message")));
    }

    @Test
    void platformScaffoldPagesExposeExpectedHooks() throws Exception {
        mockMvc.perform(get("/discover.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Spoiler-free discovery")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("filter-chip")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-discover-list")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-nav=\"discover\"")));

        mockMvc.perform(get("/lists.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Football paths worth exploring")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-list-filter")));

        mockMvc.perform(get("/list.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sample list detail")));

        mockMvc.perform(get("/team.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Real Madrid CF")));

        mockMvc.perform(get("/player.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Luka Modri")));

        mockMvc.perform(get("/competition.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("UEFA Champions League")));
    }
}
