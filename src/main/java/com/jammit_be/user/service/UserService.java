package com.jammit_be.user.service;

import com.jammit_be.auth.dto.response.EmailCheckResponse;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.storage.FileStorage;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorage fileStorage;

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
        
        // 선호 장르와 선호 밴드 세션 설정
        user.updatePreferredGenres(createUserRequest.getPreferredGenres());
        user.updatePreferredBandSessions(createUserRequest.getPreferredBandSessions());

        userRepository.save(user);
        return UserResponse.of(user);
    }

    @Transactional
    public UserResponse updateUserInfo(String email, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AlertException("유저를 찾지 못하였습니다"));
        user.modify(updateUserRequest, passwordEncoder);
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

    public String uploadProfileImage(Long userId ,MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AlertException("유저를 찾지 못하였습니다"));
        
        if(file == null || file.isEmpty()) {
            throw new AlertException("파일을 첨부하지 않았습니다.");
        }

        String url = fileStorage.save(file, "profile");
        user.changeProfileImage(file.getOriginalFilename(), url);

        return url;
    }
}
