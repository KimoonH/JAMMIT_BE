package com.jammit_be.gathering.service;

import com.jammit_be.auth.entity.CustomUserDetail;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.gathering.entity.GatheringSession;
import com.jammit_be.gathering.repository.GatheringParticipantRepository;
import com.jammit_be.gathering.repository.GatheringRepository;
import com.jammit_be.gathering.service.GatheringOwnerService;
import com.jammit_be.user.entity.User;
import com.jammit_be.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = com.jammit_be.JammitBeApplication.class)
class GatheringConcurrencyTest {

    @Autowired
    private GatheringOwnerService gatheringOwnerService;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private GatheringParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("동시에 20명이 승인 요청 시 정원 10명만 승인되어야 함")
    void 동시_승인_시_정원_초과_방지() throws InterruptedException {
        // given
        int threadCount = 20;              // 20명이 동시 요청
        int maxCapacity = 10;              // 정원 10명

        Gathering gathering = createTestGathering(maxCapacity);
        User owner = gathering.getCreatedBy();

        // SecurityContext 설정
        setSecurityContext(owner);

        List<GatheringParticipant> participants = createPendingParticipants(gathering, threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);  // 성공 카운터
        AtomicInteger failCount = new AtomicInteger(0);     // 실패 카운터

        // when - 20명이 동시에 승인 요청
        for (GatheringParticipant p : participants) {
            executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                try {
                    System.out.println("🔵 [" + threadName + "] 승인 요청 시작: 참가자 ID=" + p.getId());

                    // 각 스레드마다 SecurityContext 설정
                    setSecurityContext(owner);

                    gatheringOwnerService.approveParticipation(
                            gathering.getId(),
                            p.getId()
                    );
                    successCount.incrementAndGet();
                    System.out.println("✅ [" + threadName + "] 승인 성공: 참가자 ID=" + p.getId());
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("❌ [" + threadName + "] 승인 실패: 참가자 ID=" + p.getId() + " - " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // then - 결과 검증
        int approvedCount = participantRepository.countApproved(gathering, BandSession.ACOUSTIC_GUITAR);

        System.out.println("\n=== 동시성 테스트 결과 ===");
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("실제 DB 승인된 수: " + approvedCount);
        System.out.println("======================\n");

        // 🎯 핵심 검증
        assertThat(approvedCount)
                .as("실제 승인된 수는 정원과 같아야 함")
                .isEqualTo(maxCapacity);

        assertThat(successCount.get())
                .as("성공 횟수는 정원과 같아야 함")
                .isEqualTo(maxCapacity);

        assertThat(failCount.get())
                .as("실패 횟수는 나머지와 같아야 함")
                .isEqualTo(threadCount - maxCapacity);
    }

    /**
     * 테스트용 모임 생성
     */
    private Gathering createTestGathering(int guitarCapacity) {
        // 주최자 생성
        User owner = User.builder()
                .email("owner@test.com")
                .password("password123")
                .username("owner")
                .nickname("주최자")
                .build();
        userRepository.save(owner);

        // 세션 생성 (기타 파트만)
        List<GatheringSession> sessions = new ArrayList<>();
        sessions.add(GatheringSession.create(BandSession.ACOUSTIC_GUITAR, guitarCapacity));

        // 모임 생성
        Gathering gathering = Gathering.create(
                "락밴드 합주",
                "thumbnail.jpg",
                "홍대 연습실",
                "함께 합주해요",
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(3),
                Set.of(Genre.ROCK),
                sessions,
                owner
        );

        return gatheringRepository.save(gathering);
    }

    /**
     * 대기 중인 참가자들 생성
     */
    private List<GatheringParticipant> createPendingParticipants(Gathering gathering, int count) {
        List<GatheringParticipant> participants = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            // 유저 생성
            User user = User.builder()
                    .email("user" + i + "@test.com")
                    .password("password123")
                    .username("user" + i)
                    .nickname("참가자" + i)
                    .build();
            userRepository.save(user);

            // 참가 신청 (PENDING 상태)
            GatheringParticipant participant = GatheringParticipant.pending(
                    user,
                    gathering,
                    BandSession.ACOUSTIC_GUITAR,
                    "잘 부탁드립니다!"
            );
            participants.add(participantRepository.save(participant));
        }

        return participants;
    }

    /**
     * SecurityContext 설정
     */
    private void setSecurityContext(User user) {
        CustomUserDetail userDetail = new CustomUserDetail(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetail,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
