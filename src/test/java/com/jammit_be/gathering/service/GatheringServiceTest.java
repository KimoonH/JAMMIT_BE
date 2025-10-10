package com.jammit_be.gathering.service;

import com.jammit_be.auth.entity.CustomUserDetail;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.gathering.dto.request.GatheringCreateRequest;
import com.jammit_be.gathering.dto.request.GatheringSessionRequest;
import com.jammit_be.gathering.dto.request.GatheringUpdateRequest;
import com.jammit_be.gathering.dto.response.GatheringCreateResponse;
import com.jammit_be.gathering.dto.response.GatheringDetailResponse;
import com.jammit_be.gathering.dto.response.GatheringListResponse;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.gathering.entity.GatheringSession;
import com.jammit_be.gathering.exception.GatheringException;
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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("GatheringService 단위 테스트")
class GatheringServiceTest {

    @Autowired
    private GatheringService gatheringService;

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
                testUser
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
    @DisplayName("모임 생성 성공 - 올바른 요청으로 모임 생성")
    void createGathering_success() {
        // given
        List<GatheringSessionRequest> sessionRequests = List.of(
                GatheringSessionRequest.builder()
                        .bandSession(BandSession.VOCAL)
                        .recruitCount(2)
                        .build(),
                GatheringSessionRequest.builder()
                        .bandSession(BandSession.ELECTRIC_GUITAR)
                        .recruitCount(1)
                        .build()
        );

        GatheringCreateRequest request = GatheringCreateRequest.builder()
                .name("새로운 모임")
                .thumbnail("new_thumbnail.jpg")
                .place("서울시 서초구")
                .description("새로운 모임입니다")
                .gatheringDateTime(LocalDateTime.now().plusDays(10))
                .recruitDateTime(LocalDateTime.now().plusDays(8))
                .genres(Set.of(Genre.ROCK))
                .gatheringSessions(sessionRequests)
                .build();

        // when
        GatheringCreateResponse response = gatheringService.createGathering(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("새로운 모임");
        assertThat(response.getStatus()).isEqualTo(GatheringStatus.RECRUITING);

        // DB에 저장되었는지 확인
        Gathering savedGathering = gatheringRepository.findById(response.getId()).orElseThrow();
        assertThat(savedGathering.getName()).isEqualTo("새로운 모임");
        assertThat(savedGathering.getPlace()).isEqualTo("서울시 서초구");
        assertThat(savedGathering.getCreatedBy()).isEqualTo(testUser);

        // 주최자가 참여자로 자동 등록되었는지 확인
        Optional<GatheringParticipant> hostParticipant = gatheringParticipantRepository
                .findByUserAndGathering(testUser, savedGathering);
        assertThat(hostParticipant).isPresent();
        assertThat(hostParticipant.get().getStatus()).isEqualTo(com.jammit_be.common.enums.ParticipantStatus.COMPLETED);
        assertThat(hostParticipant.get().getName()).isNull(); // 주최자는 세션이 null
    }

    @Test
    @DisplayName("모임 목록 조회 성공 - 필터링 없이 전체 조회")
    void findGatherings_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        GatheringListResponse response = gatheringService.findGatherings(null, null, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGatherings()).isNotEmpty();
        assertThat(response.getTotalElements()).isGreaterThan(0);
    }

    @Test
    @DisplayName("모임 목록 조회 성공 - 장르 필터링")
    void findGatherings_withGenreFilter_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Genre> genres = List.of(Genre.ROCK);

        // when
        GatheringListResponse response = gatheringService.findGatherings(genres, null, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGatherings()).isNotEmpty();
        assertThat(response.getGatherings()).allSatisfy(gathering ->
                assertThat(gathering.getGenres()).contains(Genre.ROCK)
        );
    }

    @Test
    @DisplayName("모임 상세 조회 성공")
    void getGatheringDetail_success() {
        // when
        GatheringDetailResponse response = gatheringService.getGatheringDetail(testGathering.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testGathering.getId());
        assertThat(response.getName()).isEqualTo("테스트 모임");
        assertThat(response.getPlace()).isEqualTo("서울시 강남구");
        assertThat(response.getSessions()).hasSize(2);
        assertThat(response.getGenres()).containsExactlyInAnyOrder(Genre.ROCK, Genre.JAZZ);
    }

    @Test
    @DisplayName("모임 상세 조회 실패 - 존재하지 않는 모임")
    void getGatheringDetail_notFound() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> gatheringService.getGatheringDetail(nonExistentId))
                .isInstanceOf(GatheringException.NotFound.class)
                .hasMessage("해당 모임을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("모임 수정 성공 - 주최자가 모임 정보 수정")
    void updateGathering_success() {
        // given
        List<GatheringSessionRequest> updatedSessions = List.of(
                GatheringSessionRequest.builder()
                        .bandSession(BandSession.VOCAL)
                        .recruitCount(3)
                        .build()
        );

        GatheringUpdateRequest request = GatheringUpdateRequest.builder()
                .name("수정된 모임")
                .place("서울시 종로구")
                .description("수정된 설명")
                .thumbnail("updated_thumbnail.jpg")
                .gatheringDateTime(LocalDateTime.now().plusDays(10))
                .recruitDeadline(LocalDateTime.now().plusDays(8))
                .genres(Set.of(Genre.POP))
                .gatheringSessions(updatedSessions)
                .build();

        // when
        GatheringDetailResponse response = gatheringService.updateGathering(testGathering.getId(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("수정된 모임");
        assertThat(response.getPlace()).isEqualTo("서울시 종로구");
        assertThat(response.getDescription()).isEqualTo("수정된 설명");
        assertThat(response.getGenres()).containsExactly(Genre.POP);
        assertThat(response.getSessions()).hasSize(1);
    }

    @Test
    @DisplayName("모임 수정 실패 - 주최자가 아닌 사용자가 수정 시도")
    void updateGathering_noPermission() {
        // given
        setAuthenticatedUser(otherUser);

        GatheringUpdateRequest request = GatheringUpdateRequest.builder()
                .name("수정된 모임")
                .place("서울시 종로구")
                .description("수정된 설명")
                .thumbnail("updated_thumbnail.jpg")
                .gatheringDateTime(LocalDateTime.now().plusDays(10))
                .recruitDeadline(LocalDateTime.now().plusDays(8))
                .genres(Set.of(Genre.POP))
                .gatheringSessions(List.of())
                .build();

        // when & then
        assertThatThrownBy(() -> gatheringService.updateGathering(testGathering.getId(), request))
                .isInstanceOf(GatheringException.NoUpdatePermission.class)
                .hasMessage("수정 권한이 없습니다.");
    }

    @Test
    @DisplayName("모임 취소 성공 - 주최자가 모임 취소")
    void cancelGathering_success() {
        // when
        gatheringService.cancelGathering(testGathering.getId());

        // then
        Gathering canceledGathering = gatheringRepository.findById(testGathering.getId())
                .orElseThrow();
        assertThat(canceledGathering.getStatus()).isEqualTo(GatheringStatus.CANCELED);
    }

    @Test
    @DisplayName("모임 취소 실패 - 주최자가 아닌 사용자가 취소 시도")
    void cancelGathering_noPermission() {
        // given
        setAuthenticatedUser(otherUser);

        // when & then
        assertThatThrownBy(() -> gatheringService.cancelGathering(testGathering.getId()))
                .isInstanceOf(GatheringException.NoCancelPermission.class)
                .hasMessage("취소 권한이 없습니다.");
    }

    @Test
    @DisplayName("내가 생성한 모임 목록 조회 성공")
    void getMyCreatedGatherings_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        GatheringListResponse response = gatheringService.getMyCreatedGatherings(false, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGatherings()).isNotEmpty();
        assertThat(response.getGatherings()).allSatisfy(gathering ->
                assertThat(gathering.getCreator().getId()).isEqualTo(testUser.getId())
        );
    }

    @Test
    @DisplayName("내가 생성한 모임 목록 조회 - 취소된 모임 포함")
    void getMyCreatedGatherings_includeCanceled() {
        // given
        gatheringService.cancelGathering(testGathering.getId());
        Pageable pageable = PageRequest.of(0, 10);

        // when - 취소된 모임 포함
        GatheringListResponse response = gatheringService.getMyCreatedGatherings(true, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getGatherings()).isNotEmpty();

        // when - 취소된 모임 제외
        GatheringListResponse responseExcludeCanceled = gatheringService.getMyCreatedGatherings(false, pageable);

        // then
        assertThat(responseExcludeCanceled.getGatherings())
                .noneMatch(gathering -> gathering.getStatus() == GatheringStatus.CANCELED);
    }
}
