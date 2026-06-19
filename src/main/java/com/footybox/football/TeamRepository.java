package com.footybox.football;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findTop10ByNameContainingIgnoreCaseOrShortNameContainingIgnoreCaseOrderByNameAsc(String name, String shortName);

    Optional<Team> findByExternalProviderAndExternalId(String provider, String externalId);

    Optional<Team> findFirstByTlaIgnoreCase(String tla);

    Optional<Team> findFirstByNameIgnoreCase(String name);
}
