package com.footybox.football;

import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/competitions")
public class CompetitionController {

    private final CompetitionRepository competitions;
    private final FootballMatchRepository matches;

    public CompetitionController(CompetitionRepository competitions, FootballMatchRepository matches) {
        this.competitions = competitions;
        this.matches = matches;
    }

    @GetMapping
    List<CompetitionSummaryResponse> competitions() {
        return competitions.findAll().stream()
                .map(competition -> new CompetitionSummaryResponse(
                        competition.getId(),
                        competition.getCode(),
                        competition.getName(),
                        competition.getCountry(),
                        matches.countByCompetition(competition)))
                .filter(competition -> competition.matchCount() > 0)
                .sorted(Comparator
                        .comparing((CompetitionSummaryResponse competition) -> !"WC".equalsIgnoreCase(competition.code()))
                        .thenComparing(CompetitionSummaryResponse::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
