package com.jammit_be.review.dto.response;

import com.jammit_be.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "특정 참가자의 리뷰/평가/프로필 정보를 통합 제공하는 응답")
public class ReviewUserPageResponse {
    @Schema(
            description = "유저 기본 정보 및 프로필",
            implementation = UserResponse.class
    )
    private UserResponse userInfo;

    @Schema(
            description = "받은 리뷰의 통계 정보",
            implementation = ReviewStatisticsResponse.class
    )
    private ReviewStatisticsResponse statistics;

    @ArraySchema(
            schema = @Schema(implementation = ReviewResponse.class),
            arraySchema = @Schema(description = "받은 리뷰 목록")
    )
    private List<ReviewResponse> reviews;
}
