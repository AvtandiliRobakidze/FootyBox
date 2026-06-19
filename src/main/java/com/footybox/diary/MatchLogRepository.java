package com.footybox.diary;

import com.footybox.football.FootballMatch;
import com.footybox.user.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchLogRepository extends JpaRepository<MatchLog, Long> {
    Optional<MatchLog> findByUserAndMatch(AppUser user, FootballMatch match);

    @EntityGraph(attributePaths = {"match", "match.competition", "match.homeTeam", "match.awayTeam", "user"})
    Optional<MatchLog> findByIdAndUser(Long id, AppUser user);

    @EntityGraph(attributePaths = {"match", "match.competition", "match.homeTeam", "match.awayTeam", "user"})
    List<MatchLog> findTop25ByUserOrderByWatchedAtDesc(AppUser user);

    @EntityGraph(attributePaths = {"match", "match.competition", "match.homeTeam", "match.awayTeam", "user"})
    List<MatchLog> findTop25ByMatchOrderByCreatedAtDesc(FootballMatch match);
}
