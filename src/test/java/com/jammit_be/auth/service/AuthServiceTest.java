package com.jammit_be.auth.service;

import com.jammit_be.auth.dto.request.LoginRequest;
import com.jammit_be.auth.dto.response.LoginResponse;
import com.jammit_be.auth.dto.response.TokenResponse;
import com.jammit_be.auth.util.JwtUtil;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.user.entity.OauthPlatform;
import com.jammit_be.user.entity.User;
import com.jammit_be.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 실제 DB에 테스트용 유저 저장
        testUser = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123!"))  // 실제 인코딩된 비밀번호
                .username("testuser")
                .nickname("테스터")
                .oauthPlatform(OauthPlatform.NONE)
                .build();

        userRepository.save(testUser);

        // 로그인 요청 데이터
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123!")  // 원본 비밀번호
                .build();
    }

    @Test
    @DisplayName("정상 로그인 - 올바른 이메일과 비밀번호로 로그인 성공")
    void login_success() {
        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getExpiredAt()).isNotNull();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getUser().getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_fail_userNotFound() {
        // given
        LoginRequest wrongEmailRequest = LoginRequest.builder()
                .email("notexist@example.com")
                .password("password123!")
                .build();

        // when & then
        assertThatThrownBy(() -> authService.login(wrongEmailRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("가입되지 않은 이메일입니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_wrongPassword() {
        // given
        LoginRequest wrongPasswordRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongPassword!")
                .build();

        // when & then
        assertThatThrownBy(() -> authService.login(wrongPasswordRequest))
                .isInstanceOf(AlertException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("토큰 갱신 성공 - 유효한 refreshToken으로 accessToken 갱신")
    void refresh_success() {
        // given - 먼저 로그인해서 실제 refreshToken 얻기
        LoginResponse loginResponse = authService.login(loginRequest);
        String refreshToken = loginResponse.getRefreshToken();

        // when
        TokenResponse response = authService.refresh(refreshToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getExpiredAt()).isNotNull();
    }
}
