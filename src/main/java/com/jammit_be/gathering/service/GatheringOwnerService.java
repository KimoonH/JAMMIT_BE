package com.jammit_be.gathering.service;

import com.jammit_be.auth.util.AuthUtil;
import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.gathering.exception.GatheringException;
import com.jammit_be.gathering.exception.ParticipantException;
import com.jammit_be.gathering.exception.OwnerException;
import com.jammit_be.gathering.dto.GatheringParticipantSummary;
import com.jammit_be.gathering.dto.response.GatheringParticipantListResponse;
import com.jammit_be.gathering.dto.response.GatheringParticipationResponse;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.gathering.entity.GatheringSession;
import com.jammit_be.gathering.repository.GatheringParticipantRepository;
import com.jammit_be.gathering.repository.GatheringRepository;
import com.jammit_be.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 모임 주최자 관련 서비스
 * - 참가 신청 승인/거절
 * - 모임 완료 처리
 * - 참가자 목록 조회
 */
@Service
@RequiredArgsConstructor
public class GatheringOwnerService {

    private final GatheringRepository gatheringRepository;
    private final GatheringParticipantRepository gatheringParticipantRepository;

    /**
     * 주최자 승인 처리 API
     * @param gatheringId 모임 ID
     * @param participantId 참가자 ID
     * @return 승인 결과 응답
     */
    @Transactional
    public GatheringParticipationResponse approveParticipation(Long gatheringId, Long participantId) {
        User owner = AuthUtil.getUserInfo();

        // 1. 모임 및 참가자 조회
        Gathering gathering = gatheringRepository.findByIdWithSessions(gatheringId)
                .orElseThrow(GatheringException.NotFound::new);

        GatheringParticipant participant = gatheringParticipantRepository.findById(participantId)
                .orElseThrow(ParticipantException.NotFound::new);

        validateApprovalRequest(gatheringId, owner, gathering, participant);

        // 정원 인원 체크
        checkBandSessionCapacity(gathering, participant);

        // 승인 처리
        participant.approve();

        // 모집 인원수 증가 및 상태 변경
        handleBandSessionCountIncrementAndStatusUpdate(participant, gathering);

        return GatheringParticipationResponse.approved(
                gatheringId,
                owner.getId(),
                participant.getName());
    }

    private void validateApprovalRequest(Long gatheringId, User owner, Gathering gathering, GatheringParticipant participant) {

        // gatheringId 일치 확인
        if(!participant.getGathering().getId().equals(gatheringId)) {
            throw new ParticipantException.NotGatheringParticipant();
        }

        // 주최자 권한 확인
        if(!gathering.getCreatedBy().equals(owner)) {
            throw new OwnerException.NoApprovalPermission();
        }

        // 상태 검증
        if(participant.isApproved()) {
            throw new OwnerException.AlreadyApproved();
        }

        if(participant.isCanceled()) {
            throw new OwnerException.AlreadyCanceled();
        }

        if(participant.isRejected()) {
            throw new OwnerException.AlreadyRejected();
        }
    }

    private void checkBandSessionCapacity(Gathering gathering, GatheringParticipant participant) {
        GatheringSession session = gathering.getSession(participant.getName());
        int approvedCount = gatheringParticipantRepository.countApproved(gathering, participant.getName());

        if (approvedCount >= session.getRecruitCount()) {
            throw new OwnerException.SessionFull();
        }
    }

    private void handleBandSessionCountIncrementAndStatusUpdate(GatheringParticipant participant, Gathering gathering) {
        GatheringSession bandSession = gathering.getSession(participant.getName());
        bandSession.incrementCurrentCount();

        if(gathering.isAllBandSessionFilled()) {
            gathering.confirm();
        }
    }

    /**
     * 주최자 참가자 모임 거절 처리
     * @param gatheringId 모임 ID
     * @param participantId 참가자 ID
     * @return 거절 결과 응답
     */
    @Transactional
    public GatheringParticipationResponse rejectParticipation(Long gatheringId, Long participantId) {
        User owner = AuthUtil.getUserInfo();

        // 1. 모임 및 참가자 조회
        Gathering gathering = gatheringRepository.findByIdWithSessions(gatheringId)
                .orElseThrow(GatheringException.NotFound::new);

        GatheringParticipant participant = gatheringParticipantRepository.findById(participantId)
                .orElseThrow(ParticipantException.NotFound::new);

        // 2. 검증
        validateRejectionRequest(gatheringId, owner, gathering, participant);

        // 3. 거절 처리
        participant.reject();

        return GatheringParticipationResponse.rejected(
                gathering.getId(),
                participant.getUser().getId(),
                participant.getName()
        );
    }

    private void validateRejectionRequest(Long gatheringId, User owner, Gathering gathering, GatheringParticipant participant) {
        // gatheringId 일치 확인
        if (!participant.getGathering().getId().equals(gatheringId)) {
            throw new ParticipantException.NotGatheringParticipant();
        }

        // 주최자 권한 확인
        if (!gathering.getCreatedBy().equals(owner)) {
            throw new OwnerException.OnlyOwnerCanProcess();
        }

        // 상태 검증
        if (participant.isApproved()) {
            throw new OwnerException.AlreadyApprovedApplication();
        }

        if (participant.isCanceled()) {
            throw new OwnerException.AlreadyCanceledApplication();
        }

        if (participant.isRejected()) {
            throw new OwnerException.AlreadyRejectedApplication();
        }
    }

    /**
     * 모임 완료 처리 API
     * @param gatheringId 모임 ID
     */
    @Transactional
    public void completeGathering(Long gatheringId) {
        User owner = AuthUtil.getUserInfo();

        // 모임 조회
        Gathering gathering = gatheringRepository.findByIdWithSessions(gatheringId)
                .orElseThrow(GatheringException.NotFound::new);

        validateCompletionRequest(owner, gathering);

        // 모임 완료 처리 (참가자도 함께 참여 완료 상태로 변경됨)
        gathering.complete();
    }

    private void validateCompletionRequest(User owner, Gathering gathering) {
        // 주최자 권한 확인
        if (!gathering.getCreatedBy().equals(owner)) {
            throw new OwnerException.OnlyOwnerCanComplete();
        }

        // 모임 상태 확인
        if (gathering.getStatus() != GatheringStatus.CONFIRMED) {
            throw new OwnerException.OnlyConfirmedCanComplete();
        }
    }

    /**
     * 참가자 목록 조회 API
     * @param gatheringId 모임 아이디 PK
     * @return 모임에 참가된 목록
     */
    @Transactional(readOnly = true)
    public GatheringParticipantListResponse findParticipants(Long gatheringId){
        // 1. 참가자 목록 조회
        List<GatheringParticipant> participants = gatheringParticipantRepository.findByGatheringId(gatheringId);

        // 2. DTO 변환
        List<GatheringParticipantSummary> summaries = participants.stream()
                .map(participant -> {
                    User user = participant.getUser();
                    return GatheringParticipantSummary.builder()
                            .participantId(participant.getId())
                            .userId(user.getId())
                            .userEmail(user.getEmail())
                            .userNickname(user.getNickname())
                            .userProfileImagePath(user.getProfileImagePath())
                            .bandSession(participant.getName())
                            .status(participant.getStatus())
                            .createdAt(participant.getCreatedAt())
                            .introduction(participant.getIntroduction())
                            .build();
                })
                .collect(Collectors.toList());

        // 3. 반환
        return GatheringParticipantListResponse.builder()
                .participants(summaries)
                .total(summaries.size())
                .build();
    }
}
