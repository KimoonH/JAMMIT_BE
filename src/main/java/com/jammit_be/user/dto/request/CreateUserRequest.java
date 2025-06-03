package com.jammit_be.user.dto.request;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "사용자 회원가입 요청")
public class CreateUserRequest {

    @Email
    @Schema(description = "사용자의 이메일", example = "test@test.com", nullable = false)
    private String email;
    @NotEmpty
    @Schema(description = "사용자의 유저 네임", example = "test", nullable = false)
    private String username;
    @NotEmpty
    @Schema(description = "사용자의 비밀번호", example = "1234", nullable = false)
    private String password;
    @NotEmpty
    @Schema(description = "사용자의 닉네임", example = "Nick", nullable = false)
    private String nickname;
    @Schema(description = "선호하는 장르 목록", example = "[\"ROCK_METAL\", \"INDIE\", \"JAZZ\"]")
    private List<Genre> preferredGenres;
    @Schema(description = "선호하는 밴드 세션 목록", example = "[\"VOCAL\", \"ELECTRIC_GUITAR\", \"BASS\", \"KEYBOARD\"]")
    private List<BandSession> preferredBandSessions;
}
