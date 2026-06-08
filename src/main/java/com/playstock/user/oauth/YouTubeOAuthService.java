package com.playstock.user.oauth;

import com.playstock.channel.Channel;
import com.playstock.channel.ChannelMetrics;
import com.playstock.channel.ChannelMetricsRepository;
import com.playstock.channel.ChannelRepository;
import com.playstock.common.exception.ErrorCode;
import com.playstock.common.exception.PlaystockException;
import com.playstock.infra.google.GoogleOAuthService;
import com.playstock.infra.google.GoogleOAuthService.OAuthTokens;
import com.playstock.infra.google.TokenEncryptionService;
import com.playstock.infra.youtube.YouTubeAnalyticsClient;
import com.playstock.infra.youtube.YouTubeAnalyticsMetrics;
import com.playstock.infra.youtube.YouTubeCreatorChannelInfo;
import com.playstock.infra.youtube.YouTubeDataApiClient;
import com.playstock.infra.youtube.YouTubeVideoMetrics;
import com.playstock.user.User;
import com.playstock.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeOAuthService {

    private final GoogleOAuthService googleOAuthService;
    private final TokenEncryptionService encryptionService;
    private final YouTubeDataApiClient youTubeDataApiClient;
    private final YouTubeAnalyticsClient analyticsClient;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMetricsRepository channelMetricsRepository;

    public OAuthConnectUrlResponse getConnectUrl(Long userId) {
        String authUrl = googleOAuthService.buildAuthorizationUrl(userId);
        return OAuthConnectUrlResponse.of(authUrl);
    }

    @Transactional
    public void processCallback(String code, String state) {
        Long userId = googleOAuthService.validateState(state);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PlaystockException(ErrorCode.USER_NOT_FOUND));

        OAuthTokens tokens = googleOAuthService.exchangeCodeForTokens(code);

        // 채널 기본 정보 수집 (mine=true)
        YouTubeCreatorChannelInfo channelInfo = youTubeDataApiClient.getMyChannelInfo(tokens.accessToken());

        // 토큰 암호화 후 저장
        String encryptedAccess = encryptionService.encrypt(tokens.accessToken());
        String encryptedRefresh = encryptionService.encrypt(tokens.refreshToken());
        user.connectYouTube(encryptedAccess, encryptedRefresh, tokens.expiresAt(), channelInfo.channelId());

        // 이미 등록된 채널이 있다면 초기 metrics 수집
        Optional<Channel> channel = channelRepository.findByYoutubeChannelId(channelInfo.channelId());
        channel.ifPresent(ch -> collectAndSaveMetrics(ch, tokens.accessToken(), channelInfo));

        log.info("YouTube OAuth 연동 완료: userId={}, channelId={}", userId, channelInfo.channelId());
    }

    public OAuthStatusResponse getStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PlaystockException(ErrorCode.USER_NOT_FOUND));

        if (!user.isYouTubeConnected()) {
            return OAuthStatusResponse.notConnected();
        }

        // 채널명은 channels 테이블에서 조회 (없으면 channelId 그대로 반환)
        String channelName = channelRepository.findByYoutubeChannelId(user.getYoutubeChannelId())
                .map(Channel::getName)
                .orElse(user.getYoutubeChannelId());

        return OAuthStatusResponse.connected(user, channelName);
    }

    @Transactional
    public void disconnect(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PlaystockException(ErrorCode.USER_NOT_FOUND));

        if (!user.isYouTubeConnected()) {
            throw new PlaystockException(ErrorCode.OAUTH_NOT_CONNECTED);
        }

        user.disconnectYouTube();
        log.info("YouTube OAuth 연동 해제: userId={}", userId);
    }

    /** 유효한 access_token 반환 (만료 임박 시 자동 갱신) */
    public String getValidAccessToken(User user) {
        if (!user.isYouTubeConnected()) {
            throw new PlaystockException(ErrorCode.OAUTH_NOT_CONNECTED);
        }

        if (user.isTokenExpiringSoon()) {
            OAuthTokens refreshed = googleOAuthService.refreshAccessToken(user.getGoogleOauthRefreshToken());
            user.updateAccessToken(encryptionService.encrypt(refreshed.accessToken()), refreshed.expiresAt());
            return refreshed.accessToken();
        }

        return encryptionService.decrypt(user.getGoogleOauthAccessToken());
    }

    public void collectAndSaveMetrics(Channel channel, String accessToken, YouTubeCreatorChannelInfo channelInfo) {
        YouTubeVideoMetrics videoMetrics = youTubeDataApiClient.getVideoMetrics(channelInfo.uploadsPlaylistId());
        YouTubeAnalyticsMetrics analytics = analyticsClient.getMetrics(accessToken);

        ChannelMetrics metrics = ChannelMetrics.create(
                channel,
                channelInfo.subscriberCount(),
                videoMetrics.avgViewCount(),
                videoMetrics.avgLikes(),
                videoMetrics.avgComments(),
                videoMetrics.uploadCount30d(),
                videoMetrics.lastUploadAt(),
                analytics.estimatedRevenue(),
                analytics.cpm(),
                analytics.rpm(),
                analytics.watchTimeMinutes(),
                analytics.avgViewDuration(),
                analytics.impressions(),
                analytics.impressionCtr(),
                analytics.subscribersGained(),
                analytics.subscribersLost()
        );

        channelMetricsRepository.save(metrics);
    }
}
