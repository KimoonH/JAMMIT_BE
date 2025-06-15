package com.jammit_be.review.dto.response;

import com.jammit_be.review.entity.Review;
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
@Schema(description = "리뷰 통계 정보 응답")
public class ReviewStatisticsResponse {

    @Schema(description = "총 받은 리뷰 개수", example = "15")
    private int totalReviews;

    @Schema(description = "연주 실력이 좋아요 평가 개수", example = "12")
    private int practiceHelpedCount;

    @Schema(description = "곡 준비를 잘 해왔어요 평가 개수", example = "10")
    private int goodWithMusicCount;

    @Schema(description = "다른 파트와의 호흡이 잘 맞아요 평가 개수", example = "9")
    private int goodWithOthersCount;

    @Schema(description = "악보나 연습 자료를 잘 공유해줬어요 평가 개수", example = "8")
    private int sharesPracticeResourcesCount;

    @Schema(description = "분위기를 잘 이끌어요 평가 개수", example = "11")
    private int managingWellCount;

    @Schema(description = "팀워크가 좋고 함께 연주하기 편했어요 평가 개수", example = "13")
    private int helpfulCount;

    @Schema(description = "빨리 배워서 잘 따라해줘요 평가 개수", example = "7")
    private int goodLearnerCount;

    @Schema(description = "합주 시간 약속을 잘 지켜요 평가 개수", example = "14")
    private int keepingPromisesCount;
    
    // 각 항목별 백분율 (소수점 1자리까지)
    @Schema(description = "연주 실력이 좋아요 백분율", example = "80.0")
    private double practiceHelpedPercentage;
    
    @Schema(description = "곡 준비를 잘 해왔어요 백분율", example = "66.7")
    private double goodWithMusicPercentage;
    
    @Schema(description = "다른 파트와의 호흡이 잘 맞아요 백분율", example = "60.0")
    private double goodWithOthersPercentage;
    
    @Schema(description = "악보나 연습 자료를 잘 공유해줬어요 백분율", example = "53.3")
    private double sharesPracticeResourcesPercentage;
    
    @Schema(description = "분위기를 잘 이끌어요 백분율", example = "73.3")
    private double managingWellPercentage;
    
    @Schema(description = "팀워크가 좋고 함께 연주하기 편했어요 백분율", example = "86.7")
    private double helpfulPercentage;
    
    @Schema(description = "빨리 배워서 잘 따라해줘요 백분율", example = "46.7")
    private double goodLearnerPercentage;
    
    @Schema(description = "합주 시간 약속을 잘 지켜요 백분율", example = "93.3")
    private double keepingPromisesPercentage;

    public static ReviewStatisticsResponse of(List<Review> reviews) {
        int totalReviews = reviews.size();
        if (totalReviews == 0) {
            return ReviewStatisticsResponse.builder()
                    .totalReviews(0)
                    .practiceHelpedCount(0)
                    .goodWithMusicCount(0)
                    .goodWithOthersCount(0)
                    .sharesPracticeResourcesCount(0)
                    .managingWellCount(0)
                    .helpfulCount(0)
                    .goodLearnerCount(0)
                    .keepingPromisesCount(0)
                    .practiceHelpedPercentage(0)
                    .goodWithMusicPercentage(0)
                    .goodWithOthersPercentage(0)
                    .sharesPracticeResourcesPercentage(0)
                    .managingWellPercentage(0)
                    .helpfulPercentage(0)
                    .goodLearnerPercentage(0)
                    .keepingPromisesPercentage(0)
                    .build();
        }

        int practiceHelpedCount = (int) reviews.stream().filter(Review::isPracticeHelped).count();
        int goodWithMusicCount = (int) reviews.stream().filter(Review::isGoodWithMusic).count();
        int goodWithOthersCount = (int) reviews.stream().filter(Review::isGoodWithOthers).count();
        int sharesPracticeResourcesCount = (int) reviews.stream().filter(Review::isSharesPracticeResources).count();
        int managingWellCount = (int) reviews.stream().filter(Review::isManagingWell).count();
        int helpfulCount = (int) reviews.stream().filter(Review::isHelpful).count();
        int goodLearnerCount = (int) reviews.stream().filter(Review::isGoodLearner).count();
        int keepingPromisesCount = (int) reviews.stream().filter(Review::isKeepingPromises).count();

        return ReviewStatisticsResponse.builder()
                .totalReviews(totalReviews)
                .practiceHelpedCount(practiceHelpedCount)
                .goodWithMusicCount(goodWithMusicCount)
                .goodWithOthersCount(goodWithOthersCount)
                .sharesPracticeResourcesCount(sharesPracticeResourcesCount)
                .managingWellCount(managingWellCount)
                .helpfulCount(helpfulCount)
                .goodLearnerCount(goodLearnerCount)
                .keepingPromisesCount(keepingPromisesCount)
                .practiceHelpedPercentage(calcPercent(practiceHelpedCount, totalReviews))
                .goodWithMusicPercentage(calcPercent(goodWithMusicCount, totalReviews))
                .goodWithOthersPercentage(calcPercent(goodWithOthersCount, totalReviews))
                .sharesPracticeResourcesPercentage(calcPercent(sharesPracticeResourcesCount, totalReviews))
                .managingWellPercentage(calcPercent(managingWellCount, totalReviews))
                .helpfulPercentage(calcPercent(helpfulCount, totalReviews))
                .goodLearnerPercentage(calcPercent(goodLearnerCount, totalReviews))
                .keepingPromisesPercentage(calcPercent(keepingPromisesCount, totalReviews))
                .build();
    }

    private static double calcPercent(int count, int total) {
        return total == 0 ? 0.0 : Math.round(((double) count / total) * 1000) / 10.0;
    }
} 