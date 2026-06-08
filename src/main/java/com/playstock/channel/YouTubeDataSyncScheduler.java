package com.playstock.channel;

import com.playstock.infra.youtube.YouTubeCreatorChannelInfo;
import com.playstock.infra.youtube.YouTubeDataApiClient;
import com.playstock.user.User;
import com.playstock.user.UserRepository;
import com.playstock.user.oauth.YouTubeOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class YouTubeDataSyncScheduler {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final YouTubeOAuthService oAuthService;
    private final YouTubeDataApiClient youTubeDataApiClient;

    /** 매주 일요일 02:00 전체 채널 데이터 갱신 */
    @Scheduled(cron = "0 0 2 ? * SUN")
    @Transactional
    public void syncAllChannels() {
        List<User> creators = userRepository.findAllByYoutubeChannelIdIsNotNull();
        log.info("YouTube 데이터 동기화 시작: {} 명의 크리에이터", creators.size());

        int success = 0;
        int failed = 0;

        for (User creator : creators) {
            try {
                syncCreatorChannel(creator);
                success++;
            } catch (Exception e) {
                log.error("채널 동기화 실패: userId={}, channelId={}, error={}",
                        creator.getId(), creator.getYoutubeChannelId(), e.getMessage());
                failed++;
            }
        }

        log.info("YouTube 데이터 동기화 완료: 성공={}, 실패={}", success, failed);
    }

    private void syncCreatorChannel(User creator) {
        if (!creator.isYouTubeConnected()) return;

        Optional<Channel> channelOpt = channelRepository.findByYoutubeChannelId(creator.getYoutubeChannelId());
        if (channelOpt.isEmpty()) return;

        Channel channel = channelOpt.get();
        if (channel.getStatus() == ChannelStatus.EXPIRED || channel.getStatus() == ChannelStatus.SUSPENDED) return;

        String accessToken = oAuthService.getValidAccessToken(creator);
        YouTubeCreatorChannelInfo channelInfo = youTubeDataApiClient.getMyChannelInfo(accessToken);
        oAuthService.collectAndSaveMetrics(channel, accessToken, channelInfo);

        log.debug("채널 동기화 완료: channelId={}", channel.getYoutubeChannelId());
    }
}
