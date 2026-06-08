package com.playstock.infra.youtube;

public record YouTubeCreatorChannelInfo(
        String channelId,
        String name,
        String thumbnailUrl,
        long subscriberCount,
        String uploadsPlaylistId
) {}
