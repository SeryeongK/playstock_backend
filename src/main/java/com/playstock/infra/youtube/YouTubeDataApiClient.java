package com.playstock.infra.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.playstock.common.exception.ErrorCode;
import com.playstock.common.exception.PlaystockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OAuth Bearer 토큰이 필요한 YouTube Data API 호출을 담당.
 * mine=true 파라미터로 인증된 크리에이터의 채널 정보를 조회한다.
 */
@Component
public class YouTubeDataApiClient {

    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3";

    private final RestClient restClient;
    private final String apiKey;

    public YouTubeDataApiClient(
            @Value("${spring.youtube.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
        this.apiKey = apiKey;
    }

    /** OAuth 로그인한 크리에이터 본인의 채널 정보 조회 (mine=true) */
    public YouTubeCreatorChannelInfo getMyChannelInfo(String accessToken) {
        ChannelListResponse response = restClient.get()
                .uri("/channels?part=snippet,statistics,contentDetails&mine=true")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(ChannelListResponse.class);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            throw new PlaystockException(ErrorCode.YOUTUBE_MY_CHANNEL_NOT_FOUND);
        }

        ChannelItem item = response.items().get(0);
        String uploadsPlaylistId = item.contentDetails().relatedPlaylists().uploads();

        return new YouTubeCreatorChannelInfo(
                item.id(),
                item.snippet().title(),
                item.snippet().thumbnails().high().url(),
                Long.parseLong(item.statistics().subscriberCount()),
                uploadsPlaylistId
        );
    }

    /** 업로드 재생목록에서 최근 50개 영상의 통계를 집계 */
    public YouTubeVideoMetrics getVideoMetrics(String uploadsPlaylistId) {
        // Step 1: 재생목록에서 최근 영상 ID 수집
        PlaylistItemsResponse playlistResponse = restClient.get()
                .uri("/playlistItems?part=contentDetails,snippet&playlistId={pid}&maxResults=50&key={key}",
                        uploadsPlaylistId, apiKey)
                .retrieve()
                .body(PlaylistItemsResponse.class);

        if (playlistResponse == null || playlistResponse.items() == null || playlistResponse.items().isEmpty()) {
            return new YouTubeVideoMetrics(0, 0, 0, 0, null);
        }

        List<PlaylistItem> items = playlistResponse.items();
        String videoIds = items.stream()
                .map(i -> i.contentDetails().videoId())
                .collect(Collectors.joining(","));

        LocalDateTime lastUploadAt = items.stream()
                .map(i -> OffsetDateTime.parse(i.snippet().publishedAt()).toLocalDateTime())
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // 30일 이내 업로드 수
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int uploadCount30d = (int) items.stream()
                .map(i -> OffsetDateTime.parse(i.snippet().publishedAt()).toLocalDateTime())
                .filter(dt -> dt.isAfter(thirtyDaysAgo))
                .count();

        // Step 2: 영상 통계 조회
        VideoListResponse videoResponse = restClient.get()
                .uri("/videos?part=statistics&id={ids}&key={key}", videoIds, apiKey)
                .retrieve()
                .body(VideoListResponse.class);

        if (videoResponse == null || videoResponse.items() == null || videoResponse.items().isEmpty()) {
            return new YouTubeVideoMetrics(0, 0, 0, uploadCount30d, lastUploadAt);
        }

        List<VideoItem> videos = videoResponse.items();
        long avgViews = (long) videos.stream()
                .mapToLong(v -> parseLong(v.statistics().viewCount()))
                .average().orElse(0);
        long avgLikes = (long) videos.stream()
                .mapToLong(v -> parseLong(v.statistics().likeCount()))
                .average().orElse(0);
        long avgComments = (long) videos.stream()
                .mapToLong(v -> parseLong(v.statistics().commentCount()))
                .average().orElse(0);

        return new YouTubeVideoMetrics(avgViews, avgLikes, avgComments, uploadCount30d, lastUploadAt);
    }

    private long parseLong(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ── 응답 파싱용 내부 레코드 ──────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChannelListResponse(List<ChannelItem> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChannelItem(String id, Snippet snippet, Statistics statistics, ContentDetails contentDetails) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Snippet(String title, Thumbnails thumbnails) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Thumbnails(Thumbnail high) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Thumbnail(String url) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Statistics(String subscriberCount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ContentDetails(RelatedPlaylists relatedPlaylists) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RelatedPlaylists(String uploads) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PlaylistItemsResponse(List<PlaylistItem> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PlaylistItem(PlaylistContentDetails contentDetails, PlaylistSnippet snippet) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PlaylistContentDetails(String videoId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PlaylistSnippet(String publishedAt) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record VideoListResponse(List<VideoItem> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record VideoItem(VideoStatistics statistics) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record VideoStatistics(
            @JsonProperty("viewCount") String viewCount,
            @JsonProperty("likeCount") String likeCount,
            @JsonProperty("commentCount") String commentCount
    ) {}
}
