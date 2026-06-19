package com.footybox.football;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    List<Competition> findTop10ByNameContainingIgnoreCaseOrderByNameAsc(String name);

    Optional<Competition> findByExternalProviderAndExternalId(String provider, String externalId);

    Optional<Competition> findFirstByCodeIgnoreCase(String code);
}
