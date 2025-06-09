package com.jammit_be.review.dto.response;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 응답 정보")
public class ReviewResponse {

    @Schema(description = "리뷰 ID", example = "1")
    private Long id;

    @Schema(description = "리뷰 작성자 ID", example = "1")
    private Long reviewerId;

    @Schema(description = "리뷰 작성자 닉네임", example = "작성자닉네임")
    private String reviewerNickname;
    
    @Schema(description = "리뷰 작성자의 밴드 세션", example = "[\"VOCAL\", \"GUITAR\"]")
    private List<BandSession> reviewerBandSessions;

    @Schema(description = "리뷰 대상자 ID", example = "2")
    private Long revieweeId;

    @Schema(description = "리뷰 대상자 닉네임", example = "대상닉네임")
    private String revieweeNickname;

    @Schema(description = "모임 ID", example = "1")
    private Long gatheringId;

    @Schema(description = "모임 이름", example = "락밴드 모임")
    private String gatheringName;
    
    @Schema(description = "모임 썸네일", example = "https://example.com/thumbnail.jpg")
    private String gatheringThumbnail;
    
    @Schema(description = "모임 주최자 닉네임", example = "주최자닉네임")
    private String gatheringHostNickname;

    @Schema(description = "리뷰 내용", example = "함께 연주하기 좋은 사람이었습니다. 다음에도 함께 하고 싶습니다.")
    private String content;

    @Schema(description = "연주 실력이 좋아요", example = "true")
    private boolean isPracticeHelped;

    @Schema(description = "곡 준비를 잘 해왔어요", example = "true")
    private boolean isGoodWithMusic;

    @Schema(description = "다른 파트와의 호흡이 잘 맞아요", example = "true")
    private boolean isGoodWithOthers;

    @Schema(description = "악보나 연습 자료를 잘 공유해줬어요", example = "false")
    private boolean isSharesPracticeResources;

    @Schema(description = "분위기를 잘 이끌어요", example = "true")
    private boolean isManagingWell;

    @Schema(description = "팀워크가 좋고 함께 연주하기 편했어요", example = "true")
    private boolean isHelpful;

    @Schema(description = "빨리 배워서 잘 따라해줘요", example = "false")
    private boolean isGoodLearner;

    @Schema(description = "합주 시간 약속을 잘 지켜요", example = "true")
    private boolean isKeepingPromises;

    @Schema(description = "생성 일시", example = "2023-06-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2023-06-01T12:00:00")
    private LocalDateTime updatedAt;

    public static ReviewResponse of(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .reviewerId(review.getReviewer().getId())
                .reviewerNickname(review.getReviewer().getNickname())
                .reviewerBandSessions(review.getReviewer().getUserBandSessions().stream()
                        .map(session -> session.getName())
                        .collect(Collectors.toList()))
                .revieweeId(review.getReviewee().getId())
                .revieweeNickname(review.getReviewee().getNickname())
                .gatheringId(review.getGathering().getId())
                .gatheringName(review.getGathering().getName())
                .gatheringThumbnail(review.getGathering().getThumbnail())
                .gatheringHostNickname(review.getGathering().getCreatedBy().getNickname())
                .content(review.getContent())
                .isPracticeHelped(review.isPracticeHelped())
                .isGoodWithMusic(review.isGoodWithMusic())
                .isGoodWithOthers(review.isGoodWithOthers())
                .isSharesPracticeResources(review.isSharesPracticeResources())
                .isManagingWell(review.isManagingWell())
                .isHelpful(review.isHelpful())
                .isGoodLearner(review.isGoodLearner())
                .isKeepingPromises(review.isKeepingPromises())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
} 