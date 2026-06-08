package com.playstock.channel;

import com.playstock.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "channels")
@Getter
@NoArgsConstructor
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "youtube_channel_id", nullable = false, unique = true)
    private String youtubeChannelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelCategory category;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelStatus status;

    @Enumerated(EnumType.STRING)
    private ChannelTier tier;

    @Column(name = "total_shares", nullable = false)
    private Integer totalShares;

    @Column(name = "sold_shares", nullable = false)
    private Integer soldShares;

    @Column(name = "reserved_shares", nullable = false)
    private Integer reservedShares;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "dividend_rate", nullable = false)
    private BigDecimal dividendRate;

    @Column(name = "rights_start_at")
    private LocalDateTime rightsStartAt;

    @Column(name = "rights_end_at")
    private LocalDateTime rightsEndAt;

    @Column(name = "warning_level")
    private Integer warningLevel;

    @Column(name = "warning_triggered_at")
    private LocalDateTime warningTriggeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.soldShares = 0;
        this.reservedShares = 0;
        this.status = ChannelStatus.PENDING;
    }

    public static Channel create(
            String youtubeChannelId,
            User creator,
            String name,
            ChannelCategory category,
            String thumbnailUrl,
            Integer totalShares,
            Integer price,
            Integer durationMonths,
            BigDecimal dividendRate
    ) {
        Channel channel = new Channel();
        channel.youtubeChannelId = youtubeChannelId;
        channel.creator = creator;
        channel.name = name;
        channel.category = category;
        channel.thumbnailUrl = thumbnailUrl;
        channel.totalShares = totalShares;
        channel.price = price;
        channel.durationMonths = durationMonths;
        channel.dividendRate = dividendRate;
        return channel;
    }
}
