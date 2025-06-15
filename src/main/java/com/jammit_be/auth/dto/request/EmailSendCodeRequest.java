package com.jammit_be.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmailSendCodeRequest {

    @Schema(description = "이메일 주소", example = "test@example.com")
    private String email;
}
