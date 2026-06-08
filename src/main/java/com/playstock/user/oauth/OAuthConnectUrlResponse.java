package com.playstock.user.oauth;

public record OAuthConnectUrlResponse(String authUrl) {

    public static OAuthConnectUrlResponse of(String authUrl) {
        return new OAuthConnectUrlResponse(authUrl);
    }
}
