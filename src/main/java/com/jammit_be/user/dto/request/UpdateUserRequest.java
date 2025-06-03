package com.jammit_be.user.dto.request;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "사용자 정보 수정 요청")
public class UpdateUserRequest {

    @Email
    @Schema(description = "사용자의 이메일", example = "new_email@test.com", nullable = true)
    private String email;
    
    @Schema(description = "사용자의 유저 네임", example = "new_username", nullable = true)
    private String username;
    
    @Schema(description = "사용자의 비밀번호", example = "new_password", nullable = true)
    private String password;
    
    @Schema(description = "선호하는 장르 목록", example = "[\"ROCK\", \"BALLAD\", \"FOLK\"]", nullable = true)
    private List<Genre> preferredGenres;
    
    @Schema(description = "선호하는 밴드 세션 목록", example = "[\"DRUM\", \"BASS\", \"KEYBOARD\"]", nullable = true)
    private List<BandSession> preferredBandSessions;

}
