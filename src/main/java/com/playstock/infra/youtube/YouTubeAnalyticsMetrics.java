package com.playstock.infra.youtube;

import java.math.BigDecimal;

public record YouTubeAnalyticsMetrics(
        long estimatedRevenue,
        BigDecimal cpm,
        BigDecimal rpm,
        long watchTimeMinutes,
        int avgViewDuration,
        long impressions,
        BigDecimal impressionCtr,
        int subscribersGained,
        int subscribersLost
) {
    public static YouTubeAnalyticsMetrics empty() {
        return new YouTubeAnalyticsMetrics(0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 0, BigDecimal.ZERO, 0, 0);
    }
}
