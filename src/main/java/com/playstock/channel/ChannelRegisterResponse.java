package com.playstock.channel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ChannelRegisterResponse {

    private final Long id;
    private final String youtubeChannelId;
    private final String name;
    private final String thumbnailUrl;
    private final ChannelCategory category;
    private final ChannelStatus status;
    private final Integer totalShares;
    private final Integer price;
    private final Integer durationMonths;
    private final BigDecimal dividendRate;
    private final LocalDateTime createdAt;

    public static ChannelRegisterResponse from(Channel channel) {
        return new ChannelRegisterResponse(
                channel.getId(),
                channel.getYoutubeChannelId(),
                channel.getName(),
                channel.getThumbnailUrl(),
                channel.getCategory(),
                channel.getStatus(),
                channel.getTotalShares(),
                channel.getPrice(),
                channel.getDurationMonths(),
                channel.getDividendRate(),
                channel.getCreatedAt()
        );
    }
}
