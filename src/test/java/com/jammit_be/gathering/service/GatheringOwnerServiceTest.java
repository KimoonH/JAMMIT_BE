package com.jammit_be.gathering.service;

import com.jammit_be.auth.entity.CustomUserDetail;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.common.enums.ParticipantStatus;
import com.jammit_be.gathering.dto.response.GatheringParticipantListResponse;
import com.jammit_be.gathering.dto.response.GatheringParticipationResponse;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.gathering.entity.GatheringSession;
import com.jammit_be.gathering.exception.GatheringException;
import com.jammit_be.gathering.exception.OwnerException;
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
@DisplayName("GatheringOwnerService 단위 테스트")
class GatheringOwnerServiceTest {

    @Autowired
    private GatheringOwnerService gatheringOwnerService;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private GatheringParticipantRepository gatheringParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User participant;
    private Gathering testGathering;
    private GatheringParticipant testParticipant;

    @BeforeEach
    void setUp() {
        // 주최자 유저 생성
        owner = User.builder()
                .email("owner@example.com")
                .password("password123!")
                .username("owner")
                .nickname("주최자")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(owner);

        // 참가자 유저 생성
        participant = User.builder()
                .email("participant@example.com")
                .password("password123!")
                .username("participant")
                .nickname("참가자")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(participant);

        // SecurityContext에 owner 인증 정보 설정
        setAuthenticatedUser(owner);

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
                owner
        );
        gatheringRepository.save(testGathering);

        // 테스트용 참가자 생성 (PENDING 상태)
        testParticipant = GatheringParticipant.pending(
                participant,
                testGathering,
                BandSession.VOCAL,
                "참여하고 싶습니다!"
        );
        gatheringParticipantRepository.save(testParticipant);
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
    @DisplayName("참가 승인 성공 - 주최자가 참가 신청 승인")
    void approveParticipation_success() {
        // when
        GatheringParticipationResponse response = gatheringOwnerService.approveParticipation(
                testGathering.getId(), testParticipant.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGatheringId()).isEqualTo(testGathering.getId());

        // DB에서 승인 상태 확인
        GatheringParticipant approvedParticipant = gatheringParticipantRepository
                .findById(testParticipant.getId())
                .orElseThrow();
        assertThat(approvedParticipant.getStatus()).isEqualTo(ParticipantStatus.APPROVED);

        // 세션 카운트 증가 확인
        GatheringSession session = testGathering.getSession(BandSession.VOCAL);
        assertThat(session.getCurrentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("참가 승인 실패 - 존재하지 않는 모임")
    void approveParticipation_gatheringNotFound() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> gatheringOwnerService.approveParticipation(
                nonExistentId, testParticipant.getId()))
                .isInstanceOf(GatheringException.NotFound.class)
                .hasMessage("해당 모임을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("참가 승인 실패 - 존재하지 않는 참가자")
    void approveParticipation_participantNotFound() {
        // given
        Long nonExistentParticipantId = 99999L;

        // when & then
        assertThatThrownBy(() -> gatheringOwnerService.approveParticipation(
                testGathering.getId(), nonExistentParticipantId))
                .isInstanceOf(ParticipantException.NotFound.class)
                .hasMessage("해당 참가 신청이 없습니다.");
    }

    @Test
    @DisplayName("참가 승인 실패 - 주최자가 아닌 사용자")
    void approveParticipation_notOwner() {
        // given - participant로 인증 변경
        setAuthenticatedUser(participant);

        // when & then
        assertThatThrownBy(() -> gatheringOwnerService.approveParticipation(
                testGathering.getId(), testParticipant.getId()))
                .isInstanceOf(OwnerException.NoApprovalPermission.class)
                .hasMessage("승인 권한이 없습니다.");
    }

    @Test
    @DisplayName("참가 승인 실패 - 이미 승인된 참가자")
    void approveParticipation_alreadyApproved() {
        // given - 첫 번째 승인
        gatheringOwnerService.approveParticipation(testGathering.getId(), testParticipant.getId());

        // when & then - 두 번째 승인 시도
        assertThatThrownBy(() -> gatheringOwnerService.approveParticipation(
                testGathering.getId(), testParticipant.getId()))
                .isInstanceOf(OwnerException.AlreadyApproved.class)
                .hasMessage("이미 승인된 참가자입니다.");
    }

    @Test
    @DisplayName("참가 승인 실패 - 이미 취소된 참가자")
    void approveParticipation_alreadyCanceled() {
        // given
        testParticipant.cancel();
        gatheringParticipantRepository.save(testParticipant);

        // when & then
        assertThatThrownBy(() -> gatheringOwnerService.approveParticipation(
                testGathering.getId(), testParticipant.getId()))
                .isInstanceOf(OwnerException.AlreadyCanceled.class)
                .hasMessage("이미 취소된 참가자입니다.");
    }

    @Test
    @DisplayName("참가 승인 실패 - 세션 정원 초과")
    void approveParticipation_sessionFull() {
        // given - VOCAL 세션 정원 2명 채우기
        User participant2 = User.builder()
                .email("participant2@example.com")
                .password("password123!")
                .username("participant2")
                .nickname("참가자2")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(participant2);

        GatheringParticipant participant2Application = GatheringParticipant.pending(
                participant2,
                testGathering,
                BandSession.VOCAL,
                "참여 신청2"
        );
        gatheringParticipantRepository.save(participant2Application);

        // 두 명 승인
        gatheringOwnerService.approveParticipation(testGathering.getId(), testParticipant.getId());
        gatheringOwnerService.approveParticipation(testGathering.getId(), participant2Application.getId());

        // 세 번째 참가자 생성
        User participant3 = User.builder()
                .email("participant3@example.com")
                .password("password123!")
                .username("participant3")
                .nickname("참가자3")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(participant3);

        GatheringParticipant participant3Application = GatheringParticipant.pending(
                participant3,
                testGathering,
                BandSession.VOCAL,
                "참여 신청3"
        );
        gatheringParticipantRepository.save(participant3Application);

        // when & then - 정원 초과
        assertThatThrownBy(() -> gatheringOwnerService.approveParticipation(
                testGathering.getId(), participant3Application.getId()))
                .isInstanceOf(OwnerException.SessionFull.class)
                .hasMessage("해당 세션의 모집 인원이 마감되었습니다.");
    }

    @Test
    @DisplayName("모든 세션이 채워지면 모임 상태가 CONFIRMED로 변경")
    void approveParticipation_allSessionsFilled_statusConfirmed() {
        // given - VOCAL 2명, ELECTRIC_GUITAR 1명 신청
        User participant2 = User.builder()
                .email("participant2@example.com")
                .password("password123!")
                .username("participant2")
                .nickname("참가자2")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(participant2);

        User participant3 = User.builder()
                .email("participant3@example.com")
                .password("password123!")
                .username("participant3")
                .nickname("참가자3")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(participant3);

        GatheringParticipant vocalParticipant2 = GatheringParticipant.pending(
                participant2,
                testGathering,
                BandSession.VOCAL,
                "참여 신청2"
        );
        gatheringParticipantRepository.save(vocalParticipant2);

        GatheringParticipant guitarParticipant = GatheringParticipant.pending(
                participant3,
                testGathering,
                BandSession.ELECTRIC_GUITAR,
                "참여 신청3"
        );
        gatheringParticipantRepository.save(guitarParticipant);

        // when - 모든 세션 승인
        gatheringOwnerService.approveParticipation(testGathering.getId(), testParticipant.getId());
        gatheringOwnerService.approveParticipation(testGathering.getId(), vocalParticipant2.getId());
        gatheringOwnerService.approveParticipation(testGathering.getId(), guitarParticipant.getId());

        // then
        Gathering gathering = gatheringRepository.findById(testGathering.getId()).orElseThrow();
        assertThat(gathering.getStatus()).isEqualTo(GatheringStatus.CONFIRMED);
    }

    @Test
    @DisplayName("참가 거절 성공 - 주최자가 참가 신청 거절")
    void rejectParticipation_success() {
        // when
        GatheringParticipationResponse response = gatheringOwnerService.rejectParticipation(
                testGathering.getId(), testParticipant.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGatheringId()).isEqualTo(testGathering.getId());

        // DB에서 거절 상태 확인
        GatheringParticipant rejectedParticipant = gatheringParticipantRepository
                .findById(testParticipant.getId())
                .orElseThrow();
        assertThat(rejectedParticipant.getStatus()).isEqualTo(ParticipantStatus.REJECTED);
    }

    @Test
    @DisplayName("참가 거절 실패 - 주최자가 아닌 사용자")
    void rejectParticipation_notOwner() {
        // given
        setAuthenticatedUser(participant);

        // when & then
        assertThatThrownBy(() -> gatheringOwnerService.rejectParticipation(
                testGathering.getId(), testParticipant.getId()))
                .isInstanceOf(OwnerException.OnlyOwnerCanProcess.class)
                .hasMessage("모임 주최자만 처리할 수 있습니다.");
    }

    @Test
    @DisplayName("참가 거절 실패 - 이미 승인된 참가자")
    void rejectParticipation_alreadyApproved() {
        // given
        gatheringOwnerService.approveParticipation(testGathering.getId(), testParticipant.getId());

        // when & then
        assertThatThrownBy(() -> gatheringOwnerService.rejectParticipation(
                testGathering.getId(), testParticipant.getId()))
                .isInstanceOf(OwnerException.AlreadyApprovedApplication.class)
                .hasMessage("이미 승인된 신청입니다.");
    }

    @Test
    @DisplayName("모임 완료 처리 성공")
    void completeGathering_success() {
        // given - 모임을 CONFIRMED 상태로 변경
        testGathering.confirm();
        gatheringRepository.save(testGathering);

        // 참가자 승인
        gatheringOwnerService.approveParticipation(testGathering.getId(), testParticipant.getId());

        // when
        gatheringOwnerService.completeGathering(testGathering.getId());

        // then
        Gathering completedGathering = gatheringRepository.findById(testGathering.getId()).orElseThrow();
        assertThat(completedGathering.getStatus()).isEqualTo(GatheringStatus.COMPLETED);

        // 승인된 참가자도 COMPLETED 상태로 변경되었는지 확인
        GatheringParticipant completedParticipant = gatheringParticipantRepository
                .findById(testParticipant.getId())
                .orElseThrow();
        assertThat(completedParticipant.getStatus()).isEqualTo(ParticipantStatus.COMPLETED);
    }

    @Test
    @DisplayName("모임 완료 처리 실패 - 주최자가 아닌 사용자")
    void completeGathering_notOwner() {
        // given
        testGathering.confirm();
        gatheringRepository.save(testGathering);
        setAuthenticatedUser(participant);

        // when & then
        assertThatThrownBy(() -> gatheringOwnerService.completeGathering(testGathering.getId()))
                .isInstanceOf(OwnerException.OnlyOwnerCanComplete.class)
                .hasMessage("모임 주최자만 완료 처리할 수 있습니다.");
    }

    @Test
    @DisplayName("모임 완료 처리 실패 - CONFIRMED 상태가 아닌 모임")
    void completeGathering_notConfirmed() {
        // given - RECRUITING 상태 유지

        // when & then
        assertThatThrownBy(() -> gatheringOwnerService.completeGathering(testGathering.getId()))
                .isInstanceOf(OwnerException.OnlyConfirmedCanComplete.class)
                .hasMessage("멤버 모집이 완료된 모임만 완료 처리할 수 있습니다.");
    }

    @Test
    @DisplayName("참가자 목록 조회 성공")
    void findParticipants_success() {
        // given - 추가 참가자 생성
        User participant2 = User.builder()
                .email("participant2@example.com")
                .password("password123!")
                .username("participant2")
                .nickname("참가자2")
                .oauthPlatform(OauthPlatform.NONE)
                .build();
        userRepository.save(participant2);

        GatheringParticipant participant2Application = GatheringParticipant.pending(
                participant2,
                testGathering,
                BandSession.ELECTRIC_GUITAR,
                "참여 신청2"
        );
        gatheringParticipantRepository.save(participant2Application);

        // when
        GatheringParticipantListResponse response = gatheringOwnerService.findParticipants(testGathering.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getParticipants()).hasSize(2);
        assertThat(response.getParticipants()).anyMatch(p -> p.getUserNickname().equals("참가자"));
        assertThat(response.getParticipants()).anyMatch(p -> p.getUserNickname().equals("참가자2"));
    }
}
