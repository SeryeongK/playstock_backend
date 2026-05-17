package com.playstock.user;

import com.playstock.common.JwtUtil;
import com.playstock.common.exception.ErrorCode;
import com.playstock.common.exception.PlaystockException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new PlaystockException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new PlaystockException(ErrorCode.INVALID_PASSWORD);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return LoginResponse.of(token, user);
    }
}
