package com.jammit_be.gathering.repository;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GatheringParticipantRepository extends JpaRepository<GatheringParticipant, Long> {
    // 이미 참여한 기록이 있는지(중복 신청 방지)
    boolean existsByUserAndGatheringAndNameAndCanceledFalse(User user, Gathering gathering, BandSession name);
    // 해당 모임, 세션에서 승인된(approved) 인원 수 카운트
    int countByGatheringAndNameAndApprovedTrue(Gathering gathering, BandSession name);
    
    // 내가 신청한 모임 목록 조회
    @Query("SELECT gp FROM GatheringParticipant gp JOIN FETCH gp.gathering WHERE gp.user = :user AND gp.canceled = false")
    List<GatheringParticipant> findMyParticipations(User user);
    
    // 내가 신청한 모든 모임 목록 조회 (취소된 것 포함)
    @Query("SELECT gp FROM GatheringParticipant gp JOIN FETCH gp.gathering WHERE gp.user = :user")
    List<GatheringParticipant> findAllMyParticipations(User user);
}
