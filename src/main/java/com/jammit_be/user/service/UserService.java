package com.jammit_be.user.service;

import com.jammit_be.auth.dto.response.EmailCheckResponse;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.repository.GatheringRepository;
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
import java.time.LocalDateTime;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorage fileStorage;
    private final GatheringRepository gatheringRepository;

    public UserResponse getUserInfo(String email) {
        var user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AlertException("유저를 찾지 못하였습니다"));
        
        // 유저가 작성한 모임 수와 완료된 모임 수 조회
        long totalCreatedGatheringCount = gatheringRepository.countByCreatedBy(user);
        long completedGatheringCount = gatheringRepository.countByCreatedByAndStatusCompleted(user);
        
        // UserResponse 생성
        UserResponse response = UserResponse.of(user);
        
        // 필드 설정
        return UserResponse.builder()
                .id(response.getId())
                .username(response.getUsername())
                .email(response.getEmail())
                .nickname(response.getNickname())
                .profileImagePath(response.getProfileImagePath())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .preferredGenres(response.getPreferredGenres())
                .preferredBandSessions(response.getPreferredBandSessions())
                .totalCreatedGatheringCount(totalCreatedGatheringCount)
                .completedGatheringCount(completedGatheringCount)
                .build();
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
        
        // 새로 등록된 유저는 아직 모임을 생성하지 않았으므로 0으로 설정
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImagePath(user.getProfileImagePath())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .preferredGenres(user.getPreferredGenres().stream()
                        .map(preferredGenre -> preferredGenre.getName())
                        .collect(java.util.stream.Collectors.toList()))
                .preferredBandSessions(user.getUserBandSessions().stream()
                        .map(preferredBandSession -> preferredBandSession.getName())
                        .collect(java.util.stream.Collectors.toList()))
                .totalCreatedGatheringCount(0L)
                .completedGatheringCount(0L)
                .build();
    }

    @Transactional
    public UserResponse updateUserInfo(String email, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AlertException("유저를 찾지 못하였습니다"));
        
        // 기본 정보 업데이트
        if (updateUserRequest.getEmail() != null) {
            user.setEmail(updateUserRequest.getEmail());
        }
        if (updateUserRequest.getUsername() != null) {
            user.setUsername(updateUserRequest.getUsername());
        }
        if (updateUserRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        }
        
        // 기존 선호 장르와 밴드 세션을 DB에서 직접 삭제
        userRepository.deleteAllPreferredGenresByUserId(user.getId());
        userRepository.deleteAllPreferredBandSessionsByUserId(user.getId());
        
        // 새로운 선호 장르와 밴드 세션 추가
        if (updateUserRequest.getPreferredGenres() != null && !updateUserRequest.getPreferredGenres().isEmpty()) {
            user.updatePreferredGenres(updateUserRequest.getPreferredGenres());
        }
        
        if (updateUserRequest.getPreferredBandSessions() != null && !updateUserRequest.getPreferredBandSessions().isEmpty()) {
            user.updatePreferredBandSessions(updateUserRequest.getPreferredBandSessions());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        
        // 유저가 작성한 모임 수와 완료된 모임 수 조회
        long totalCreatedGatheringCount = gatheringRepository.countByCreatedBy(user);
        long completedGatheringCount = gatheringRepository.countByCreatedByAndStatusCompleted(user);
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImagePath(user.getProfileImagePath())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .preferredGenres(user.getPreferredGenres().stream()
                        .map(preferredGenre -> preferredGenre.getName())
                        .collect(java.util.stream.Collectors.toList()))
                .preferredBandSessions(user.getUserBandSessions().stream()
                        .map(preferredBandSession -> preferredBandSession.getName())
                        .collect(java.util.stream.Collectors.toList()))
                .totalCreatedGatheringCount(totalCreatedGatheringCount)
                .completedGatheringCount(completedGatheringCount)
                .build();
    }

    @Transactional
    public UserResponse updateProfileImage(String email, UpdateImageRequest updateImageRequest) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AlertException("유저를 찾지 못하였습니다"));
        user.updateProfileImage(updateImageRequest);
        
        // 유저가 작성한 모임 수와 완료된 모임 수 조회
        long totalCreatedGatheringCount = gatheringRepository.countByCreatedBy(user);
        long completedGatheringCount = gatheringRepository.countByCreatedByAndStatusCompleted(user);
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImagePath(user.getProfileImagePath())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .preferredGenres(user.getPreferredGenres().stream()
                        .map(preferredGenre -> preferredGenre.getName())
                        .collect(java.util.stream.Collectors.toList()))
                .preferredBandSessions(user.getUserBandSessions().stream()
                        .map(preferredBandSession -> preferredBandSession.getName())
                        .collect(java.util.stream.Collectors.toList()))
                .totalCreatedGatheringCount(totalCreatedGatheringCount)
                .completedGatheringCount(completedGatheringCount)
                .build();
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
