package com.footybox.search;

import com.footybox.football.CompetitionRepository;
import com.footybox.football.TeamRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchService {

    private final TeamRepository teams;
    private final CompetitionRepository competitions;

    public SearchService(TeamRepository teams, CompetitionRepository competitions) {
        this.teams = teams;
        this.competitions = competitions;
    }

    @Transactional(readOnly = true)
    public List<SearchResultResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String trimmed = query.trim();
        List<SearchResultResponse> results = new ArrayList<>();

        teams.findTop10ByNameContainingIgnoreCaseOrShortNameContainingIgnoreCaseOrderByNameAsc(trimmed, trimmed)
                .forEach(team -> results.add(new SearchResultResponse(
                        "team",
                        team.getId(),
                        team.getName(),
                        team.getCountry()
                )));

        competitions.findTop10ByNameContainingIgnoreCaseOrderByNameAsc(trimmed)
                .forEach(competition -> results.add(new SearchResultResponse(
                        "competition",
                        competition.getId(),
                        competition.getName(),
                        competition.getCountry()
                )));

        return results;
    }
}
