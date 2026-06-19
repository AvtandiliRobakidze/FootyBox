package com.footybox.football;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FootballMatchRepository extends JpaRepository<FootballMatch, Long> {

    Optional<FootballMatch> findByExternalProviderAndExternalId(String provider, String externalId);

    Optional<FootballMatch> findFirstByUtcDateAndHomeTeamAndAwayTeam(
            Instant utcDate,
            Team homeTeam,
            Team awayTeam
    );

    @EntityGraph(attributePaths = {"competition", "season", "homeTeam", "awayTeam"})
    @Query("""
            select m from FootballMatch m
            where :query is null
               or lower(m.homeTeam.name) like lower(concat('%', :query, '%'))
               or lower(m.awayTeam.name) like lower(concat('%', :query, '%'))
               or lower(m.competition.name) like lower(concat('%', :query, '%'))
            order by m.utcDate desc
            """)
    List<FootballMatch> search(@Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"competition", "season", "homeTeam", "awayTeam"})
    @Query("select m from FootballMatch m order by m.utcDate desc")
    List<FootballMatch> latest(Pageable pageable);

    long countByCompetition(Competition competition);

    @EntityGraph(attributePaths = {"competition", "season", "homeTeam", "awayTeam"})
    @Query("""
            select m from FootballMatch m
            where (:query = ''
                   or lower(m.homeTeam.name) like lower(concat('%', :query, '%'))
                   or lower(m.awayTeam.name) like lower(concat('%', :query, '%'))
                   or lower(m.competition.name) like lower(concat('%', :query, '%')))
              and (:competitionCode = '' or lower(m.competition.code) = lower(:competitionCode))
              and m.utcDate >= :fromDate
              and m.utcDate < :toDate
            order by m.utcDate desc
            """)
    List<FootballMatch> filteredNewest(
            @Param("query") String query,
            @Param("competitionCode") String competitionCode,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"competition", "season", "homeTeam", "awayTeam"})
    @Query("""
            select m from FootballMatch m
            where (:query = ''
                   or lower(m.homeTeam.name) like lower(concat('%', :query, '%'))
                   or lower(m.awayTeam.name) like lower(concat('%', :query, '%'))
                   or lower(m.competition.name) like lower(concat('%', :query, '%')))
              and (:competitionCode = '' or lower(m.competition.code) = lower(:competitionCode))
              and m.utcDate >= :fromDate
              and m.utcDate < :toDate
            order by m.utcDate asc
            """)
    List<FootballMatch> filteredOldest(
            @Param("query") String query,
            @Param("competitionCode") String competitionCode,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );
}
