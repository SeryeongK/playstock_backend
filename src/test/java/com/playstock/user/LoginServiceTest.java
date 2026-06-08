package com.playstock.user;

import com.playstock.common.JwtUtil;
import com.playstock.common.exception.ErrorCode;
import com.playstock.common.exception.PlaystockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private LoginService loginService;

    @Test
    @DisplayName("정상 로그인 - JWT 토큰 포함 LoginResponse 반환")
    void login_성공() {
        // given
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "password", "password123");

        User user = User.create("test@example.com", "테스터", "hashed_password", UserRole.INVESTOR);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "hashed_password")).willReturn(true);
        given(jwtUtil.generateToken(user.getId(), UserRole.INVESTOR)).willReturn("mocked.jwt.token");

        // when
        LoginResponse response = loginService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.INVESTOR);
    }

    @Test
    @DisplayName("존재하지 않는 이메일 - USER_NOT_FOUND 예외 발생")
    void login_유저없음_예외() {
        // given
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "none@example.com");
        ReflectionTestUtils.setField(request, "password", "password123");

        given(userRepository.findByEmail("none@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(PlaystockException.class)
                .satisfies(ex -> assertThat(((PlaystockException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("비밀번호 불일치 - INVALID_PASSWORD 예외 발생")
    void login_비밀번호불일치_예외() {
        // given
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "password", "wrong_password");

        User user = User.create("test@example.com", "테스터", "hashed_password", UserRole.INVESTOR);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong_password", "hashed_password")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(PlaystockException.class)
                .satisfies(ex -> assertThat(((PlaystockException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_PASSWORD));
    }
}
