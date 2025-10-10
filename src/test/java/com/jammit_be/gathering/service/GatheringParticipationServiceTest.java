package com.jammit_be.gathering.service;

import com.jammit_be.auth.entity.CustomUserDetail;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.common.enums.ParticipantStatus;
import com.jammit_be.gathering.dto.request.GatheringParticipationRequest;
import com.jammit_be.gathering.dto.response.GatheringListResponse;
import com.jammit_be.gathering.dto.response.GatheringParticipationResponse;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.gathering.entity.GatheringSession;
import com.jammit_be.gathering.exception.GatheringException;
import com.jammit_be.gathering.exception.ParticipantException;
import com.jammit_be.gathering.repository.GatheringParticipantRepository;
import com.jammit_be.gathering.repository.GatheringRepository;
import com.jammit_be.user.entity.OauthPlatform;
import com.jammit_be.user.entity.User;
import com.jammit_be.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@DisplayName("GatheringParticipationService 단위 테스트")
class GatheringParticipationServiceTest {

    @Autowired
    private GatheringParticipationService gatheringParticipationService;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private GatheringParticipantRepository gatheringParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;
    private Gathering testGathering;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        testUser = User.builder()
                .email("test@example.com")
                .password("password123!")
                .username("testuser")
                .nickname("테스터")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(testUser);

        otherUser = User.builder()
                .email("other@example.com")
                .password("password123!")
                .username("otheruser")
                .nickname("다른유저")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(otherUser);

        // SecurityContext에 testUser 인증 정보 설정
        setAuthenticatedUser(testUser);

        // 테스트용 모임 생성
        List<GatheringSession> sessions = List.of(
                GatheringSession.create(BandSession.VOCAL, 2),
                GatheringSession.create(BandSession.ELECTRIC_GUITAR, 1)
        );

        testGathering = Gathering.create(
                "테스트 모임",
                "thumbnail.jpg",
                "서울시 강남구",
                "테스트 모임입니다",
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(5),
                Set.of(Genre.ROCK, Genre.JAZZ),
                sessions,
                otherUser // 다른 유저가 주최자
        );
        gatheringRepository.save(testGathering);
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
    @DisplayName("모임 참여 성공 - 정상적으로 모임에 참여 신청")
    void participate_success() {
        // given
        GatheringParticipationRequest request = GatheringParticipationRequest.builder()
                .bandSession(BandSession.VOCAL)
                .introduction("보컬로 참여하고 싶습니다!")
                .build();

        // when
        GatheringParticipationResponse response = gatheringParticipationService.participate(
                testGathering.getId(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGatheringId()).isEqualTo(testGathering.getId());
        assertThat(response.getUserId()).isEqualTo(testUser.getId());
        assertThat(response.getBandSession()).isEqualTo(BandSession.VOCAL);

        // DB에 참여자가 저장되었는지 확인
        GatheringParticipant savedParticipant = gatheringParticipantRepository
                .findByUserAndGathering(testUser, testGathering)
                .orElseThrow();
        assertThat(savedParticipant.getStatus()).isEqualTo(ParticipantStatus.PENDING);
        assertThat(savedParticipant.getName()).isEqualTo(BandSession.VOCAL);
    }

    @Test
    @DisplayName("모임 참여 실패 - 존재하지 않는 모임")
    void participate_gatheringNotFound() {
        // given
        Long nonExistentId = 99999L;
        GatheringParticipationRequest request = GatheringParticipationRequest.builder()
                .bandSession(BandSession.VOCAL)
                .introduction("참여 신청")
                .build();

        // when & then
        assertThatThrownBy(() -> gatheringParticipationService.participate(nonExistentId, request))
                .isInstanceOf(GatheringException.NotFound.class)
                .hasMessage("해당 모임을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("모임 참여 실패 - 이미 신청한 세션")
    void participate_alreadyApplied() {
        // given
        GatheringParticipationRequest request = GatheringParticipationRequest.builder()
                .bandSession(BandSession.VOCAL)
                .introduction("첫 번째 신청")
                .build();
        gatheringParticipationService.participate(testGathering.getId(), request);

        // when & then - 같은 세션에 다시 신청
        assertThatThrownBy(() -> gatheringParticipationService.participate(testGathering.getId(), request))
                .isInstanceOf(ParticipantException.AlreadyAppliedForSession.class)
                .hasMessage("이미 해당 파트로 신청한 이력이 있습니다.");
    }

    @Test
    @DisplayName("모임 참여 실패 - 모집 완료된 모임")
    void participate_notJoinable() {
        // given
        testGathering.cancel(); // 모임 취소
        gatheringRepository.save(testGathering);

        GatheringParticipationRequest request = GatheringParticipationRequest.builder()
                .bandSession(BandSession.VOCAL)
                .introduction("참여 신청")
                .build();

        // when & then
        assertThatThrownBy(() -> gatheringParticipationService.participate(testGathering.getId(), request))
                .isInstanceOf(GatheringException.NotJoinable.class)
                .hasMessage("참가 신청이 불가능한 모임 상태입니다.");
    }

    @Test
    @DisplayName("모임 참여 취소 성공 - 대기 상태의 참여 신청 취소")
    void cancelParticipation_success() {
        // given
        GatheringParticipationRequest request = GatheringParticipationRequest.builder()
                .bandSession(BandSession.VOCAL)
                .introduction("참여 신청")
                .build();
        gatheringParticipationService.participate(testGathering.getId(), request);

        GatheringParticipant participant = gatheringParticipantRepository
                .findByUserAndGathering(testUser, testGathering)
                .orElseThrow();

        // when
        GatheringParticipationResponse response = gatheringParticipationService.cancelParticipation(
                testGathering.getId(), participant.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGatheringId()).isEqualTo(testGathering.getId());

        // DB에서 취소 상태 확인
        GatheringParticipant canceledParticipant = gatheringParticipantRepository
                .findById(participant.getId())
                .orElseThrow();
        assertThat(canceledParticipant.getStatus()).isEqualTo(ParticipantStatus.CANCELED);
    }

    @Test
    @DisplayName("모임 참여 취소 실패 - 존재하지 않는 참가 신청")
    void cancelParticipation_notFound() {
        // given
        Long nonExistentParticipantId = 99999L;

        // when & then
        assertThatThrownBy(() -> gatheringParticipationService.cancelParticipation(
                testGathering.getId(), nonExistentParticipantId))
                .isInstanceOf(ParticipantException.NotFound.class)
                .hasMessage("해당 참가 신청이 없습니다.");
    }

    @Test
    @DisplayName("모임 참여 취소 실패 - 다른 사용자의 참가 신청 취소 시도")
    void cancelParticipation_notOwner() {
        // given - testUser가 참여 신청
        GatheringParticipationRequest request = GatheringParticipationRequest.builder()
                .bandSession(BandSession.VOCAL)
                .introduction("참여 신청")
                .build();
        gatheringParticipationService.participate(testGathering.getId(), request);

        GatheringParticipant participant = gatheringParticipantRepository
                .findByUserAndGathering(testUser, testGathering)
                .orElseThrow();

        // otherUser로 인증 변경
        setAuthenticatedUser(otherUser);

        // when & then - otherUser가 testUser의 참가 신청 취소 시도
        assertThatThrownBy(() -> gatheringParticipationService.cancelParticipation(
                testGathering.getId(), participant.getId()))
                .isInstanceOf(ParticipantException.OnlySelfCanCancel.class)
                .hasMessage("본인의 참가 신청만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("모임 참여 취소 실패 - 이미 취소된 참가 신청")
    void cancelParticipation_alreadyCanceled() {
        // given
        GatheringParticipationRequest request = GatheringParticipationRequest.builder()
                .bandSession(BandSession.VOCAL)
                .introduction("참여 신청")
                .build();
        gatheringParticipationService.participate(testGathering.getId(), request);

        GatheringParticipant participant = gatheringParticipantRepository
                .findByUserAndGathering(testUser, testGathering)
                .orElseThrow();

        // 첫 번째 취소
        gatheringParticipationService.cancelParticipation(testGathering.getId(), participant.getId());

        // when & then - 두 번째 취소 시도
        assertThatThrownBy(() -> gatheringParticipationService.cancelParticipation(
                testGathering.getId(), participant.getId()))
                .isInstanceOf(ParticipantException.AlreadyCanceled.class)
                .hasMessage("이미 취소된 참가 신청입니다.");
    }

    @Test
    @DisplayName("승인된 참여 취소 시 세션 카운트 감소")
    void cancelParticipation_decrementSessionCount() {
        // given
        GatheringParticipationRequest request = GatheringParticipationRequest.builder()
                .bandSession(BandSession.VOCAL)
                .introduction("참여 신청")
                .build();
        gatheringParticipationService.participate(testGathering.getId(), request);

        GatheringParticipant participant = gatheringParticipantRepository
                .findByUserAndGathering(testUser, testGathering)
                .orElseThrow();

        // 참가 승인
        participant.approve();
        GatheringSession session = testGathering.getSession(BandSession.VOCAL);
        int initialCount = session.getCurrentCount();
        session.incrementCurrentCount();
        gatheringParticipantRepository.save(participant);

        // when
        gatheringParticipationService.cancelParticipation(testGathering.getId(), participant.getId());

        // then
        assertThat(session.getCurrentCount()).isEqualTo(initialCount); // 카운트 감소 확인
    }

    @Test
    @DisplayName("내가 신청한 모임 목록 조회 성공")
    void getMyParticipations_success() {
        // given
        GatheringParticipationRequest request1 = GatheringParticipationRequest.builder()
                .bandSession(BandSession.VOCAL)
                .introduction("참여 신청 1")
                .build();
        gatheringParticipationService.participate(testGathering.getId(), request1);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        GatheringListResponse response = gatheringParticipationService.getMyParticipations(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGatherings()).isNotEmpty();
        assertThat(response.getGatherings()).hasSize(1);
    }

    @Test
    @DisplayName("CONFIRMED 모임에서 승인된 참여 취소 시 RECRUITING으로 상태 변경")
    void cancelParticipation_confirmedToRecruiting() {
        // given
        GatheringParticipationRequest request = GatheringParticipationRequest.builder()
                .bandSession(BandSession.VOCAL)
                .introduction("참여 신청")
                .build();
        gatheringParticipationService.participate(testGathering.getId(), request);

        GatheringParticipant participant = gatheringParticipantRepository
                .findByUserAndGathering(testUser, testGathering)
                .orElseThrow();

        // 참가 승인 및 모임 확정
        participant.approve();
        GatheringSession session = testGathering.getSession(BandSession.VOCAL);
        session.incrementCurrentCount();
        testGathering.confirm();
        gatheringParticipantRepository.save(participant);

        assertThat(testGathering.getStatus()).isEqualTo(GatheringStatus.CONFIRMED);

        // when - 승인된 참가 취소
        gatheringParticipationService.cancelParticipation(testGathering.getId(), participant.getId());

        // then - 빈 자리가 생겼으므로 RECRUITING으로 변경
        assertThat(testGathering.getStatus()).isEqualTo(GatheringStatus.RECRUITING);
    }
}
