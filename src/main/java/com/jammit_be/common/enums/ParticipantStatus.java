package com.jammit_be.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticipantStatus {
    PENDING("신청 완료"),       // 신청했고, 승인 대기 중
    APPROVED("신청 승인"),      // 내 신청이 승인됨 (모임 진행 전)
    REJECTED("신청 거절"),      // 내 신청이 거절됨
    COMPLETED("참여 완료"),     // 실제 모임에 참여하여 합주 완료 (리뷰 작성 가능)
    CANCELED("참여 취소");      // 참여를 취소함

    private final String description;
    
    /**
     * 리뷰 작성이 가능한 상태인지 확인
     * @return 리뷰 작성 가능 여부
     */
    public boolean isReviewable() {
        return this == COMPLETED;
    }
    
    /**
     * 승인된 상태인지 확인 (APPROVED 또는 COMPLETED)
     * @return 승인 여부
     */
    public boolean isApproved() {
        return this == APPROVED || this == COMPLETED;
    }
} 