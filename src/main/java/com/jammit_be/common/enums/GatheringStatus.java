package com.jammit_be.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GatheringStatus {
    RECRUITING("멤버 모집 중"),      // 개설 대기 - 멤버 모집 중
    CONFIRMED("멤버 모집 완료"),     // 개설 확정 - 멤버 모집 완료 (실제 모임 진행 전)
    COMPLETED("모임 완료"),         // 모임 완료 - 실제 모임이 진행되어 합주 완료 (리뷰 가능)
    CANCELED("모임 취소");          // 모임 취소 - 주최자가 모임 자체를 취소함

    private final String description;
    
    /**
     * 리뷰 작성이 가능한 상태인지 확인
     * @return 리뷰 작성 가능 여부
     */
    public boolean isReviewable() {
        return this == COMPLETED;
    }
    
    /**
     * 활성 상태(취소되지 않은 상태)인지 확인
     * @return 활성 상태 여부
     */
    public boolean isActive() {
        return this != CANCELED;
    }
    
    /**
     * 참가 신청이 가능한 상태인지 확인
     * @return 참가 신청 가능 여부
     */
    public boolean isJoinable() {
        return this == RECRUITING;
    }
} 