package com.jammit_be.gathering.service;

import com.jammit_be.common.enums.BandSession;
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
        boolean alreadyExists = gatheringParticipantRepository.existsByUserAndGatheringAndName(user, gathering, request.getBandSession());
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
}
