package com.playstock.user.oauth;

import com.playstock.user.User;

public record OAuthStatusResponse(
        boolean connected,
        String channelId,
        String channelName
) {
    public static OAuthStatusResponse connected(User user, String channelName) {
        return new OAuthStatusResponse(true, user.getYoutubeChannelId(), channelName);
    }

    public static OAuthStatusResponse notConnected() {
        return new OAuthStatusResponse(false, null, null);
    }
}
