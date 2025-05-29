package com.jammit_be.gathering.service;

import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.dto.request.GatheringParticipationRequest;
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

@Service
@RequiredArgsConstructor
public class GatheringParticipationService {

    private final GatheringRepository gatheringRepository;
    private final GatheringParticipantRepository gatheringParticipantRepository;

    /**
     * 모임 참여 API
     * @param gatheringId 어떤 모임
     * @param request 어떤 밴드 세션
     * @param user 누구인지
     * @return
     */
    @Transactional
    public GatheringParticipationResponse participate(Long gatheringId, GatheringParticipationRequest request, User user) {
        // 1. 모임 조회 및 존재 검증 (세션 정보 포함)
        Gathering gathering = gatheringRepository.findByIdWithSessions(gatheringId)
                .orElseThrow(() -> new AlertException("존재하지 않는 모임입니다."));

        // 2. 이미 해당 세션에 신청한 기록이 있는지 체크 (중복 방지)
        boolean alreadyExists = gatheringParticipantRepository.existsByUserAndGatheringAndNameAndCanceledFalse(user, gathering, request.getBandSession());
        if (alreadyExists) {
            return GatheringParticipationResponse.fail("이미 해당 파트로 신청한 이력이 있습니다.");
        }

        // 3. 밴드 세션별 모집 현황 확인 (정원 초과 방지)
        GatheringSession session = null;
        for (GatheringSession s : gathering.getGatheringSessions()) {
            if (s.getName() == request.getBandSession()) {
                session = s;
                break;
            }
        }
        if (session == null) {
            throw new AlertException("모집 중인 세션이 아닙니다.");
        }

        // 4. 해당 세션의 승인된 인원 수(모집 정원 초과 방지)
        int approvedCount = gatheringParticipantRepository.countByGatheringAndNameAndApprovedTrue(gathering, request.getBandSession());
        if (approvedCount >= session.getRecruitCount()) {
            return GatheringParticipationResponse.fail("해당 세션의 모집 인원이 이미 마감되었습니다.");
        }

        // 5. GatheringParticipant(참가자) 엔티티 생성 및 저장 (대기 상태)
        GatheringParticipant participant = GatheringParticipant.pending(user, gathering, request.getBandSession());
        gatheringParticipantRepository.save(participant);

        // 6. 응답 DTO 생성 및 반환 (승인 전: 대기)
        return GatheringParticipationResponse.waiting(
                gathering.getId(),
                user.getId(),
                request.getBandSession()
        );
    }

    /**
     * 모임 참여 취소 API
     * @param gatheringId 모임 아이디 PK
     * @param participantId 참가자 아이디 PK
     * @param user 유저
     * @return
     */
    @Transactional
    public GatheringParticipationResponse cancelParticipation(
            Long gatheringId
            , Long participantId
            , User user) {
        // 1. 참가 엔티티 조회
        GatheringParticipant participant = gatheringParticipantRepository.findById(participantId)
                .orElseThrow(() -> new AlertException("해당 참가 신청이 없습니다."));
        // 취소
        participant.cancel();
        return GatheringParticipationResponse.canceled(gatheringId, user.getId(), participant.getName());
    }

    @Transactional
    public GatheringParticipationResponse approveParticipation(Long gatheringId, Long participantId, User user) {
        // 1. 모임 및 참가자 조회
        Gathering gathering = gatheringRepository.findByIdWithSessions(gatheringId)
                .orElseThrow(() -> new AlertException("존재하지 않은 모임입니다."));

        GatheringParticipant participant = gatheringParticipantRepository.findById(participantId)
                .orElseThrow(() -> new AlertException("해당 참가 신처이 없습니다."));

        // 2. 권한(주최자) 체크
        if(!gathering.getCreatedBy().equals(user)) {
            throw new AlertException("승인 권한이 없습니다.");
        }

        // 3. 상태 검증
        if(participant.isApproved()) {
            return GatheringParticipationResponse.fail("이미 승인된 참가자 입니다.");
        }

        if(participant.isCanceled()) {
            return GatheringParticipationResponse.fail("이미 취소된 참가자 입니다.");
        }

        // 4. 정원 인원 체크
        GatheringSession targetSession = null;
        for(GatheringSession s : gathering.getGatheringSessions()) {
            if(s.getName() == participant.getName()) {
                targetSession = s;
                break;
            }
        }

        if(targetSession == null) {
            throw new AlertException("밴드 세션 정보를 찾을 수 없습니다.");
        }

        int approvedCount = gatheringParticipantRepository
                .countByGatheringAndNameAndApprovedTrue(gathering, participant.getName());

        if(approvedCount >= targetSession.getRecruitCount()) {
            return GatheringParticipationResponse.fail("해당 세션의 모집 이원이 마감되었습니다.");
        }

        // 5. 승인 처리
        participant.approve();
        targetSession.incrementCurrentCount();


        return GatheringParticipationResponse.approved(
                gatheringId
                , user.getId()
                , participant.getName())    ;
    }
}
