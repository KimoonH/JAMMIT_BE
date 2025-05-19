package com.jammit_be.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UpdateImageRequest {

    @Schema(description = "사용자의 유저 프로필 이미지 파일 원본 이름", example = "증명사진.jpg", nullable = false)
    private String orgFileName;
    @Schema(description = "사용자의 유저 프로필 이미지 PATH", example = "2024/01/11/uuid-profile.jpg", nullable = false)
    private String profileImagePath;

}
