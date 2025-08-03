package com.jammit_be.gathering.service;

import com.jammit_be.auth.util.AuthUtil;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.dto.CreatorInfo;
import com.jammit_be.gathering.dto.GatheringSessionInfo;
import com.jammit_be.gathering.dto.GatheringSummary;
import com.jammit_be.gathering.dto.request.GatheringCreateRequest;
import com.jammit_be.gathering.dto.request.GatheringSessionRequest;
import com.jammit_be.gathering.dto.request.GatheringUpdateRequest;
import com.jammit_be.gathering.dto.response.GatheringCreateResponse;
import com.jammit_be.gathering.dto.response.GatheringDetailResponse;
import com.jammit_be.gathering.dto.response.GatheringListResponse;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringParticipantRepository gatheringParticipantRepository;

    /**
     * 모임 등록 API
     * @param request 모임 요청 데이터
     * @return
     */
    @Transactional
    public GatheringCreateResponse createGathering(GatheringCreateRequest request) {
        User user = AuthUtil.getUserInfo();
        List<GatheringSession> sessionEntities = request.getGatheringSessions().stream()
                .map(GatheringSessionRequest::toEntity)
                .toList();

        Gathering gathering = Gathering.create(
                request.getName()
                ,request.getThumbnail()
                ,request.getPlace()
                ,request.getDescription()
                ,request.getGatheringDateTime()
                ,request.getRecruitDateTime()
                ,request.getGenres()
                ,sessionEntities
                ,user
        );

        Gathering saved = gatheringRepository.save(gathering);

        // 주최자 참여자로 저장
        GatheringParticipant hostParticipant = GatheringParticipant.createHostParticipant(user, saved);
        gatheringParticipantRepository.save(hostParticipant);


        return GatheringCreateResponse.from(saved);
    }

    /**
     * 모임 전체 목록 조회 API
     * @param genres 검색할 음악 장르 리스트
     * @param sessions 모집 파트 리스트
     * @param pageable 페이징/정렬 정보
     * @return 데이터 + 페이징
     */
    public GatheringListResponse findGatherings(
            List<Genre> genres
            , List<BandSession> sessions
            , Pageable pageable
    ) {

        // 1. DB에서 조건/페이징/정렬에 맞는 Gathering 목록 조회
        Page<Gathering> page = gatheringRepository.findGatherings(genres, sessions, pageable);

        // 2. 각 엔티티를 DTO(GatheringSummary)로 변환
        List<GatheringSummary> summaries = new ArrayList<>();
        for (Gathering gathering : page.getContent()) {
            summaries.add(GatheringSummary.of(gathering));
        }

        // 3. 페이징 정보와 함께 리스트를 Response DTO로 감싸서 반환
        return GatheringListResponse.builder()
                .gatherings(summaries)
                .currentPage(page.getNumber())
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    /**
     * 모임 상세 조회 API
     * @param gatheringId 상세조회 할 모임 PK
     * @return GatheringDetailResponse
     */
    public GatheringDetailResponse getGatheringDetail(Long gatheringId) {
        // 1. 모임 엔티티 + 밴드 세션 정보까지 한번에 조회
        Gathering gathering = gatheringRepository.findByIdWithSessions(gatheringId)
                .orElseThrow(() -> new AlertException("모임이 존재하지 않습니다."));

        // 2. 밴드 세션 엔티티 리스트 → 세션 응답 DTO 리스트로 변환
        List<GatheringSessionInfo> sessionInfos = new ArrayList<>();
        for(GatheringSession gatheringSession : gathering.getGatheringSessions()) {
            sessionInfos.add(GatheringSessionInfo.builder()
                    .bandSession(gatheringSession.getName())
                    .recruitCount(gatheringSession.getRecruitCount())
                    .currentCount(gatheringSession.getCurrentCount())
                    .build());
        }

        // 3. 모임 상세 응답 DTO 생성 및 반환
        return GatheringDetailResponse.builder()
                .id(gathering.getId())
                .name(gathering.getName())
                .place(gathering.getPlace())
                .thumbnail(gathering.getThumbnail())
                .description(gathering.getDescription())
                .gatheringDateTime(gathering.getGatheringDateTime())
                .recruitDeadline(gathering.getRecruitDeadline())
                .status(gathering.getStatus())
                .genres(gathering.getGenres())
                .sessions(sessionInfos)
                .creator(CreatorInfo.of(gathering.getCreatedBy()))
                .build();
    }

    /**
     * 모임 정보 및 밴드 세션 수정하는 서비스 로직
     * @param id 수정할 모임 PK
     * @param request 수정 요청 DTO
     * @return 수정 후 상세 응답 DTO
     */
    @Transactional
    public GatheringDetailResponse updateGathering(Long id, GatheringUpdateRequest request) {
        User user = AuthUtil.getUserInfo();
        // 1. 기존 모임 데이터 조회 (세션 정보 포함)
        Gathering gathering = gatheringRepository.findByIdWithSessions(id)
                .orElseThrow(() -> new AlertException("모임을 찾을 수 없습니다."));

        // 작성자 권한 체크
        if(!gathering.getCreatedBy().equals(user)) {
            throw new AlertException("수정 권한이 없습니다.");
        }

        // 3. 값 변경 (changeXXX 메서드 활용)
        gathering.changeName(request.getName());
        gathering.changePlace(request.getPlace());
        gathering.changeDescription(request.getDescription());
        gathering.changeThumbnail(request.getThumbnail());
        gathering.changeGatheringDateTime(request.getGatheringDateTime());
        gathering.changeRecruitDeadline(request.getRecruitDeadline());
        gathering.changeGenres(request.getGenres());

        // 4. 세션(파트/모집인원) 정보가 수정된다면 별도 처리 (예시)
        if (request.getGatheringSessions() != null && !request.getGatheringSessions().isEmpty()) {
            // GatheringSessionRequest -> GatheringSession 변환
            List<GatheringSession> newSessions = new ArrayList<>();
            for (GatheringSessionRequest sessionReq : request.getGatheringSessions()) {
                newSessions.add(GatheringSession.create(
                        sessionReq.getBandSession(),
                        sessionReq.getRecruitCount()
                ));
            }
            gathering.updateGatheringSessions(newSessions);
        }

        return GatheringDetailResponse.from(gathering);
    }

    /**
     * 모임 취소
     * @param id 취소할 모임 PK
     */
    @Transactional
    public void cancelGathering(Long id) {
        User user = AuthUtil.getUserInfo();
        Gathering gathering = gatheringRepository.findById(id)
                .orElseThrow(() -> new AlertException("모임을 찾을 수 없습니다."));

        if (!gathering.getCreatedBy().equals(user)) {
            throw new AlertException("취소 권한이 없습니다.");
        }

        // 실제 삭제 대신 상태를 취소로 변경
        gathering.cancel();
    }

    /**
     * 내가 생성한 모임 목록 조회 API
     * @param includeCanceled 취소된 모임 포함 여부
     * @param pageable 페이징 정보
     * @return 내가 생성한 모임 목록과 페이징 정보
     */
    @Transactional(readOnly = true)
    public GatheringListResponse getMyCreatedGatherings(boolean includeCanceled, Pageable pageable) {
        User user = AuthUtil.getUserInfo();

        Page<Gathering> gatheringPage = gatheringRepository.findByCreatedBy(user, includeCanceled, pageable);

        List<GatheringSummary> summaries = gatheringPage.getContent().stream()
                .map(GatheringSummary::of)
                .toList();
        
        // 페이징 정보와 함께 응답 객체 생성
        return GatheringListResponse.builder()
                .gatherings(summaries)
                .currentPage(gatheringPage.getNumber())
                .totalPage(gatheringPage.getTotalPages())
                .totalElements(gatheringPage.getTotalElements())
                .build();
    }

}
