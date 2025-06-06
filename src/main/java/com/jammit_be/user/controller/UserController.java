package com.jammit_be.user.controller;

import com.jammit_be.auth.dto.response.EmailCheckResponse;
import com.jammit_be.auth.util.AuthUtil;
import com.jammit_be.common.dto.CommonResponse;
import com.jammit_be.user.dto.request.UpdateImageRequest;
import com.jammit_be.user.dto.request.UpdateUserRequest;
import com.jammit_be.user.dto.request.CreateUserRequest;
import com.jammit_be.user.dto.response.UserResponse;
import com.jammit_be.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "유저 정보 조회 API")
@RequestMapping("/jammit/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "회원가입 API", description = "유저네임, 이메일, 비밀번호를 받아 회원가입을 진행합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "400", description = "회원가입 실패")
            }
    )
    public CommonResponse<UserResponse> register(@Valid @RequestBody CreateUserRequest createUserRequest) {
        var response = userService.registerUser(createUserRequest);
        return new CommonResponse<UserResponse>().success(response);
    }

    @PutMapping
    @Operation(summary = "유저 정보 수정 API", description = "유저네임, 이메일, 비밀번호를 받아 수정을 진행합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "유저 정보 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "유저 정보 수정 실패")
            }
    )
    public CommonResponse<UserResponse> modify(@RequestBody UpdateUserRequest updateUserRequest) {
        var response = userService.updateUserInfo(AuthUtil.getEmail(), updateUserRequest);
        return new CommonResponse<UserResponse>().success(response);
    }

    @PutMapping("/image")
    @Operation(summary = "유저 이미지 프로필 수정 API", description = "유저 프로필 이미지 정보를 받아 수정을 진행합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "유저 프로필 이미지 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "유저 프로필 이미지 수정 실패")
            }
    )
    public CommonResponse<UserResponse> updateProfileImage(@RequestBody UpdateImageRequest updateImageRequest) {
        var response = userService.updateProfileImage(AuthUtil.getEmail(), updateImageRequest);
        return new CommonResponse<UserResponse>().success(response);
    }

    @GetMapping
    @Operation(summary = "유저 정보 조회 API", description = "토큰을 활용해 유저 정보를 조회한다.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "유저 정보 조회 성공."),
                    @ApiResponse(responseCode = "400",
                            description = "유저 정보 조회 실패")
            }
    )
    public UserResponse getUserInfo() {
        return userService.getUserInfo(AuthUtil.getEmail());
    }

    @GetMapping("/exists")
    @Operation(summary = "이메일 중복 검사 API", description = "이메일을 받아서 중복인지 검사한다.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "이메일 중복 검사 성공."),
                    @ApiResponse(responseCode = "400",
                            description = "이메일 검사 실패. 잘못된 이메일 형식이거나 서버 오류로 인해 검사가 실패했습니다.")
            }
    )
    public CommonResponse<EmailCheckResponse> checkEmailExists(@RequestParam String email) {
        return new CommonResponse<EmailCheckResponse>().success(userService.checkEmailExists(email));
    }

    @Operation(
            summary = "프로필 이미지 업로드",
            description = "PK로 유저를 지정하고, 프로필 이미지를 파일로 업로드합니다. (multipart/form-data 형식)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "업로드 성공 (이미지 URL 반환)")
            }
    )
    @PostMapping(value = "/{userId}/profile-image", consumes = "multipart/form-data")
    public CommonResponse<String> uploadProfileImage(
            @Parameter(description = "유저의 PK (숫자)", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "업로드할 프로필 이미지 파일", required = true)
            @RequestPart("file") MultipartFile file) {

        String result = userService.uploadProfileImage(userId, file);
        return new CommonResponse<String>().success(result);
    }
}
