package com.playstock.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginResponse {

    private final String accessToken;
    private final Long userId;
    private final String email;
    private final String nickname;
    private final UserRole role;

    public static LoginResponse of(String accessToken, User user) {
        return new LoginResponse(
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole()
        );
    }
}
