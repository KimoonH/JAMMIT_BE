package com.jammit_be.user.dto.request;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "사용자 회원가입 요청")
public class CreateUserRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력해 주세요.")
    @Schema(description = "사용자의 이메일", example = "test@test.com", nullable = false)
    private String email;

    @NotBlank(message = "유저 네임을 입력해 주세요.")
    @Size(min = 2, max = 30, message = "유저 네임은 2~30자여야 합니다.")
    @Schema(description = "사용자의 유저 네임", example = "test", nullable = false)
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    @Schema(description = "사용자의 비밀번호", example = "1234", nullable = false)
    private String password;

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(min = 2, max = 30, message = "닉네임은 2~30자여야 합니다.")
    @Schema(description = "사용자의 닉네임", example = "Nick", nullable = false)
    private String nickname;

    @Schema(description = "선호하는 장르 목록", example = "[\"ROCK\", \"INDIE\", \"JAZZ\"]")
    private List<Genre> preferredGenres;
    @Schema(description = "선호하는 밴드 세션 목록", example = "[\"VOCAL\", \"ELECTRIC_GUITAR\", \"BASS\", \"KEYBOARD\"]")
    private List<BandSession> preferredBandSessions;
}
