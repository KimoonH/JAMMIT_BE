package com.jammit_be.gathering.service;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.gathering.dto.GatheringSummary;
import com.jammit_be.gathering.dto.request.GatheringCreateRequest;
import com.jammit_be.gathering.dto.request.GatheringSessionRequest;
import com.jammit_be.gathering.dto.response.GatheringCreateResponse;
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

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;

    /**
     * 모임 등록
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
     * 모임 전체 목록 조회
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

}
