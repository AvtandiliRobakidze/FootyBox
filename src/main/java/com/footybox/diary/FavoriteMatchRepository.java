package com.footybox.diary;

import com.footybox.football.FootballMatch;
import com.footybox.user.AppUser;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteMatchRepository extends JpaRepository<FavoriteMatch, Long> {
    boolean existsByUserAndMatch(AppUser user, FootballMatch match);

    Optional<FavoriteMatch> findByUserAndMatch(AppUser user, FootballMatch match);

    @EntityGraph(attributePaths = {"match", "match.competition", "match.homeTeam", "match.awayTeam"})
    List<FavoriteMatch> findByUserOrderByCreatedAtDesc(AppUser user);
}
