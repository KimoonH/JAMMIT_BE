package com.jammit_be.auth.controller;

import com.jammit_be.auth.dto.request.LoginRequest;
import com.jammit_be.auth.dto.request.TokenRequest;
import com.jammit_be.auth.dto.response.LoginResponse;
import com.jammit_be.auth.dto.response.TokenResponse;
import com.jammit_be.auth.service.AuthService;
import com.jammit_be.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/jammit/auth")
public class AuthController {

  private final AuthService authService;

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

}
