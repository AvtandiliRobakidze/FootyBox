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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "match_logs")
public class MatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    private FootballMatch match;

    private Integer rating;

    @Column(name = "review_text", columnDefinition = "text")
    private String reviewText;

    @Column(name = "seen_in_stadium", nullable = false)
    private boolean seenInStadium;

    @Column(name = "contains_spoilers", nullable = false)
    private boolean containsSpoilers;

    @Column(name = "player_of_match_name", length = 160)
    private String playerOfMatchName;

    @Column(name = "watched_at", nullable = false)
    private Instant watchedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (watchedAt == null) {
            watchedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public FootballMatch getMatch() {
        return match;
    }

    public void setMatch(FootballMatch match) {
        this.match = match;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public boolean isSeenInStadium() {
        return seenInStadium;
    }

    public void setSeenInStadium(boolean seenInStadium) {
        this.seenInStadium = seenInStadium;
    }

    public boolean isContainsSpoilers() {
        return containsSpoilers;
    }

    public void setContainsSpoilers(boolean containsSpoilers) {
        this.containsSpoilers = containsSpoilers;
    }

    public String getPlayerOfMatchName() {
        return playerOfMatchName;
    }

    public void setPlayerOfMatchName(String playerOfMatchName) {
        this.playerOfMatchName = playerOfMatchName;
    }

    public Instant getWatchedAt() {
        return watchedAt;
    }

    public void setWatchedAt(Instant watchedAt) {
        this.watchedAt = watchedAt;
    }
}
