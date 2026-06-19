package com.footybox.diary;

import com.footybox.football.FootballMatch;
import com.footybox.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "favorite_matches")
public class FavoriteMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    private FootballMatch match;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public void setMatch(FootballMatch match) {
        this.match = match;
    }

    public Long getId() {
        return id;
    }

    public FootballMatch getMatch() {
        return match;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
