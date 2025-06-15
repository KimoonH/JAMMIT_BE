package com.jammit_be.gathering.scheduler;

import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.repository.GatheringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatheringScheduler {

    private final GatheringRepository gatheringRepository;

    /**
     * 매일 자정(00:00)에 실행되어 전날에 시작된 모임들을 완료 처리합니다.
     * CONFIRMED 상태인 모임 중 gatheringDateTime이 전날인 모임들을 찾아 COMPLETED 상태로 변경합니다.
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    @Transactional
    public void completeGatherings() {
        log.info("모임 완료 처리 스케줄러 실행 시작");
        
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime today = yesterday.plusDays(1);
        
        // 전날에 시작된 CONFIRMED 상태의 모임들을 조회
        List<Gathering> gatheringsToComplete = gatheringRepository.findConfirmedGatheringsBetweenDates(yesterday, today);
        
        // 모임들을 완료 처리
        for (Gathering gathering : gatheringsToComplete) {
            gathering.complete();
            log.info("모임 완료 처리: 모임 ID={}, 모임명={}", gathering.getId(), gathering.getName());
        }
        
        log.info("모임 완료 처리 스케줄러 실행 완료: {}개의 모임이 완료 처리됨", gatheringsToComplete.size());
    }

    /**
     * 애플리케이션 시작 시 즉시 실행되고, 이후 이전 작업 완료 후 30초 뒤에 실행되어 모집 마감일이 지난 모임들을 취소 처리합니다.
     * RECRUITING 상태인 모임 중 recruitDeadline이 지났는데 모든 세션이 모집되지 않은 모임을 CANCELED 상태로 변경합니다.
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 0) // 애플리케이션 시작 시 즉시 실행, 이후 이전 작업 완료 후 30초 뒤 실행
    @Transactional
    public void cancelIncompleteGatherings() {
        log.info("미완료 모임 취소 처리 스케줄러 실행 시작");
        
        LocalDateTime currentTime = LocalDateTime.now();
        
        // 모집 마감일이 지나고 모든 세션이 모집되지 않은 RECRUITING 상태의 모임들을 조회
        List<Gathering> gatheringsToCancel = gatheringRepository.findIncompleteGatheringsAfterDeadline(currentTime);
        
        // 모임들을 취소 처리
        for (Gathering gathering : gatheringsToCancel) {
            gathering.cancel();
            log.info("모임 취소 처리: 모임 ID={}, 모임명={}", gathering.getId(), gathering.getName());
        }
        
        log.info("미완료 모임 취소 처리 스케줄러 실행 완료: {}개의 모임이 취소 처리됨", gatheringsToCancel.size());
    }
}
