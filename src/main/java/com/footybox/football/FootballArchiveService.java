package com.footybox.football;

import com.footybox.common.NotFoundException;
import java.util.List;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FootballArchiveService {

    private static final int MAX_RESULTS = 50;

    private final FootballMatchRepository matches;

    public FootballArchiveService(FootballMatchRepository matches) {
        this.matches = matches;
    }

    @Transactional(readOnly = true)
    public List<MatchSummaryResponse> listMatches(String query, boolean spoilerFree) {
        return listMatches(query, spoilerFree, null, null, "newest");
    }

    @Transactional(readOnly = true)
    public List<MatchSummaryResponse> listMatches(
            String query,
            boolean spoilerFree,
            String competitionCode,
            Integer decade,
            String sort
    ) {
        String normalized = query == null || query.isBlank() ? "" : query.trim();
        String normalizedCompetition = competitionCode == null || competitionCode.isBlank()
                ? ""
                : competitionCode.trim();
        String normalizedSort = sort == null || sort.isBlank() ? "newest" : sort.trim().toLowerCase();
        if (!List.of("newest", "oldest").contains(normalizedSort)) {
            throw new IllegalArgumentException("Sort must be newest or oldest.");
        }
        if (decade != null && (decade < 1900 || decade > 2100 || decade % 10 != 0)) {
            throw new IllegalArgumentException("Decade must be a valid decade start year.");
        }
        Instant fromDate = decade == null
                ? Instant.parse("1900-01-01T00:00:00Z")
                : Instant.parse(decade + "-01-01T00:00:00Z");
        Instant toDate = decade == null
                ? Instant.parse("2101-01-01T00:00:00Z")
                : Instant.parse((decade + 10) + "-01-01T00:00:00Z");
        PageRequest page = PageRequest.of(0, MAX_RESULTS);
        List<FootballMatch> results = switch (normalizedSort) {
            case "oldest" -> matches.filteredOldest(normalized, normalizedCompetition, fromDate, toDate, page);
            default -> matches.filteredNewest(normalized, normalizedCompetition, fromDate, toDate, page);
        };
        return results.stream()
                .map(match -> MatchSummaryResponse.from(match, spoilerFree))
                .toList();
    }

    @Transactional(readOnly = true)
    public MatchDetailResponse match(Long id, boolean spoilerFree) {
        FootballMatch match = matches.findById(id)
                .orElseThrow(() -> new NotFoundException("Match not found."));
        match.getCompetition().getName();
        match.getHomeTeam().getName();
        match.getAwayTeam().getName();
        return MatchDetailResponse.from(match, spoilerFree);
    }
}
