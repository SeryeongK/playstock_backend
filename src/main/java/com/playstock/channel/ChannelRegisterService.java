package com.playstock.channel;

import com.playstock.common.exception.ErrorCode;
import com.playstock.common.exception.PlaystockException;
import com.playstock.infra.youtube.YouTubeChannelInfo;
import com.playstock.infra.youtube.YouTubeClient;
import com.playstock.user.User;
import com.playstock.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChannelRegisterService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final YouTubeClient youTubeClient;

    @Transactional
    public ChannelRegisterResponse register(Long creatorId, ChannelRegisterRequest request) {
        if (channelRepository.existsByYoutubeChannelId(request.getYoutubeChannelId())) {
            throw new PlaystockException(ErrorCode.DUPLICATE_CHANNEL);
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new PlaystockException(ErrorCode.USER_NOT_FOUND));

        YouTubeChannelInfo info = youTubeClient.getChannelInfo(request.getYoutubeChannelId());

        Channel channel = Channel.create(
                info.channelId(),
                creator,
                info.name(),
                request.getCategory(),
                info.thumbnailUrl(),
                request.getTotalShares(),
                request.getPrice(),
                request.getDurationMonths(),
                request.getDividendRate()
        );

        channelRepository.save(channel);

        return ChannelRegisterResponse.from(channel);
    }
}
