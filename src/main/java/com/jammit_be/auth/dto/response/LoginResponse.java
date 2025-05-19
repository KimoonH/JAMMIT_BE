package com.jammit_be.auth.dto.response;

import com.jammit_be.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    @Schema(description = "사용자 정보")
    private UserResponse user;
    @Schema(description = "accessToken", example = "eyJhbGciOiJIUzI1NiJ9.eyJsb2dpbklkIjoic3RyaW5nIiwidHlwZSI6ImFjY2VzcyIsImlhdCI6MTczMjgwMzI0OSwiZXhwIjoxNzMyODAzODQ5fQ.F3hjvzGHgoahAAUUe3M44UfU8eceSHHdl4LFkH8GBjQ")
    private String accessToken;
    @Schema(description = "refreshToken", example = "eyJhbGciOiJIUzI1NiJ9.eyJsb2dpbklkIjoic3RyaW5nIiwidHlwZSI6ImFjY2VzcyIsImlhdCI6MTczMjgwMzI0OSwiZXhwIjoxNzMyODAzODQ5fQ.F3hjvzGHgoahAAUUe3M44UfU8eceSHHdl4LFkH8GBjQ")
    private String refreshToken;
    @Schema(description = "refreshToken 만료 시간", example = "2021-11-01T00:00:00")
    private LocalDateTime expiredAt;

}
