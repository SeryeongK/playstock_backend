package com.playstock.user;

import com.playstock.common.exception.ErrorCode;
import com.playstock.common.exception.PlaystockException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new PlaystockException(ErrorCode.DUPLICATE_EMAIL);
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = User.create(request.getEmail(), request.getNickname(), passwordHash, request.getRole());
        userRepository.save(user);

        return SignupResponse.from(user);
    }
}
