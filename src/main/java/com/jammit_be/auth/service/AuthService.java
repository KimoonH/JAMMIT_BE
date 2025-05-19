package com.jammit_be.auth.service;

import com.jammit_be.auth.dto.request.LoginRequest;
import com.jammit_be.auth.dto.response.LoginResponse;
import com.jammit_be.auth.dto.response.TokenResponse;
import com.jammit_be.auth.util.JwtUtil;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.user.dto.response.UserResponse;
import com.jammit_be.user.entity.OauthPlatform;
import com.jammit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest loginRequest) {
        var email = loginRequest.getEmail();
        var password = loginRequest.getPassword();
        var user = userRepository.findUserByEmailAndOauthPlatform(email, OauthPlatform.NONE)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 이메일입니다."));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AlertException("비밀번호가 일치하지 않습니다.");
        }
        var userDto = UserResponse.of(user);
        return LoginResponse.builder()
                .user(userDto)
                .accessToken(jwtUtil.createAccessToken(email))
                .refreshToken(jwtUtil.createRefreshToken(email))
                .expiredAt(jwtUtil.getExpiredAt())
                .build();
    }

    public TokenResponse refresh(String refreshToken) {
        var accessToken = jwtUtil.refreshAccessToken(refreshToken);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiredAt(jwtUtil.getExpiredAt())
                .build();
    }

}
