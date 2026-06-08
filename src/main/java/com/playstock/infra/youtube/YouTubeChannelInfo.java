package com.playstock.infra.youtube;

public record YouTubeChannelInfo(
        String channelId,
        String name,
        String thumbnailUrl,
        Long subscriberCount
) {}
