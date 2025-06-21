package com.jammit_be.review.service;

import com.jammit_be.auth.util.AuthUtil;
import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.dto.GatheringParticipantSummary;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.repository.GatheringParticipantRepository;
import com.jammit_be.gathering.repository.GatheringRepository;
import com.jammit_be.review.dto.request.CreateReviewRequest;
import com.jammit_be.review.dto.response.ReviewResponse;
import com.jammit_be.review.dto.response.ReviewStatisticsResponse;
import com.jammit_be.review.dto.response.ReviewUserPageResponse;
import com.jammit_be.review.entity.Review;
import com.jammit_be.review.repository.ReviewRepository;
import com.jammit_be.user.dto.response.UserResponse;
import com.jammit_be.user.entity.User;
import com.jammit_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jammit_be.common.dto.response.PageResponse;
import com.jammit_be.review.dto.response.UnwrittenReviewProjection;
import com.jammit_be.review.dto.response.UnwrittenReviewListResponse;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringParticipantRepository gatheringParticipantRepository;

    /**
     * 리뷰 생성
     */
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        User reviewer = AuthUtil.getUserInfo();
        
        // 1. 리뷰 대상자 확인
        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new AlertException("리뷰 대상자를 찾을 수 없습니다."));

        // 2. 모임 확인
        Gathering gathering = gatheringRepository.findById(request.getGatheringId())
                .orElseThrow(() -> new AlertException("모임을 찾을 수 없습니다."));
                
        // 3. 모임 상태 확인 - 모임이 완료 상태인지 확인
        if (gathering.getStatus() != GatheringStatus.COMPLETED) {
            throw new AlertException("완료된 모임만 리뷰를 작성할 수 있습니다.");
        }
        
        // 4. 리뷰 작성자가 해당 모임에 참여 완료했는지 확인
        boolean reviewerParticipated = gatheringParticipantRepository.isParticipationCompleted(reviewer, gathering);
        if (!reviewerParticipated) {
            throw new AlertException("모임에 참여 완료한 사용자만 리뷰를 작성할 수 있습니다.");
        }
        
        // 5. 리뷰 대상자가 해당 모임에 참여 완료했는지 확인
        boolean revieweeParticipated = gatheringParticipantRepository.isParticipationCompleted(reviewee, gathering);
        if (!revieweeParticipated) {
            throw new AlertException("모임에 참여 완료한 사용자에게만 리뷰를 작성할 수 있습니다.");
        }

        // 6. 이미 해당 모임에서 해당 사용자에 대한 리뷰를 작성했는지 확인
        reviewRepository.findByReviewerIdAndRevieweeIdAndGatheringId(
                        reviewer.getId(), request.getRevieweeId(), request.getGatheringId())
                .ifPresent(r -> {
                    throw new AlertException("이미 이 모임에서 해당 사용자에 대한 리뷰를 작성했습니다.");
                });

        // 7. 자기 자신에게 리뷰를 작성하는지 확인
        if (reviewer.getId().equals(request.getRevieweeId())) {
            throw new AlertException("자기 자신에게 리뷰를 작성할 수 없습니다.");
        }

        // 8. 리뷰 생성
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
     * 사용자가 받은 리뷰 목록 페이지네이션 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getReviewsByRevieweeWithPagination(int page, int pageSize) {
        Long revieweeId = AuthUtil.getUserInfo().getId();
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Review> reviewPage = reviewRepository.findAllByRevieweeId(revieweeId, pageable);
        
        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(ReviewResponse::of)
                .collect(Collectors.toList());
        
        return PageResponse.<ReviewResponse>builder()
                .content(content)
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .build();
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

    /**
     * 특정 모임의 주최자가 해당 모임 참가자의 리뷰 통합 정보를 조회
     * @param userId 리뷰/통계를 조회할 대상 참가자의 유저 ID
     * @param gatheringId 해당 모임의 PK (주최자 권한 체크 용도)
     * @return  대상 유저의 프로필, 리뷰 통계, 리뷰 목록을 포함한 통합 응답 DTO
     */
    @Transactional(readOnly = true)
    public ReviewUserPageResponse getReviewUserPage(Long userId, Long gatheringId) {
        // 모임, 주최자, 참가자 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new AlertException("모임을 찾을 수 없습니다."));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new AlertException("유저를 찾을 수 없습니다."));

        // 주최자 권한 체크
        User owner = AuthUtil.getUserInfo();
        if(!gathering.getCreatedBy().getId().equals(owner.getId())) {
            throw new AlertException("모임 주최자만 접근할 수 있습니다.");
        }

        // 유저 정보 생성
        UserResponse userInfo = UserResponse.of(targetUser);

        // 리뷰 리스트
        List<Review> reviews = reviewRepository.findAllByRevieweeId(userId);
        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : reviews) {
            reviewResponses.add(ReviewResponse.of(review));
        }

        ReviewStatisticsResponse reviewStatistics = ReviewStatisticsResponse.of(reviews);

        return ReviewUserPageResponse.builder()
                .userInfo(userInfo)
                .statistics(reviewStatistics)
                .reviews(reviewResponses)
                .build();
    }

    /**
     * 내가 참여한 COMPLETED 모임들 중, 각 모임별로 내가 리뷰를 작성하지 않은 참가자 목록 반환 (쿼리 기반)
     */
    @Transactional(readOnly = true)
    public List<UnwrittenReviewListResponse> getUnwrittenReviewList() {
        User me = AuthUtil.getUserInfo();
        List<UnwrittenReviewProjection> projections = gatheringParticipantRepository.findUnwrittenReviewsByUser(me);
        // 모임별로 그룹핑
        Map<Long, List<UnwrittenReviewProjection>> grouped = projections.stream()
                .collect(Collectors.groupingBy(UnwrittenReviewProjection::getGatheringId));
        List<UnwrittenReviewListResponse> result = new ArrayList<>();
        for (Map.Entry<Long, List<UnwrittenReviewProjection>> entry : grouped.entrySet()) {
            List<GatheringParticipantSummary> participants = entry.getValue().stream()
                    .map(p -> GatheringParticipantSummary.builder()
                            .participantId(p.getParticipantId())
                            .userId(p.getUserId())
                            .userNickname(p.getUserNickname())
                            .userEmail(p.getUserEmail())
                            .bandSession(com.jammit_be.common.enums.BandSession.valueOf(p.getBandSession()))
                            .status(com.jammit_be.common.enums.ParticipantStatus.valueOf(p.getStatus()))
                            .createdAt(p.getCreatedAt())
                            .introduction(p.getIntroduction())
                            .build())
                    .collect(Collectors.toList());
            UnwrittenReviewProjection first = entry.getValue().get(0);
            result.add(new UnwrittenReviewListResponse(
                    first.getGatheringId(),
                    first.getGatheringName(),
                    first.getGatheringThumbnail(),
                    participants
            ));
        }
        return result;
    }
} 