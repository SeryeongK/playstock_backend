package com.playstock.user;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupService signupService;

    @Test
    @DisplayName("정상 회원가입 - SignupResponse 반환")
    void signup_성공() {
        // given
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "nickname", "테스터");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "role", UserRole.INVESTOR);

        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("hashed_password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        SignupResponse response = signupService.signup(request);

        // then
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("테스터");
        assertThat(response.getRole()).isEqualTo(UserRole.INVESTOR);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일 - DUPLICATE_EMAIL 예외 발생")
    void signup_중복이메일_예외() {
        // given
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "duplicate@example.com");
        ReflectionTestUtils.setField(request, "nickname", "테스터");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "role", UserRole.INVESTOR);

        given(userRepository.existsByEmail("duplicate@example.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(PlaystockException.class)
                .satisfies(ex -> assertThat(((PlaystockException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_EMAIL));

        verify(userRepository, never()).save(any());
    }
}
