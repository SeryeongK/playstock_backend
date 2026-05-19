package com.playstock.channel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "channel_metrics")
@Getter
@NoArgsConstructor
public class ChannelMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    // YouTube Data API (API 키로 수집)
    @Column(name = "subscriber_count")
    private Long subscriberCount;

    @Column(name = "avg_view_count")
    private Long avgViewCount;

    @Column(name = "avg_likes")
    private Long avgLikes;

    @Column(name = "avg_comments")
    private Long avgComments;

    @Column(name = "upload_count_30d")
    private Integer uploadCount30d;

    @Column(name = "last_upload_at")
    private LocalDateTime lastUploadAt;

    // YouTube Analytics API (OAuth 필요)
    @Column(name = "estimated_revenue")
    private Long estimatedRevenue;

    @Column(name = "cpm", precision = 10, scale = 4)
    private BigDecimal cpm;

    @Column(name = "rpm", precision = 10, scale = 4)
    private BigDecimal rpm;

    @Column(name = "watch_time_minutes")
    private Long watchTimeMinutes;

    @Column(name = "avg_view_duration")
    private Integer avgViewDuration;

    @Column(name = "impressions")
    private Long impressions;

    @Column(name = "impression_ctr", precision = 10, scale = 4)
    private BigDecimal impressionCtr;

    @Column(name = "subscribers_gained")
    private Integer subscribersGained;

    @Column(name = "subscribers_lost")
    private Integer subscribersLost;

    @Column(name = "snapshot_at", nullable = false)
    private LocalDateTime snapshotAt;

    @PrePersist
    private void prePersist() {
        this.snapshotAt = LocalDateTime.now();
    }

    public static ChannelMetrics create(
            Channel channel,
            Long subscriberCount,
            Long avgViewCount,
            Long avgLikes,
            Long avgComments,
            Integer uploadCount30d,
            LocalDateTime lastUploadAt,
            Long estimatedRevenue,
            BigDecimal cpm,
            BigDecimal rpm,
            Long watchTimeMinutes,
            Integer avgViewDuration,
            Long impressions,
            BigDecimal impressionCtr,
            Integer subscribersGained,
            Integer subscribersLost
    ) {
        ChannelMetrics m = new ChannelMetrics();
        m.channel = channel;
        m.subscriberCount = subscriberCount;
        m.avgViewCount = avgViewCount;
        m.avgLikes = avgLikes;
        m.avgComments = avgComments;
        m.uploadCount30d = uploadCount30d;
        m.lastUploadAt = lastUploadAt;
        m.estimatedRevenue = estimatedRevenue;
        m.cpm = cpm;
        m.rpm = rpm;
        m.watchTimeMinutes = watchTimeMinutes;
        m.avgViewDuration = avgViewDuration;
        m.impressions = impressions;
        m.impressionCtr = impressionCtr;
        m.subscribersGained = subscribersGained;
        m.subscribersLost = subscribersLost;
        return m;
    }
}
