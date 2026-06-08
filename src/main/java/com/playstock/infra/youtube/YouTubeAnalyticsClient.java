package com.playstock.infra.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class YouTubeAnalyticsClient {

    private static final String BASE_URL = "https://youtubeanalytics.googleapis.com/v2";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String METRICS =
            "estimatedRevenue,cpm,rpm,estimatedMinutesWatched,averageViewDuration," +
            "impressions,impressionClickThroughRate,subscribersGained,subscribersLost";

    // 응답 컬럼 인덱스 (metrics 파라미터 순서와 일치)
    private static final Map<String, Integer> COL_INDEX = Map.of(
            "day", 0,
            "estimatedRevenue", 1,
            "cpm", 2,
            "rpm", 3,
            "estimatedMinutesWatched", 4,
            "averageViewDuration", 5,
            "impressions", 6,
            "impressionClickThroughRate", 7,
            "subscribersGained", 8,
            "subscribersLost", 9
    );

    private final RestClient restClient;

    public YouTubeAnalyticsClient() {
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    /** 최근 28일 분석 데이터를 집계하여 반환. 실패 시 null 반환 (Analytics 연동 선택). */
    public YouTubeAnalyticsMetrics getMetrics(String accessToken) {
        String endDate = LocalDate.now().format(DATE_FORMAT);
        String startDate = LocalDate.now().minusDays(28).format(DATE_FORMAT);

        try {
            AnalyticsResponse response = restClient.get()
                    .uri("/reports?ids=channel==MINE&startDate={start}&endDate={end}&metrics={metrics}&dimensions=day",
                            startDate, endDate, METRICS)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(AnalyticsResponse.class);

            if (response == null || response.rows() == null || response.rows().isEmpty()) {
                return YouTubeAnalyticsMetrics.empty();
            }

            return aggregate(response.rows());
        } catch (Exception e) {
            log.warn("YouTube Analytics API 호출 실패 (채널 미연동 또는 권한 없음): {}", e.getMessage());
            return YouTubeAnalyticsMetrics.empty();
        }
    }

    private YouTubeAnalyticsMetrics aggregate(List<List<Object>> rows) {
        double totalRevenue = 0;
        double totalCpm = 0;
        double totalRpm = 0;
        long totalMinutesWatched = 0;
        double totalAvgViewDuration = 0;
        long totalImpressions = 0;
        double totalImpCtr = 0;
        int totalSubsGained = 0;
        int totalSubsLost = 0;
        int count = rows.size();

        for (List<Object> row : rows) {
            totalRevenue += toDouble(row, 1);
            totalCpm += toDouble(row, 2);
            totalRpm += toDouble(row, 3);
            totalMinutesWatched += toLong(row, 4);
            totalAvgViewDuration += toDouble(row, 5);
            totalImpressions += toLong(row, 6);
            totalImpCtr += toDouble(row, 7);
            totalSubsGained += (int) toLong(row, 8);
            totalSubsLost += (int) toLong(row, 9);
        }

        // estimatedRevenue는 USD → KRW 환산 없이 그대로 저장 (운영시 환율 처리 필요)
        // 저장 단위: 원화 환산은 별도 처리, MVP에서는 USD × 100 (센트 단위 long)
        long estimatedRevenueKrw = (long) (totalRevenue * 100);

        return new YouTubeAnalyticsMetrics(
                estimatedRevenueKrw,
                count > 0 ? BigDecimal.valueOf(totalCpm / count) : BigDecimal.ZERO,
                count > 0 ? BigDecimal.valueOf(totalRpm / count) : BigDecimal.ZERO,
                totalMinutesWatched,
                count > 0 ? (int) (totalAvgViewDuration / count) : 0,
                totalImpressions,
                count > 0 ? BigDecimal.valueOf(totalImpCtr / count) : BigDecimal.ZERO,
                totalSubsGained,
                totalSubsLost
        );
    }

    private double toDouble(List<Object> row, int idx) {
        if (idx >= row.size() || row.get(idx) == null) return 0;
        return ((Number) row.get(idx)).doubleValue();
    }

    private long toLong(List<Object> row, int idx) {
        if (idx >= row.size() || row.get(idx) == null) return 0;
        return ((Number) row.get(idx)).longValue();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AnalyticsResponse(List<List<Object>> rows) {}
}
