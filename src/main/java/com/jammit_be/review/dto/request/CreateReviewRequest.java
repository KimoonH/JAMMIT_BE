package com.jammit_be.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "리뷰 생성 요청")
public class CreateReviewRequest {

    @NotNull
    @Schema(description = "리뷰 대상 사용자 ID", example = "2", required = true)
    private Long revieweeId;

    @NotNull
    @Schema(description = "모임 ID", example = "1", required = true)
    private Long gatheringId;

    @Schema(description = "리뷰 내용", example = "함께 연주하기 좋은 사람이었습니다. 다음에도 함께 하고 싶습니다.")
    private String content;

    @Schema(description = "연주 실력이 좋아요", example = "true", defaultValue = "false")
    private Boolean isPracticeHelped = false;

    @Schema(description = "곡 준비를 잘 해왔어요", example = "true", defaultValue = "false")
    private Boolean isGoodWithMusic = false;

    @Schema(description = "다른 파트와의 호흡이 잘 맞아요", example = "true", defaultValue = "false")
    private Boolean isGoodWithOthers = false;

    @Schema(description = "악보나 연습 자료를 잘 공유해줬어요", example = "true", defaultValue = "false")
    private Boolean isSharesPracticeResources = false;

    @Schema(description = "분위기를 잘 이끌어요", example = "true", defaultValue = "false")
    private Boolean isManagingWell = false;

    @Schema(description = "팀워크가 좋고 함께 연주하기 편했어요", example = "true", defaultValue = "false")
    private Boolean isHelpful = false;

    @Schema(description = "빨리 배워서 잘 따라해줘요", example = "true", defaultValue = "false")
    private Boolean isGoodLearner = false;

    @Schema(description = "합주 시간 약속을 잘 지켜요", example = "true", defaultValue = "false")
    private Boolean isKeepingPromises = false;
} 