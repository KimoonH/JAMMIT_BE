package com.jammit_be.auth.controller;

import com.jammit_be.auth.dto.request.EmailSendCodeRequest;
import com.jammit_be.auth.dto.request.EmailVerifyCodeRequest;
import com.jammit_be.auth.dto.request.LoginRequest;
import com.jammit_be.auth.dto.request.TokenRequest;
import com.jammit_be.auth.dto.response.EmailVerifyCodeResponse;
import com.jammit_be.auth.dto.response.LoginResponse;
import com.jammit_be.auth.dto.response.TokenResponse;
import com.jammit_be.auth.service.AuthService;
import com.jammit_be.auth.service.EmailAuthService;
import com.jammit_be.auth.util.authcode.AuthCodeVerifyResult;
import com.jammit_be.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/jammit/auth")
public class AuthController {

  private final AuthService authService;
  private final EmailAuthService emailAuthService;

  @PostMapping("/login")
  @Operation(summary = "로그인 API", description = "이메일과 비밀번호를 받아 토큰을 생성합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "로그인 성공"),
          @ApiResponse(responseCode = "400", description = "로그인 실패")
      }
  )
  public CommonResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
    var response = authService.login(loginRequest);
    return new CommonResponse<LoginResponse>().success(response);
  }

  @PostMapping("/refresh")
  @Operation(summary = "토큰 갱신 API", description = "리프레시 토큰을 받아 새로운 액세스 토큰을 생성합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
          @ApiResponse(responseCode = "400", description = "토큰 갱신 실패")
      }
  )
  public CommonResponse<TokenResponse> refresh(@RequestBody TokenRequest refreshToken) {
    var response = authService.refresh(refreshToken.getRefreshToken());
    return new CommonResponse<TokenResponse>().success(response);
  }

  // 인증번호 발송
  @PostMapping("/email/send-code")
  @Operation(
          summary = "이메일 인증번호 발송",
          description = "입력한 이메일 주소로 인증번호를 전송합니다."
  )
  public CommonResponse<Void> sendAuthCode(@RequestBody EmailSendCodeRequest request) {
    emailAuthService.sendAuthCode(request.getEmail());
    return CommonResponse.ok();
  }


  @PostMapping("/email/verify-code")
  @Operation(
          summary = "이메일 인증번호 검증",
          description = "이메일과 인증번호를 받아 인증 성공 여부와 메시지를 반환합니다.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "인증 성공"),
                  @ApiResponse(responseCode = "401", description = "인증번호가 일치하지 않음"),
                  @ApiResponse(responseCode = "404", description = "해당 이메일 인증 요청이 없음"),
                  @ApiResponse(responseCode = "440", description = "인증번호 만료")
          }
  )
  public ResponseEntity<CommonResponse<EmailVerifyCodeResponse>> verifyAuthCode(@RequestBody EmailVerifyCodeRequest request) {
    AuthCodeVerifyResult result = emailAuthService.verifyAuthCode(request.getEmail(), request.getCode());
    EmailVerifyCodeResponse response = EmailVerifyCodeResponse.of(result);

    switch (result) {
      case SUCCESS:
        // 200 OK (성공)
        return ResponseEntity.ok(CommonResponse.ok(response));
      case EXPIRED:
        // 440: 인증번호 만료
        return ResponseEntity.status(440)
                .body(CommonResponse.fail(440, result.getMessage(), response));
      case INVALID:
        // 401: 인증번호 불일치
        return ResponseEntity.status(401)
                .body(CommonResponse.fail(401, result.getMessage(), response));
      case NOT_FOUND:
      default:
        // 404: 인증 요청 내역 없음
        return ResponseEntity.status(404)
                .body(CommonResponse.fail(404, result.getMessage(), response));
    }
  }
}
