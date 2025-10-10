package com.jammit_be.review.service;

import com.jammit_be.auth.entity.CustomUserDetail;
import com.jammit_be.common.dto.response.PageResponse;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.common.enums.ParticipantStatus;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.gathering.entity.GatheringSession;
import com.jammit_be.gathering.repository.GatheringParticipantRepository;
import com.jammit_be.gathering.repository.GatheringRepository;
import com.jammit_be.review.dto.request.CreateReviewRequest;
import com.jammit_be.review.dto.response.ReviewResponse;
import com.jammit_be.review.dto.response.ReviewStatisticsResponse;
import com.jammit_be.review.dto.response.ReviewUserPageResponse;
import com.jammit_be.review.entity.Review;
import com.jammit_be.review.repository.ReviewRepository;
import com.jammit_be.user.entity.OauthPlatform;
import com.jammit_be.user.entity.User;
import com.jammit_be.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("ReviewService 단위 테스트")
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private GatheringParticipantRepository gatheringParticipantRepository;

    private User reviewer;
    private User reviewee;
    private User owner;
    private Gathering completedGathering;

    @BeforeEach
    void setUp() {
        // 리뷰 작성자
        reviewer = User.builder()
                .email("reviewer@example.com")
                .password("password123!")
                .username("reviewer")
                .nickname("리뷰어")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(reviewer);

        // 리뷰 대상자
        reviewee = User.builder()
                .email("reviewee@example.com")
                .password("password123!")
                .username("reviewee")
                .nickname("리뷰이")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        reviewee.updatePreferredGenres(List.of(Genre.ROCK));
        reviewee.updatePreferredBandSessions(List.of(BandSession.VOCAL));
        userRepository.save(reviewee);

        // 모임 주최자
        owner = User.builder()
                .email("owner@example.com")
                .password("password123!")
                .username("owner")
                .nickname("주최자")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(owner);

        // 완료된 모임 생성 - owner로 인증하여 createdBy가 owner로 설정되도록 함
        setAuthenticatedUser(owner);

        List<GatheringSession> sessions = List.of(
                GatheringSession.create(BandSession.VOCAL, 2)
        );

        completedGathering = Gathering.create(
                "완료된 모임",
                "thumbnail.jpg",
                "서울시 강남구",
                "완료된 모임입니다",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(3),
                Set.of(Genre.ROCK),
                sessions,
                owner
        );
        completedGathering.confirm();
        completedGathering.complete();
        gatheringRepository.save(completedGathering);

        // 기본 인증을 reviewer로 변경 (리뷰 작성을 위해)
        setAuthenticatedUser(reviewer);

        // 참가자 등록 (COMPLETED 상태)
        GatheringParticipant reviewerParticipant = GatheringParticipant.pending(reviewer, completedGathering, BandSession.VOCAL, "참여");
        reviewerParticipant.approve();
        reviewerParticipant.complete();
        gatheringParticipantRepository.save(reviewerParticipant);

        GatheringParticipant revieweeParticipant = GatheringParticipant.pending(reviewee, completedGathering, BandSession.VOCAL, "참여");
        revieweeParticipant.approve();
        revieweeParticipant.complete();
        gatheringParticipantRepository.save(revieweeParticipant);
    }

    private void setAuthenticatedUser(User user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        CustomUserDetail userDetail = new CustomUserDetail(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetail,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("리뷰 생성 성공")
    void createReview_success() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("좋은 합주였습니다!")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        // when
        ReviewResponse response = reviewService.createReview(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("좋은 합주였습니다!");
        assertThat(response.getReviewerId()).isEqualTo(reviewer.getId());
        assertThat(response.getRevieweeId()).isEqualTo(reviewee.getId());

        // DB 확인
        Review savedReview = reviewRepository.findById(response.getId()).orElseThrow();
        assertThat(savedReview.isPracticeHelped()).isTrue();
        assertThat(savedReview.isGoodWithMusic()).isTrue();
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 존재하지 않는 대상자")
    void createReview_revieweeNotFound() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(99999L)
                .gatheringId(completedGathering.getId())
                .content("리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(AlertException.class)
                .hasMessage("리뷰 대상자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 완료되지 않은 모임")
    void createReview_gatheringNotCompleted() {
        // given - 진행 중인 모임
        List<GatheringSession> sessions = List.of(
                GatheringSession.create(BandSession.VOCAL, 2)
        );

        Gathering ongoingGathering = Gathering.create(
                "진행 중 모임",
                "thumbnail.jpg",
                "서울시 강남구",
                "진행 중",
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(5),
                Set.of(Genre.ROCK),
                sessions,
                owner
        );
        gatheringRepository.save(ongoingGathering);

        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(ongoingGathering.getId())
                .content("리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(AlertException.class)
                .hasMessage("완료된 모임만 리뷰를 작성할 수 있습니다.");
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 자기 자신에게 리뷰 작성")
    void createReview_selfReview() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(reviewer.getId()) // 자기 자신
                .gatheringId(completedGathering.getId())
                .content("리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(AlertException.class)
                .hasMessage("자기 자신에게 리뷰를 작성할 수 없습니다.");
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 중복 리뷰")
    void createReview_duplicateReview() {
        // given - 첫 번째 리뷰 작성
        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("첫 번째 리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        reviewService.createReview(request);

        // when & then - 같은 대상에게 두 번째 리뷰 시도
        CreateReviewRequest duplicateRequest = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("두 번째 리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        assertThatThrownBy(() -> reviewService.createReview(duplicateRequest))
                .isInstanceOf(AlertException.class)
                .hasMessage("이미 이 모임에서 해당 사용자에 대한 리뷰를 작성했습니다.");
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_success() {
        // given - 리뷰 생성
        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("삭제할 리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        ReviewResponse createdReview = reviewService.createReview(request);

        // when
        reviewService.deleteReview(createdReview.getId());

        // then
        assertThat(reviewRepository.findById(createdReview.getId())).isEmpty();
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 작성자가 아닌 사용자")
    void deleteReview_notAuthor() {
        // given - reviewer가 리뷰 작성
        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        ReviewResponse createdReview = reviewService.createReview(request);

        // reviewee로 인증 변경
        setAuthenticatedUser(reviewee);

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(createdReview.getId()))
                .isInstanceOf(AlertException.class)
                .hasMessage("리뷰를 삭제할 권한이 없습니다.");
    }

    @Test
    @DisplayName("작성한 리뷰 목록 조회")
    void getReviewsByReviewer_success() {
        // given - 여러 리뷰 작성
        CreateReviewRequest request1 = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("리뷰 1")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        reviewService.createReview(request1);

        // when
        List<ReviewResponse> reviews = reviewService.getReviewsByReviewer();

        // then
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getReviewerId()).isEqualTo(reviewer.getId());
    }

    @Test
    @DisplayName("받은 리뷰 목록 조회")
    void getReviewsByReviewee_success() {
        // given - reviewee가 리뷰를 받음
        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("좋은 리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        reviewService.createReview(request);

        // reviewee로 인증 변경
        setAuthenticatedUser(reviewee);

        // when
        List<ReviewResponse> reviews = reviewService.getReviewsByReviewee();

        // then
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getRevieweeId()).isEqualTo(reviewee.getId());
    }

    @Test
    @DisplayName("받은 리뷰 페이지네이션 조회")
    void getReviewsByRevieweeWithPagination_success() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        reviewService.createReview(request);

        setAuthenticatedUser(reviewee);

        // when
        PageResponse<ReviewResponse> response = reviewService.getReviewsByRevieweeWithPagination(0, 10);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("리뷰 통계 조회 - 리뷰가 있는 경우")
    void getReviewStatistics_withReviews() {
        // given - 2개의 리뷰 생성
        CreateReviewRequest request1 = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("리뷰 1")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(false)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(false)
                .isKeepingPromises(true)
                .build();

        reviewService.createReview(request1);

        setAuthenticatedUser(reviewee);

        // when
        ReviewStatisticsResponse stats = reviewService.getReviewStatistics();

        // then
        assertThat(stats.getTotalReviews()).isEqualTo(1);
        assertThat(stats.getPracticeHelpedCount()).isEqualTo(1);
        assertThat(stats.getGoodWithMusicCount()).isEqualTo(1);
        assertThat(stats.getGoodWithOthersCount()).isEqualTo(0);
        assertThat(stats.getPracticeHelpedPercentage()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("리뷰 통계 조회 - 리뷰가 없는 경우")
    void getReviewStatistics_noReviews() {
        // given
        setAuthenticatedUser(reviewee);

        // when
        ReviewStatisticsResponse stats = reviewService.getReviewStatistics();

        // then
        assertThat(stats.getTotalReviews()).isEqualTo(0);
        assertThat(stats.getPracticeHelpedCount()).isEqualTo(0);
        assertThat(stats.getPracticeHelpedPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("모임별 리뷰 목록 조회")
    void getReviewsByGathering_success() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        reviewService.createReview(request);

        // when
        List<ReviewResponse> reviews = reviewService.getReviewsByGathering(completedGathering.getId());

        // then
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getGatheringId()).isEqualTo(completedGathering.getId());
    }

    @Test
    @DisplayName("유저 리뷰 페이지 조회 성공 - 주최자 권한")
    void getReviewUserPage_success() {
        // given - 리뷰 생성
        CreateReviewRequest request = CreateReviewRequest.builder()
                .revieweeId(reviewee.getId())
                .gatheringId(completedGathering.getId())
                .content("리뷰")
                .isPracticeHelped(true)
                .isGoodWithMusic(true)
                .isGoodWithOthers(true)
                .isSharesPracticeResources(false)
                .isManagingWell(false)
                .isHelpful(true)
                .isGoodLearner(true)
                .isKeepingPromises(true)
                .build();

        reviewService.createReview(request);

        // owner로 인증 변경
        setAuthenticatedUser(owner);

        // when
        ReviewUserPageResponse response = reviewService.getReviewUserPage(reviewee.getId(), completedGathering.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserInfo().getId()).isEqualTo(reviewee.getId());
        assertThat(response.getStatistics().getTotalReviews()).isGreaterThanOrEqualTo(1);
        assertThat(response.getReviews()).isNotEmpty();
    }

    @Test
    @DisplayName("유저 리뷰 페이지 조회 실패 - 주최자가 아닌 사용자")
    void getReviewUserPage_notOwner() {
        // given - reviewer(주최자가 아님)로 인증 (setUp에서 이미 설정됨)
        setAuthenticatedUser(reviewer);

        // when & then
        assertThatThrownBy(() -> reviewService.getReviewUserPage(reviewee.getId(), completedGathering.getId()))
                .isInstanceOf(AlertException.class)
                .hasMessage("모임 주최자만 접근할 수 있습니다.");
    }
}
