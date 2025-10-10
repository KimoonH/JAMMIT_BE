package com.jammit_be.user.service;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringSession;
import com.jammit_be.gathering.repository.GatheringRepository;
import com.jammit_be.user.dto.request.CreateUserRequest;
import com.jammit_be.user.dto.request.UpdateImageRequest;
import com.jammit_be.user.dto.request.UpdateUserRequest;
import com.jammit_be.user.dto.response.UserResponse;
import com.jammit_be.user.entity.OauthPlatform;
import com.jammit_be.user.entity.User;
import com.jammit_be.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        testUser = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123!"))
                .username("testuser")
                .nickname("테스터")
                .oauthPlatform(OauthPlatform.NONE)
                .build();

        // 선호 장르와 밴드 세션 설정
        testUser.updatePreferredGenres(List.of(Genre.ROCK, Genre.JAZZ));
        testUser.updatePreferredBandSessions(List.of(BandSession.VOCAL, BandSession.ELECTRIC_GUITAR));

        userRepository.save(testUser);
    }

    @Test
    @DisplayName("회원가입 성공 - 새로운 사용자 등록")
    void registerUser_success() {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("newuser@example.com")
                .password("newPassword123!")
                .username("newuser")
                .nickname("새유저")
                .preferredGenres(List.of(Genre.POP, Genre.ROCK))
                .preferredBandSessions(List.of(BandSession.DRUM))
                .build();

        // when
        UserResponse response = userService.registerUser(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getNickname()).isEqualTo("새유저");
        assertThat(response.getPreferredGenres()).containsExactlyInAnyOrder(Genre.POP, Genre.ROCK);
        assertThat(response.getPreferredBandSessions()).containsExactlyInAnyOrder(BandSession.DRUM);
        assertThat(response.getTotalCreatedGatheringCount()).isEqualTo(0L);
        assertThat(response.getCompletedGatheringCount()).isEqualTo(0L);

        // DB에 저장되었는지 확인
        User savedUser = userRepository.findUserByEmail("newuser@example.com").orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");

        // 비밀번호가 암호화되었는지 확인
        assertThat(passwordEncoder.matches("newPassword123!", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void registerUser_duplicateEmail() {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com") // 이미 존재하는 이메일
                .password("password123!")
                .username("duplicate")
                .nickname("중복유저")
                .preferredGenres(List.of(Genre.ROCK))
                .preferredBandSessions(List.of(BandSession.VOCAL))
                .build();

        // when & then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(AlertException.class)
                .hasMessage("이메일이 중복되었습니다.");
    }

    @Test
    @DisplayName("사용자 정보 조회 성공 - 모임 통계 포함")
    void getUserInfo_success() {
        // given - 테스트용 모임 생성
        List<GatheringSession> sessions = List.of(
                GatheringSession.create(BandSession.VOCAL, 2)
        );

        Gathering gathering = Gathering.create(
                "테스트 모임",
                "thumbnail.jpg",
                "서울시 강남구",
                "테스트 모임입니다",
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(5),
                Set.of(Genre.ROCK),
                sessions,
                testUser
        );
        gatheringRepository.save(gathering);

        // when
        UserResponse response = userService.getUserInfo(testUser.getEmail());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getNickname()).isEqualTo("테스터");
        assertThat(response.getPreferredGenres()).containsExactlyInAnyOrder(Genre.ROCK, Genre.JAZZ);
        assertThat(response.getPreferredBandSessions()).containsExactlyInAnyOrder(BandSession.VOCAL, BandSession.ELECTRIC_GUITAR);
        assertThat(response.getTotalCreatedGatheringCount()).isEqualTo(1L);
        assertThat(response.getCompletedGatheringCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 - 존재하지 않는 사용자")
    void getUserInfo_userNotFound() {
        // when & then
        assertThatThrownBy(() -> userService.getUserInfo("nonexistent@example.com"))
                .isInstanceOf(AlertException.class)
                .hasMessage("유저를 찾지 못하였습니다");
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 기본 정보 수정")
    void updateUserInfo_success() {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("updated@example.com")
                .username("updateduser")
                .password("newPassword456!")
                .preferredGenres(List.of(Genre.POP, Genre.METAL))
                .preferredBandSessions(List.of(BandSession.BASS, BandSession.DRUM))
                .build();

        // when
        UserResponse response = userService.updateUserInfo(testUser.getEmail(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("updated@example.com");
        assertThat(response.getUsername()).isEqualTo("updateduser");
        assertThat(response.getPreferredGenres()).containsExactlyInAnyOrder(Genre.POP, Genre.METAL);
        assertThat(response.getPreferredBandSessions()).containsExactlyInAnyOrder(BandSession.BASS, BandSession.DRUM);

        // DB에서 확인
        User updatedUser = userRepository.findUserByEmail("updated@example.com").orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("updateduser");

        // 비밀번호가 새로 암호화되었는지 확인
        assertThat(passwordEncoder.matches("newPassword456!", updatedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 선호 장르만 수정")
    void updateUserInfo_onlyPreferredGenres() {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .preferredGenres(List.of(Genre.BALLAD, Genre.JAZZ))
                .preferredBandSessions(List.of(BandSession.KEYBOARD))
                .build();

        // when
        UserResponse response = userService.updateUserInfo(testUser.getEmail(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com"); // 이메일은 변경 안됨
        assertThat(response.getUsername()).isEqualTo("testuser"); // username도 변경 안됨
        assertThat(response.getPreferredGenres()).containsExactlyInAnyOrder(Genre.BALLAD, Genre.JAZZ);
        assertThat(response.getPreferredBandSessions()).containsExactlyInAnyOrder(BandSession.KEYBOARD);
    }

    @Test
    @DisplayName("사용자 정보 수정 실패 - 존재하지 않는 사용자")
    void updateUserInfo_userNotFound() {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("updated")
                .build();

        // when & then
        assertThatThrownBy(() -> userService.updateUserInfo("nonexistent@example.com", request))
                .isInstanceOf(AlertException.class)
                .hasMessage("유저를 찾지 못하였습니다");
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 성공")
    void updateProfileImage_success() {
        // given
        UpdateImageRequest request = UpdateImageRequest.builder()
                .orgFileName("profile.jpg")
                .profileImagePath("https://example.com/new-profile.jpg")
                .build();

        // when
        UserResponse response = userService.updateProfileImage(testUser.getEmail(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getProfileImagePath()).isEqualTo("https://example.com/new-profile.jpg");

        // DB에서 확인
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getProfileImagePath()).isEqualTo("https://example.com/new-profile.jpg");
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 실패 - 존재하지 않는 사용자")
    void updateProfileImage_userNotFound() {
        // given
        UpdateImageRequest request = UpdateImageRequest.builder()
                .orgFileName("profile.jpg")
                .profileImagePath("https://example.com/profile.jpg")
                .build();

        // when & then
        assertThatThrownBy(() -> userService.updateProfileImage("nonexistent@example.com", request))
                .isInstanceOf(AlertException.class)
                .hasMessage("유저를 찾지 못하였습니다");
    }

    @Test
    @DisplayName("이메일 중복 체크 - 중복된 이메일")
    void checkEmailExists_exists() {
        // when
        var response = userService.checkEmailExists("test@example.com");

        // then
        assertThat(response.getExists()).isTrue();
    }

    @Test
    @DisplayName("이메일 중복 체크 - 중복되지 않은 이메일")
    void checkEmailExists_notExists() {
        // when
        var response = userService.checkEmailExists("new@example.com");

        // then
        assertThat(response.getExists()).isFalse();
    }

    @Test
    @DisplayName("사용자 정보 수정 - 선호 장르/세션 전체 교체")
    void updateUserInfo_replacePreferences() {
        // given - 초기 상태 확인
        User initialUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(initialUser.getPreferredGenres()).hasSize(2);
        assertThat(initialUser.getUserBandSessions()).hasSize(2);

        UpdateUserRequest request = UpdateUserRequest.builder()
                .preferredGenres(List.of(Genre.INDIE)) // 완전히 새로운 장르
                .preferredBandSessions(List.of(BandSession.PERCUSSION)) // 완전히 새로운 세션
                .build();

        // when
        UserResponse response = userService.updateUserInfo(testUser.getEmail(), request);

        // then
        assertThat(response.getPreferredGenres()).containsExactly(Genre.INDIE);
        assertThat(response.getPreferredBandSessions()).containsExactly(BandSession.PERCUSSION);

        // DB에서 확인 - 이전 데이터는 삭제되고 새 데이터만 있어야 함
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getPreferredGenres()).hasSize(1);
        assertThat(updatedUser.getUserBandSessions()).hasSize(1);
        assertThat(updatedUser.getPreferredGenres().stream()
                .map(pg -> pg.getName())
                .toList()).containsExactly(Genre.INDIE);
        assertThat(updatedUser.getUserBandSessions().stream()
                .map(bs -> bs.getName())
                .toList()).containsExactly(BandSession.PERCUSSION);
    }

    @Test
    @DisplayName("완료된 모임이 있는 사용자의 통계 조회")
    void getUserInfo_withCompletedGathering() {
        // given - 완료된 모임 생성
        List<GatheringSession> sessions = List.of(
                GatheringSession.create(BandSession.VOCAL, 2)
        );

        Gathering gathering1 = Gathering.create(
                "테스트 모임 1",
                "thumbnail1.jpg",
                "서울시 강남구",
                "테스트 모임 1",
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(5),
                Set.of(Genre.ROCK),
                sessions,
                testUser
        );
        gathering1.confirm();
        gathering1.complete(); // 완료 처리
        gatheringRepository.save(gathering1);

        Gathering gathering2 = Gathering.create(
                "테스트 모임 2",
                "thumbnail2.jpg",
                "서울시 강남구",
                "테스트 모임 2",
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(8),
                Set.of(Genre.JAZZ),
                sessions,
                testUser
        );
        gatheringRepository.save(gathering2); // 진행 중

        // when
        UserResponse response = userService.getUserInfo(testUser.getEmail());

        // then
        assertThat(response.getTotalCreatedGatheringCount()).isEqualTo(2L);
        assertThat(response.getCompletedGatheringCount()).isEqualTo(1L);
    }
}
