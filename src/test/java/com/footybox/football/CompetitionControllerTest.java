package com.footybox.football;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompetitionControllerTest {

    @Mock
    private CompetitionRepository competitions;

    @Mock
    private FootballMatchRepository matches;

    @InjectMocks
    private CompetitionController controller;

    @Test
    void worldCupIsFirstAndEmptyCompetitionsAreExcluded() {
        Competition premierLeague = competition("PL", "Premier League");
        Competition empty = competition("EC", "European Championship");
        Competition worldCup = competition("WC", "FIFA World Cup");
        when(competitions.findAll()).thenReturn(List.of(premierLeague, empty, worldCup));
        when(matches.countByCompetition(premierLeague)).thenReturn(760L);
        when(matches.countByCompetition(empty)).thenReturn(0L);
        when(matches.countByCompetition(worldCup)).thenReturn(104L);

        List<CompetitionSummaryResponse> response = controller.competitions();

        assertThat(response).extracting(CompetitionSummaryResponse::code).containsExactly("WC", "PL");
        assertThat(response.get(0).matchCount()).isEqualTo(104L);
    }

    private Competition competition(String code, String name) {
        Competition competition = new Competition();
        competition.setCode(code);
        competition.setName(name);
        competition.setCountry("Test region");
        return competition;
    }
}
