package com.jammit_be.gathering.service;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.dto.GatheringSessionInfo;
import com.jammit_be.gathering.dto.GatheringSummary;
import com.jammit_be.gathering.dto.request.GatheringCreateRequest;
import com.jammit_be.gathering.dto.request.GatheringSessionRequest;
import com.jammit_be.gathering.dto.response.GatheringCreateResponse;
import com.jammit_be.gathering.dto.response.GatheringDetailResponse;
import com.jammit_be.gathering.dto.response.GatheringListResponse;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringSession;
import com.jammit_be.gathering.repository.GatheringRepository;
import com.jammit_be.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;

    /**
     * 모임 등록 API
     * @param request 모임 요청 데이터
     * @param user
     * @return
     */
    @Transactional
    public GatheringCreateResponse createGathering(GatheringCreateRequest request, User user) {
        List<GatheringSession> sessionEntities = request.getGatheringSessions().stream()
                .map(GatheringSessionRequest::toEntity)
                .toList();

        Gathering gathering = Gathering.create(
                request.getName()
                ,request.getThumbnail()
                ,request.getPlace()
                ,request.getDescription()
                ,request.getSong()
                ,request.getGatheringDateTime()
                ,request.getRecruitDateTime()
                ,request.getGenres()
                ,sessionEntities
                ,user
        );

        Gathering saved = gatheringRepository.save(gathering);

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
                .genres(gathering.getGenres())
                .sessions(sessionInfos)
                .build();
    }
}
