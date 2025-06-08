package com.jammit_be.user.dto.response;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.user.entity.PreferredBandSession;
import com.jammit_be.user.entity.PreferredGenre;
import com.jammit_be.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 응답")
public class UserResponse {
    @Schema(description = "사용자 고유키", example = "1")
    private Long id;
    @Schema(description = "사용자의 유저네임", example = "test")
    private String username;
    @Schema(description = "사용자의 이메일", example = "test@test.com")
    private String email;
    @Schema(description = "사용자의 닉네임", example = "Nick")
    private String nickname;
    @Schema(description = "사용자의 유저 프로필 이미지 PATH", example = "2024/01/11/uuid-profile.jpg", nullable = true)
    private String profileImagePath;
    @Schema(description = "가입 일자", example = "2024-10-11T15:21:00")
    private LocalDateTime createdAt;
    @Schema(description = "수정 일자", example = "2024-10-11T15:21:00")
    private LocalDateTime updatedAt;
    @Schema(description = "선호하는 장르 목록", example = "[\"ROCK\", \"INDIE\", \"JAZZ\"]")
    private List<Genre> preferredGenres;
    @Schema(description = "선호하는 밴드 세션 목록", example = "[\"VOCAL\", \"ELECTRIC_GUITAR\", \"BASS\", \"KEYBOARD\"]")
    private List<BandSession> preferredBandSessions;
    @Schema(description = "사용자가 작성한 모든 모임 수", example = "5")
    private Long totalCreatedGatheringCount;
    @Schema(description = "사용자가 작성한 모임 중 완료된(COMPLETED) 모임 수", example = "3")
    private Long completedGatheringCount;

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImagePath(user.getProfileImagePath())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .preferredGenres(user.getPreferredGenres().stream()
                        .map(PreferredGenre::getName)
                        .collect(Collectors.toList()))
                .preferredBandSessions(user.getUserBandSessions().stream()
                        .map(PreferredBandSession::getName)
                        .collect(Collectors.toList()))
                .build();
    }

}
