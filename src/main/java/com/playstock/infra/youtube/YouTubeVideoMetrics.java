package com.playstock.infra.youtube;

import java.time.LocalDateTime;

public record YouTubeVideoMetrics(
        long avgViewCount,
        long avgLikes,
        long avgComments,
        int uploadCount30d,
        LocalDateTime lastUploadAt
) {}
