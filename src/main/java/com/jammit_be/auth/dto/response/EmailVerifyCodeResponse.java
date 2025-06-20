package com.jammit_be.auth.dto.response;


import com.jammit_be.auth.util.authcode.AuthCodeVerifyResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerifyCodeResponse {

    @Schema(description = "인증 성공 여부", example = "true")
    private boolean success;
    @Schema(description = "인증 결과 메시지", example = "인증이 성공했습니다.")
    private String message;

    public static EmailVerifyCodeResponse of(AuthCodeVerifyResult result) {
        return new EmailVerifyCodeResponse(result == AuthCodeVerifyResult.SUCCESS, result.getMessage());
    }

}
