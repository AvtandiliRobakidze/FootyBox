package com.footybox.importjob;

import com.footybox.football.Competition;
import com.footybox.football.CompetitionRepository;
import com.footybox.football.FootballMatch;
import com.footybox.football.FootballMatchRepository;
import com.footybox.football.Season;
import com.footybox.football.SeasonRepository;
import com.footybox.football.Team;
import com.footybox.football.TeamRepository;
import com.footybox.provider.ProviderMatch;
import com.footybox.provider.ProviderTeam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FootballImportWriter {

    private final CompetitionRepository competitions;
    private final SeasonRepository seasons;
    private final TeamRepository teams;
    private final FootballMatchRepository matches;

    public FootballImportWriter(
            CompetitionRepository competitions,
            SeasonRepository seasons,
            TeamRepository teams,
            FootballMatchRepository matches
    ) {
        this.competitions = competitions;
        this.seasons = seasons;
        this.teams = teams;
        this.matches = matches;
    }

    @Transactional
    public int write(String provider, List<ProviderMatch> providerMatches) {
        Map<String, Competition> competitionCache = new HashMap<>();
        Map<String, Season> seasonCache = new HashMap<>();
        Map<String, Team> teamCache = new HashMap<>();
        int imported = 0;
        for (ProviderMatch source : providerMatches) {
            Competition competition = cachedCompetition(provider, source, competitionCache);
            Season season = cachedSeason(provider, competition, source, seasonCache);
            Team homeTeam = cachedTeam(provider, source.homeTeam(), teamCache);
            Team awayTeam = cachedTeam(provider, source.awayTeam(), teamCache);
            FootballMatch match = match(provider, source, homeTeam, awayTeam);
            match.setCompetition(competition);
            match.setSeason(season);
            match.setHomeTeam(homeTeam);
            match.setAwayTeam(awayTeam);
            match.setUtcDate(source.utcDate());
            match.setStatus(source.status());
            match.setMatchday(source.matchday());
            match.setHomeScore(source.homeScore());
            match.setAwayScore(source.awayScore());
            match.setVenue(source.venue());
            match.setExternalProvider(provider);
            match.setExternalId(source.externalId());
            match.setRawPayload(source.rawPayload());
            matches.save(match);
            imported++;
        }
        return imported;
    }

    private Competition cachedCompetition(
            String provider,
            ProviderMatch source,
            Map<String, Competition> cache
    ) {
        String key = source.competitionExternalId() == null
                ? "code:" + source.competitionCode().toUpperCase()
                : "id:" + source.competitionExternalId();
        Competition competition = cache.get(key);
        if (competition == null) {
            competition = competition(provider, source);
            cache.put(key, competition);
        }
        return competition;
    }

    private Season cachedSeason(
            String provider,
            Competition competition,
            ProviderMatch source,
            Map<String, Season> cache
    ) {
        String key = competition.getId() + ":" + source.seasonLabel();
        Season season = cache.get(key);
        if (season == null) {
            season = season(provider, competition, source);
            cache.put(key, season);
        }
        return season;
    }

    private Team cachedTeam(String provider, ProviderTeam source, Map<String, Team> cache) {
        String key = source.externalId() == null
                ? "name:" + source.name().toLowerCase()
                : "id:" + source.externalId();
        Team team = cache.get(key);
        if (team == null) {
            team = team(provider, source);
            cache.put(key, team);
        }
        return team;
    }

    private Competition competition(String provider, ProviderMatch source) {
        Optional<Competition> existing = source.competitionExternalId() == null
                ? Optional.empty()
                : competitions.findByExternalProviderAndExternalId(provider, source.competitionExternalId());
        Competition competition = existing
                .or(() -> competitions.findFirstByCodeIgnoreCase(source.competitionCode()))
                .orElseGet(Competition::new);
        competition.setName(source.competitionName());
        competition.setCode(source.competitionCode());
        competition.setCountry(source.competitionCountry());
        competition.setExternalProvider(provider);
        competition.setExternalId(source.competitionExternalId());
        return competitions.save(competition);
    }

    private Season season(String provider, Competition competition, ProviderMatch source) {
        Season season = seasons.findByCompetitionAndLabel(competition, source.seasonLabel())
                .orElseGet(Season::new);
        season.setCompetition(competition);
        season.setStartYear(source.seasonStartYear());
        season.setEndYear(source.seasonEndYear());
        season.setLabel(source.seasonLabel());
        season.setExternalProvider(provider);
        season.setExternalId(source.seasonExternalId());
        return seasons.save(season);
    }

    private Team team(String provider, ProviderTeam source) {
        Optional<Team> existing = source.externalId() == null
                ? Optional.empty()
                : teams.findByExternalProviderAndExternalId(provider, source.externalId());
        // TLA values are not globally unique across club and national teams.
        // Exact names are the safe fallback for merging pre-provider sample rows.
        Team team = existing.or(() -> teams.findFirstByNameIgnoreCase(source.name())).orElseGet(Team::new);
        team.setName(source.name());
        team.setShortName(source.shortName());
        team.setTla(source.tla());
        team.setCrestUrl(source.crestUrl());
        team.setExternalProvider(provider);
        team.setExternalId(source.externalId());
        return teams.save(team);
    }

    private FootballMatch match(String provider, ProviderMatch source, Team homeTeam, Team awayTeam) {
        Optional<FootballMatch> existing = source.externalId() == null
                ? Optional.empty()
                : matches.findByExternalProviderAndExternalId(provider, source.externalId());
        return existing.or(() -> matches.findFirstByUtcDateAndHomeTeamAndAwayTeam(
                        source.utcDate(),
                        homeTeam,
                        awayTeam
                ))
                .orElseGet(FootballMatch::new);
    }
}
