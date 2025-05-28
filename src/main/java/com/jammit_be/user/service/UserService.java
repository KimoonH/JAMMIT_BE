package com.jammit_be.user.service;

import com.jammit_be.auth.dto.response.EmailCheckResponse;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.user.dto.request.UpdateImageRequest;
import com.jammit_be.user.dto.request.UpdateUserRequest;
import com.jammit_be.user.dto.request.CreateUserRequest;
import com.jammit_be.user.dto.response.UserResponse;
import com.jammit_be.user.entity.OauthPlatform;
import com.jammit_be.user.entity.User;
import com.jammit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserInfo(String email) {
        var user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AlertException("유저를 찾지 못하였습니다"));
        return UserResponse.of(user);
    }

    @Transactional
    public UserResponse registerUser(CreateUserRequest createUserRequest) {
        var email = createUserRequest.getEmail();
        var password = createUserRequest.getPassword();
        if (userRepository.existsUserByEmail(email)) {
            throw new AlertException("이메일이 중복되었습니다.");
        }
        var user = User.builder()
                .username(createUserRequest.getUsername())
                .password(passwordEncoder.encode(password))
                .nickname(createUserRequest.getNickname())
                .email(email)
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(user);
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional
    public UserResponse updateUserInfo(String email, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AlertException("유저를 찾지 못하였습니다"));
        user.modify(updateUserRequest, passwordEncoder);
//    cacheService.evictCacheByKey("emailCheck", email);
        return UserResponse.of(user);
    }

    @Transactional
    public UserResponse updateProfileImage(String email, UpdateImageRequest updateImageRequest) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AlertException("유저를 찾지 못하였습니다"));
        user.updateProfileImage(updateImageRequest);
        return UserResponse.of(user);
    }

    public EmailCheckResponse checkEmailExists(String email) {
        return new EmailCheckResponse(userRepository.existsUserByEmail(email));
    }
}
