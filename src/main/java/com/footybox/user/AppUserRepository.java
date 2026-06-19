package com.footybox.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    @EntityGraph(attributePaths = "favoriteTeam")
    Optional<AppUser> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "favoriteTeam")
    Optional<AppUser> findByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);
}
