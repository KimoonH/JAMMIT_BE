package com.jammit_be.gathering.service;

import com.jammit_be.auth.util.AuthUtil;
import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.common.enums.ParticipantStatus;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.dto.GatheringSummary;
import com.jammit_be.gathering.dto.GatheringParticipantSummary;
import com.jammit_be.gathering.dto.request.GatheringParticipationRequest;
import com.jammit_be.gathering.dto.response.CompletedGatheringResponse;
import com.jammit_be.gathering.dto.response.GatheringListResponse;
import com.jammit_be.gathering.dto.response.GatheringParticipantListResponse;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatheringParticipationService {

    private final GatheringRepository gatheringRepository;
    private final GatheringParticipantRepository gatheringParticipantRepository;

    /**
     * 모임 참여 API
     * @param gatheringId 어떤 모임
     * @param request 어떤 밴드 세션
     * @return 참여 결과 응답
     */
    @Transactional
    public GatheringParticipationResponse participate(Long gatheringId, GatheringParticipationRequest request) {
        var user = AuthUtil.getUserInfo();
        // 1. 모임 조회 및 존재 검증 (세션 정보 포함)
        Gathering gathering = gatheringRepository.findByIdWithSessions(gatheringId)
                .orElseThrow(() -> new AlertException("존재하지 않는 모임입니다."));
                
        // 1.1 모임이 참가 가능한 상태인지 확인
        if (!gathering.isJoinable()) {
            return GatheringParticipationResponse.fail("참가 신청이 불가능한 모임 상태입니다.");
        }

        // 2. 이미 해당 세션에 신청한 기록이 있는지 체크 (중복 방지)
        boolean alreadyExists = gatheringParticipantRepository.existsActiveParticipation(user, gathering, request.getBandSession());
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
        int approvedCount = gatheringParticipantRepository.countApproved(gathering, request.getBandSession());
        if (approvedCount >= session.getRecruitCount()) {
            return GatheringParticipationResponse.fail("해당 세션의 모집 인원이 이미 마감되었습니다.");
        }

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

    /**
     * 모임 참여 취소 API
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
                
        // 본인 확인
        if (!participant.getUser().equals(user)) {
            throw new AlertException("본인의 참가 신청만 취소할 수 있습니다.");
        }
        
        // 이미 취소된 경우
        if (participant.isCanceled()) {
            return GatheringParticipationResponse.fail("이미 취소된 참가 신청입니다.");
        }
        
        // 이미 참여 완료된 경우
        if (participant.isCompleted()) {
            return GatheringParticipationResponse.fail("이미 참여 완료된 모임은 취소할 수 없습니다.");
        }

        boolean wasApproved = participant.isApproved(); // 취소 전 상태 체크

        // 취소
        participant.cancel();
        
        // 승인된 상태였다면 세션 카운트 감소
        if (wasApproved) {
            // 세션 찾기
            Gathering gathering = participant.getGathering();
            for (GatheringSession session : gathering.getGatheringSessions()) {
                if (session.getName() == participant.getName()) {
                    session.decrementCurrentCount();
                    break;
                }
            }
        }
        
        return GatheringParticipationResponse.canceled(gatheringId, user.getId(), participant.getName());
    }

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
                .orElseThrow(() -> new AlertException("존재하지 않은 모임입니다."));

        GatheringParticipant participant = gatheringParticipantRepository.findById(participantId)
                .orElseThrow(() -> new AlertException("해당 참가 신청이 없습니다."));

        // 2. 권한(주최자) 체크
        if(!gathering.getCreatedBy().equals(owner)) {
            throw new AlertException("승인 권한이 없습니다.");
        }

        // 3. 상태 검증
        if(participant.isApproved()) {
            return GatheringParticipationResponse.fail("이미 승인된 참가자 입니다.");
        }

        if(participant.isCanceled()) {
            return GatheringParticipationResponse.fail("이미 취소된 참가자 입니다.");
        }
        
        if(participant.isRejected()) {
            return GatheringParticipationResponse.fail("이미 거절된 참가자 입니다.");
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

        int approvedCount = gatheringParticipantRepository.countApproved(gathering, participant.getName());

        if(approvedCount >= targetSession.getRecruitCount()) {
            return GatheringParticipationResponse.fail("해당 세션의 모집 인원이 마감되었습니다.");
        }

        // 5. 승인 처리
        participant.approve();
        targetSession.incrementCurrentCount();
        
        // 6. 모든 세션이 모집 완료되었는지 확인하고, 모임 상태 업데이트
        boolean allSessionsFilled = true;
        for (GatheringSession session : gathering.getGatheringSessions()) {
            if (session.getCurrentCount() < session.getRecruitCount()) {
                allSessionsFilled = false;
                break;
            }
        }
        
        // 모든 세션이 채워졌다면 모집 완료 상태로 변경
        if (allSessionsFilled) {
            gathering.confirm();
        }

        return GatheringParticipationResponse.approved(
                gatheringId
                , owner.getId()
                , participant.getName());
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
                .orElseThrow(() -> new AlertException("존재하지 않은 모임입니다."));

        GatheringParticipant participant = gatheringParticipantRepository.findById(participantId)
                .orElseThrow(() -> new AlertException("해당 참가 신청이 없습니다."));

        // 3. 주최자 권한 체크
        if (!gathering.getCreatedBy().equals(owner)) {
            throw new AlertException("모임 주최자만 처리할 수 있습니다.");
        }

        // 4. 이미 승인/취소/거절된 신청은 거절 불가
        if (participant.isApproved()) {
            return GatheringParticipationResponse.fail("이미 승인된 신청입니다.");
        }
        if (participant.isCanceled()) {
            return GatheringParticipationResponse.fail("이미 취소된 신청입니다.");
        }
        if (participant.isRejected()) {
            return GatheringParticipationResponse.fail("이미 거절된 신청입니다.");
        }
        // 5. 거절 처리
        participant.reject();

        return GatheringParticipationResponse.rejected(
                gathering.getId()
                ,participant.getUser().getId()
                ,participant.getName()
        );
    }

    /**
     * 모임 완료 처리 API - 실제 합주가 완료되었고, 리뷰 작성이 가능하도록 모임 및 모든 승인된 참가자를 완료 상태로 변경
     * @param gatheringId 모임 ID
     * @return 처리 결과
     */
    @Transactional
    public boolean completeGathering(Long gatheringId) {
        User owner = AuthUtil.getUserInfo();

        // 모임 조회
        Gathering gathering = gatheringRepository.findByIdWithSessions(gatheringId)
                .orElseThrow(() -> new AlertException("존재하지 않은 모임입니다."));

        // 권한 체크
        if (!gathering.getCreatedBy().equals(owner)) {
            throw new AlertException("모임 주최자만 완료 처리할 수 있습니다.");
        }

        // 모임 상태 체크 - 모집이 완료된 상태(CONFIRMED)에서만 완료 처리 가능
        if (gathering.getStatus() != GatheringStatus.CONFIRMED) {
            throw new AlertException("멤버 모집이 완료된 모임만 완료 처리할 수 있습니다.");
        }

        // 모임 완료 처리 (참가자도 함께 참여 완료 상태로 변경됨)
        gathering.complete();

        return true;
    }

    /**
     * 참가자 목록 조회
     * @param gatheringId 모임 아이디 PK
     * @return 모임에 참가된 목록
     */
    @Transactional(readOnly = true)
    public GatheringParticipantListResponse findParticipants(Long gatheringId){
        // 1. 참가자 목록 조회
        List<GatheringParticipant> participants = gatheringParticipantRepository.findByGatheringId(gatheringId);

        // 2. DTO 변환
        List<GatheringParticipantSummary> summaries = new ArrayList<>();
        // 참가자 없으면 빈 객체 반환
        if(participants.isEmpty()) {
            return GatheringParticipantListResponse.builder()
                    .participants(Collections.emptyList())
                    .total(0)
                    .build();
        }
        for(GatheringParticipant participant : participants) {
            var user = participant.getUser();
            summaries.add(GatheringParticipantSummary.builder()
                    .participantId(participant.getId())
                    .userId(user.getId())
                    .userEmail(user.getEmail())
                    .userNickname(user.getNickname())
                    .userProfileImagePath(user.getProfileImagePath()) // 추가
                    .bandSession(participant.getName())
                    .status(participant.getStatus())
                    .createdAt(participant.getCreatedAt())
                    .introduction(participant.getIntroduction())
                    .build());
        }

        // 3. 반환
        return GatheringParticipantListResponse.builder()
                .participants(summaries)
                .total(summaries.size())
                .build();
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

        // 페이지 내용 처리
        Set<Gathering> gatherings = new HashSet<>();
        for (GatheringParticipant participation : participationsPage.getContent()) {
            // 모임 상태 확인 (모임이 취소되었는데 includeCanceled가 false라면 제외)
            Gathering gathering = participation.getGathering();
            gatherings.add(gathering);
        }

        // Gathering을 GatheringSummary로 변환
        List<GatheringSummary> summaries = gatherings.stream()
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
