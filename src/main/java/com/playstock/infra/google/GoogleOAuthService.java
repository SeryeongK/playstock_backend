package com.playstock.infra.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.playstock.common.exception.ErrorCode;
import com.playstock.common.exception.PlaystockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String YOUTUBE_READONLY = "https://www.googleapis.com/auth/youtube.readonly";
    private static final String ANALYTICS_READONLY = "https://www.googleapis.com/auth/yt-analytics.readonly";

    private final GoogleOAuthProperties properties;
    private final TokenEncryptionService encryptionService;
    private final RestClient restClient = RestClient.create();

    // CSRF state 관리 (단일 인스턴스 MVP)
    private final Map<String, Long> pendingStates = new ConcurrentHashMap<>();

    public String buildAuthorizationUrl(Long userId) {
        String state = UUID.randomUUID().toString();
        pendingStates.put(state, userId);

        return UriComponentsBuilder.fromUriString(AUTH_ENDPOINT)
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", YOUTUBE_READONLY + " " + ANALYTICS_READONLY)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    public Long validateState(String state) {
        Long userId = pendingStates.remove(state);
        if (userId == null) {
            throw new PlaystockException(ErrorCode.OAUTH_INVALID_STATE);
        }
        return userId;
    }

    public OAuthTokens exchangeCodeForTokens(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("redirect_uri", properties.getRedirectUri());
        form.add("grant_type", "authorization_code");

        try {
            GoogleTokenResponse response = restClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(form)
                    .retrieve()
                    .body(GoogleTokenResponse.class);

            return toOAuthTokens(response);
        } catch (PlaystockException e) {
            throw e;
        } catch (Exception e) {
            throw new PlaystockException(ErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED);
        }
    }

    public OAuthTokens refreshAccessToken(String encryptedRefreshToken) {
        String refreshToken = encryptionService.decrypt(encryptedRefreshToken);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("refresh_token", refreshToken);
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("grant_type", "refresh_token");

        try {
            GoogleTokenResponse response = restClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(form)
                    .retrieve()
                    .body(GoogleTokenResponse.class);

            // 갱신 응답에는 refresh_token이 없으므로 기존 것 유지
            return new OAuthTokens(response.accessToken(), null, LocalDateTime.now().plusSeconds(response.expiresIn()));
        } catch (Exception e) {
            throw new PlaystockException(ErrorCode.OAUTH_TOKEN_REFRESH_FAILED);
        }
    }

    private OAuthTokens toOAuthTokens(GoogleTokenResponse response) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(response.expiresIn());
        return new OAuthTokens(response.accessToken(), response.refreshToken(), expiresAt);
    }

    public record OAuthTokens(String accessToken, String refreshToken, LocalDateTime expiresAt) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") long expiresIn
    ) {}
}
