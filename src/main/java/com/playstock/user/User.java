package com.playstock.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "point_balance", nullable = false)
    private Long pointBalance;

    @Column(name = "google_oauth_access_token", columnDefinition = "TEXT")
    private String googleOauthAccessToken;

    @Column(name = "google_oauth_refresh_token", columnDefinition = "TEXT")
    private String googleOauthRefreshToken;

    @Column(name = "google_oauth_token_expires_at")
    private LocalDateTime googleOauthTokenExpiresAt;

    @Column(name = "youtube_channel_id", length = 100)
    private String youtubeChannelId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.pointBalance == null) {
            this.pointBalance = 0L;
        }
    }

    public static User create(String email, String nickname, String passwordHash, UserRole role) {
        User user = new User();
        user.email = email;
        user.nickname = nickname;
        user.passwordHash = passwordHash;
        user.role = role;
        return user;
    }

    public void connectYouTube(
            String encryptedAccessToken,
            String encryptedRefreshToken,
            LocalDateTime tokenExpiresAt,
            String youtubeChannelId
    ) {
        this.googleOauthAccessToken = encryptedAccessToken;
        this.googleOauthRefreshToken = encryptedRefreshToken;
        this.googleOauthTokenExpiresAt = tokenExpiresAt;
        this.youtubeChannelId = youtubeChannelId;
    }

    public void updateAccessToken(String encryptedAccessToken, LocalDateTime tokenExpiresAt) {
        this.googleOauthAccessToken = encryptedAccessToken;
        this.googleOauthTokenExpiresAt = tokenExpiresAt;
    }

    public void disconnectYouTube() {
        this.googleOauthAccessToken = null;
        this.googleOauthRefreshToken = null;
        this.googleOauthTokenExpiresAt = null;
        this.youtubeChannelId = null;
    }

    public boolean isYouTubeConnected() {
        return youtubeChannelId != null && googleOauthRefreshToken != null;
    }

    public boolean isTokenExpiringSoon() {
        return googleOauthTokenExpiresAt != null &&
               googleOauthTokenExpiresAt.isBefore(LocalDateTime.now().plusMinutes(5));
    }
}
