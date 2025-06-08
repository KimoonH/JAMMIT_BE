package com.jammit_be.gathering.repository;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.ParticipantStatus;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GatheringParticipantRepository extends JpaRepository<GatheringParticipant, Long> {
    // 이미 참여한 기록이 있는지(중복 신청 방지)
    boolean existsByUserAndGatheringAndNameAndStatusNot(User user, Gathering gathering, BandSession name, ParticipantStatus status);
    
    // 참가 신청 중복 확인 (취소되지 않은 상태)
    default boolean existsActiveParticipation(User user, Gathering gathering, BandSession name) {
        return existsByUserAndGatheringAndNameAndStatusNot(user, gathering, name, ParticipantStatus.CANCELED);
    }
    
    // 해당 모임, 세션에서 승인된(approved) 인원 수 카운트
    int countByGatheringAndNameAndStatus(Gathering gathering, BandSession name, ParticipantStatus status);
    
    // 승인된 참가자 수 카운트
    default int countApproved(Gathering gathering, BandSession name) {
        return countByGatheringAndNameAndStatus(gathering, name, ParticipantStatus.APPROVED);
    }

    // 내가 신청한 모임 목록 조회 (페이징 처리, 취소 상태가 아닌 참가 신청)
    @EntityGraph(value = "GatheringParticipant.withUserAndGathering")
    @Query("SELECT gp FROM GatheringParticipant gp WHERE gp.user = :user AND gp.status <> com.jammit_be.common.enums.ParticipantStatus.CANCELED")
    Page<GatheringParticipant> findMyParticipations(@Param("user") User user, Pageable pageable);

    // 내가 신청한 모든 모임 목록 조회 (취소된 것 포함, 페이징 처리)
    @EntityGraph(value = "GatheringParticipant.withUserAndGathering")
    @Query("SELECT gp FROM GatheringParticipant gp WHERE gp.user = :user")
    Page<GatheringParticipant> findAllMyParticipations(@Param("user") User user, Pageable pageable);

    @EntityGraph(value = "GatheringParticipant.withUser")
    List<GatheringParticipant> findByGatheringId(Long gatheringId);
    
    // 특정 유저가 특정 모임에 참여한 기록 조회
    @EntityGraph(value = "GatheringParticipant.withUser")
    Optional<GatheringParticipant> findByUserAndGathering(User user, Gathering gathering);
    
    // 특정 유저가 특정 모임에 참여하고 상태가 COMPLETED인 기록 확인
    boolean existsByUserAndGatheringAndStatus(User user, Gathering gathering, ParticipantStatus status);
    
    // 특정 유저가 특정 모임에 참여 완료 여부 확인
    default boolean isParticipationCompleted(User user, Gathering gathering) {
        return existsByUserAndGatheringAndStatus(user, gathering, ParticipantStatus.COMPLETED);
    }
    
    @Override
    @EntityGraph(value = "GatheringParticipant.withUser")
    Optional<GatheringParticipant> findById(Long id);
    
    @Override
    @EntityGraph(value = "GatheringParticipant.withUser")
    List<GatheringParticipant> findAll();
}
