package com.jammit_be.gathering.dto;

import com.jammit_be.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "모임 생성자(주최자) 정보")
@Builder
@Getter
public class CreatorInfo {

    @Schema(description = "유저 ID", example = "10")
    private final Long id;
    @Schema(description = "닉네임", example = "윤민")
    private final String nickname;
    @Schema(description = "주최자 프로필 이미지", example = "2024/01/11/uuid-profile.jpg")
    private final String profileImagePath;

    public static CreatorInfo of(User user) {
        if(user == null) return null;
        return CreatorInfo.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImagePath(user.getProfileImagePath())
                .build();
    }
}
