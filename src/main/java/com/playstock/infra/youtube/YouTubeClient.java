package com.playstock.infra.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.playstock.common.exception.ErrorCode;
import com.playstock.common.exception.PlaystockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class YouTubeClient {

    private final RestClient restClient;
    private final String apiKey;

    public YouTubeClient(
            @Value("${spring.youtube.base-url}") String baseUrl,
            @Value("${spring.youtube.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public YouTubeChannelInfo getChannelInfo(String channelId) {
        ChannelListResponse response = restClient.get()
                .uri("/channels?part=snippet,statistics&id={id}&key={key}", channelId, apiKey)
                .retrieve()
                .body(ChannelListResponse.class);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            throw new PlaystockException(ErrorCode.YOUTUBE_CHANNEL_NOT_FOUND);
        }

        ChannelItem item = response.items().get(0);
        return new YouTubeChannelInfo(
                item.id(),
                item.snippet().title(),
                item.snippet().thumbnails().high().url(),
                Long.parseLong(item.statistics().subscriberCount())
        );
    }

    // YouTube API 응답 파싱용 내부 레코드
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChannelListResponse(List<ChannelItem> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChannelItem(String id, Snippet snippet, Statistics statistics) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Snippet(String title, Thumbnails thumbnails) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Thumbnails(Thumbnail high) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Thumbnail(String url) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Statistics(String subscriberCount) {}
}
