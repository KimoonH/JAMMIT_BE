package com.jammit_be.gathering.service;

import com.jammit_be.auth.util.AuthUtil;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.dto.GatheringSummary;
import com.jammit_be.gathering.dto.request.GatheringParticipationRequest;
import com.jammit_be.gathering.dto.response.CompletedGatheringResponse;
import com.jammit_be.gathering.dto.response.GatheringListResponse;
import com.jammit_be.gathering.dto.response.GatheringParticipationResponse;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.gathering.entity.GatheringSession;
import com.jammit_be.gathering.repository.GatheringParticipantRepository;
import com.jammit_be.gathering.repository.GatheringRepository;
import com.jammit_be.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.stream.Collectors;

import static com.jammit_be.gathering.constants.GatheringConstants.ErrorMessage.*;


@Service
@RequiredArgsConstructor
public class GatheringParticipationService {

    private final GatheringRepository gatheringRepository;
    private final GatheringParticipantRepository gatheringParticipantRepository;

    /**
     * 모임 참여 API (참여자가 하는 행위들)
     * @param gatheringId 어떤 모임
     * @param request 어떤 밴드 세션
     * @return 참여 결과 응답
     */
    @Transactional
    public GatheringParticipationResponse participate(Long gatheringId, GatheringParticipationRequest request) {
        var user = AuthUtil.getUserInfo();

        // 1. 모임 조회 및 존재 검증 (세션 정보 포함)
        Gathering gathering = gatheringRepository.findByIdWithSessions(gatheringId)
                .orElseThrow(() -> new AlertException(GATHERING_NOT_FOUND));

        validateParticipation(user,gathering, request.getBandSession());

        // 5. GatheringParticipant(참가자) 엔티티 생성 및 저장 (대기 상태)
        GatheringParticipant participant = GatheringParticipant.pending(user, gathering, request.getBandSession(), request.getIntroduction());
        gatheringParticipantRepository.save(participant);

        // 6. 응답 DTO 생성 및 반환 (승인 전: 대기)
        return GatheringParticipationResponse.waiting(
                gathering.getId(),
                user.getId(),
                request.getBandSession()
        );
    }

    private void validateParticipation(User user, Gathering gathering, BandSession bandSession) {
        // 모임이 참가 가능한 상태인지 확인
        if (!gathering.isJoinable()) {
            throw new AlertException(GATHERING_NOT_JOINABLE);
        }
        // 중복 신청 체크
        boolean alreadyExists = gatheringParticipantRepository.existsActiveParticipation(user, gathering, bandSession);
        if (alreadyExists) {
            throw new AlertException(ALREADY_APPLIED_FOR_SESSION);
        }

        // 정원 초과 체크
        GatheringSession session = gathering.getSession(bandSession);
        int approvedCount = gatheringParticipantRepository.countApproved(gathering, bandSession);
        if (approvedCount >= session.getRecruitCount()) {
            throw new AlertException(SESSION_RECRUITMENT_FULL);
        }
    }


    /**
     * 모임 참여 취소 API (참여자가 하는 행위들)
     * @param gatheringId 모임 아이디 PK
     * @param participantId 참가자 아이디 PK
     * @return 취소 결과 응답
     */
    @Transactional
    public GatheringParticipationResponse cancelParticipation(
            Long gatheringId
            , Long participantId) {
        User user = AuthUtil.getUserInfo();
        // 1. 참가 엔티티 조회
        GatheringParticipant participant = gatheringParticipantRepository.findById(participantId)
                .orElseThrow(() -> new AlertException("해당 참가 신청이 없습니다."));

        validateCancellation(gatheringId, user, participant);

        //상태 체크 & 취소
        boolean wasApproved = participant.isApproved();
        participant.cancel();

        // 승인된 상태였다면 세션 카운트 감소
        handleSessionCountDecrementIfNeeded(participant, wasApproved);
        
        return GatheringParticipationResponse.canceled(gatheringId, user.getId(), participant.getName());
    }

    private void handleSessionCountDecrementIfNeeded(GatheringParticipant participant, boolean wasApproved) {
        if (!wasApproved) {
            return; // 승인되지 않았던 상태면 카운트 변경 불필요
        }

        Gathering gathering = participant.getGathering();
        GatheringSession targetSession = gathering.getSession(participant.getName());
        targetSession.decrementCurrentCount();

        // 모임 상태 재평가
        reevaluateGatheringStatusAfterCancellation(gathering);

    }

    // 모임 상태 재평가 메서드
    private void reevaluateGatheringStatusAfterCancellation(Gathering gathering) {

        // CONFIRMED 상태일 때만 재평가 필요
        if (gathering.getStatus() != GatheringStatus.CONFIRMED) {
            return;
        }

        // 모든 세션이 여전히 꽉 차있는지 확인
        boolean allBandSessionsFilled = gathering.isAllBandSessionFilled();

        // 빈 자리가 생겼다면 다시 모집 상태로 변경
        if (!allBandSessionsFilled) {
            gathering.startRecruiting(); // CONFIRMED → RECRUITING
        }
    }

    // 검증 메서드 추출
    private void validateCancellation(Long gatheringId ,User user, GatheringParticipant participant) {
        // 모임 일치 여부 확인 (첫 번째로 추가)
        if (!participant.getGathering().getId().equals(gatheringId)) {
            throw new AlertException("해당 모임의 참가자가 아닙니다.");
        }

        // 본인 확인
        if (!participant.getUser().equals(user)) {
            throw new AlertException("본인의 참가 신청만 취소할 수 있습니다.");
        }

        // 이미 취소된 경우
        if (participant.isCanceled()) {
            throw new AlertException("이미 취소된 참가 신청입니다.");
        }

        // 이미 참여 완료된 경우
        if (participant.isCompleted()) {
            throw new AlertException("이미 참여 완료된 모임은 취소할 수 없습니다.");
        }
    }

    /**
     * 내가 신청한 모임 목록 조회 API
     * @param pageable 페이징 정보
     * @return 내가 신청한 모임 목록과 페이징 정보
     */
    @Transactional(readOnly = true)
    public GatheringListResponse getMyParticipations(Pageable pageable) {
        User user = AuthUtil.getUserInfo();

        Page<GatheringParticipant> participationsPage = gatheringParticipantRepository.findMyParticipations(user, pageable);

        // 같은 모임에 여러 세션으로 신청한 경우 중복 제거
        List<GatheringSummary> summaries = participationsPage.getContent().stream()
                .map(participant -> participant.getGathering())
                .distinct()
                .map(GatheringSummary::of)
                .collect(Collectors.toList());


        // 페이징 정보와 함께 응답 객체 생성
        return GatheringListResponse.builder()
                .gatherings(summaries)
                .currentPage(participationsPage.getNumber())
                .totalPage(participationsPage.getTotalPages())
                .totalElements(participationsPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CompletedGatheringResponse> getMyCompletedGatherings(User user) {
        return gatheringParticipantRepository.findCompletedGatheringsByUser(user);
    }
}
