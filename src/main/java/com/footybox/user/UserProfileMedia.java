package com.footybox.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_profile_media")
public class UserProfileMedia {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "avatar_data")
    private byte[] avatarData;

    @Column(name = "banner_data")
    private byte[] bannerData;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public void setUser(AppUser user) { this.user = user; }
    public byte[] getAvatarData() { return avatarData; }
    public void setAvatarData(byte[] avatarData) { this.avatarData = avatarData; }
    public byte[] getBannerData() { return bannerData; }
    public void setBannerData(byte[] bannerData) { this.bannerData = bannerData; }
}
