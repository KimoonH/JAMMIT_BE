package com.jammit_be.review.service;

import com.jammit_be.auth.util.AuthUtil;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.repository.GatheringRepository;
import com.jammit_be.review.dto.request.CreateReviewRequest;
import com.jammit_be.review.dto.response.ReviewResponse;
import com.jammit_be.review.dto.response.ReviewStatisticsResponse;
import com.jammit_be.review.entity.Review;
import com.jammit_be.review.repository.ReviewRepository;
import com.jammit_be.user.entity.User;
import com.jammit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final GatheringRepository gatheringRepository;

    /**
     * 리뷰 생성
     */
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        Long reviewerId = AuthUtil.getUserInfo().getId();
        // 1. 리뷰 작성자 확인
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new AlertException("리뷰 작성자를 찾을 수 없습니다."));

        // 2. 리뷰 대상자 확인
        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new AlertException("리뷰 대상자를 찾을 수 없습니다."));

        // 3. 모임 확인
        Gathering gathering = gatheringRepository.findById(request.getGatheringId())
                .orElseThrow(() -> new AlertException("모임을 찾을 수 없습니다."));

        // 4. 이미 해당 모임에서 해당 사용자에 대한 리뷰를 작성했는지 확인
        reviewRepository.findByReviewerIdAndRevieweeIdAndGatheringId(
                        reviewerId, request.getRevieweeId(), request.getGatheringId())
                .ifPresent(r -> {
                    throw new AlertException("이미 이 모임에서 해당 사용자에 대한 리뷰를 작성했습니다.");
                });

        // 5. 리뷰 생성
        Review review = new Review();
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setGathering(gathering);
        review.setContent(request.getContent());
        review.setPracticeHelped(request.getIsPracticeHelped());
        review.setGoodWithMusic(request.getIsGoodWithMusic());
        review.setGoodWithOthers(request.getIsGoodWithOthers());
        review.setSharesPracticeResources(request.getIsSharesPracticeResources());
        review.setManagingWell(request.getIsManagingWell());
        review.setHelpful(request.getIsHelpful());
        review.setGoodLearner(request.getIsGoodLearner());
        review.setKeepingPromises(request.getIsKeepingPromises());

        reviewRepository.save(review);
        return ReviewResponse.of(review);
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        Long reviewerId = AuthUtil.getUserInfo().getId();
        // 1. 리뷰 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AlertException("리뷰를 찾을 수 없습니다."));

        // 2. 리뷰 작성자 확인
        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new AlertException("리뷰를 삭제할 권한이 없습니다.");
        }

        // 3. 리뷰 삭제
        reviewRepository.delete(review);
    }

    /**
     * 사용자가 작성한 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByReviewer() {
        Long reviewerId = AuthUtil.getUserInfo().getId();
        return reviewRepository.findAllByReviewerId(reviewerId).stream()
                .map(ReviewResponse::of)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 받은 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByReviewee() {
        Long revieweeId = AuthUtil.getUserInfo().getId();
        return reviewRepository.findAllByRevieweeId(revieweeId).stream()
                .map(ReviewResponse::of)
                .collect(Collectors.toList());
    }

    /**
     * 모임에 대한 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByGathering(Long gatheringId) {
        return reviewRepository.findAllByGatheringId(gatheringId).stream()
                .map(ReviewResponse::of)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자가 받은 리뷰의 평가항목별 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public ReviewStatisticsResponse getReviewStatistics() {
        Long revieweeId = AuthUtil.getUserInfo().getId();
        List<Review> reviews = reviewRepository.findAllByRevieweeId(revieweeId);
        
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
        
        // 각 평가항목별 카운트 계산
        int practiceHelpedCount = (int) reviews.stream().filter(Review::isPracticeHelped).count();
        int goodWithMusicCount = (int) reviews.stream().filter(Review::isGoodWithMusic).count();
        int goodWithOthersCount = (int) reviews.stream().filter(Review::isGoodWithOthers).count();
        int sharesPracticeResourcesCount = (int) reviews.stream().filter(Review::isSharesPracticeResources).count();
        int managingWellCount = (int) reviews.stream().filter(Review::isManagingWell).count();
        int helpfulCount = (int) reviews.stream().filter(Review::isHelpful).count();
        int goodLearnerCount = (int) reviews.stream().filter(Review::isGoodLearner).count();
        int keepingPromisesCount = (int) reviews.stream().filter(Review::isKeepingPromises).count();
        
        // 백분율 계산 (소수점 1자리까지)
        double practiceHelpedPercentage = calculatePercentage(practiceHelpedCount, totalReviews);
        double goodWithMusicPercentage = calculatePercentage(goodWithMusicCount, totalReviews);
        double goodWithOthersPercentage = calculatePercentage(goodWithOthersCount, totalReviews);
        double sharesPracticeResourcesPercentage = calculatePercentage(sharesPracticeResourcesCount, totalReviews);
        double managingWellPercentage = calculatePercentage(managingWellCount, totalReviews);
        double helpfulPercentage = calculatePercentage(helpfulCount, totalReviews);
        double goodLearnerPercentage = calculatePercentage(goodLearnerCount, totalReviews);
        double keepingPromisesPercentage = calculatePercentage(keepingPromisesCount, totalReviews);
        
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
                .practiceHelpedPercentage(practiceHelpedPercentage)
                .goodWithMusicPercentage(goodWithMusicPercentage)
                .goodWithOthersPercentage(goodWithOthersPercentage)
                .sharesPracticeResourcesPercentage(sharesPracticeResourcesPercentage)
                .managingWellPercentage(managingWellPercentage)
                .helpfulPercentage(helpfulPercentage)
                .goodLearnerPercentage(goodLearnerPercentage)
                .keepingPromisesPercentage(keepingPromisesPercentage)
                .build();
    }
    
    /**
     * 백분율 계산 (소수점 1자리까지)
     */
    private double calculatePercentage(int count, int total) {
        return Math.round((double) count / total * 1000) / 10.0;
    }
} 