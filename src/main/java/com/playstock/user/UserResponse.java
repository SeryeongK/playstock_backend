package com.playstock.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final UserRole role;
    private final Long pointBalance;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getPointBalance()
        );
    }
}
