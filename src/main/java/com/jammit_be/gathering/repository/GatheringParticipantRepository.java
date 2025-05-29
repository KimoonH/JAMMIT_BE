package com.jammit_be.gathering.repository;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringParticipantRepository extends JpaRepository<GatheringParticipant, Long> {
    // 이미 참여한 기록이 있는지(중복 신청 방지)
    boolean existsByUserAndGatheringAndNameAndCanceledFalse(User user, Gathering gathering, BandSession name);
    // 해당 모임, 세션에서 승인된(approved) 인원 수 카운트
    int countByGatheringAndNameAndApprovedTrue(Gathering gathering, BandSession name);
}
