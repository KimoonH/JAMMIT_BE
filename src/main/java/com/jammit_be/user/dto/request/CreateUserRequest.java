package com.jammit_be.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class CreateUserRequest {

    @Email
    @Schema(description = "사용자의 이메일", example = "test@test.com", nullable = false)
    private String email;
    @NotEmpty
    @NotEmpty
    @Schema(description = "사용자의 유저 네임", example = "test", nullable = false)
    private String username;
    @NotEmpty
    @Schema(description = "사용자의 비밀번호", example = "1234", nullable = false)
    private String password;

}
