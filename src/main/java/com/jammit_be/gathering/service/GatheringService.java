package com.jammit_be.gathering.service;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.gathering.dto.GatheringSummary;
import com.jammit_be.gathering.dto.request.GatheringCreateRequest;
import com.jammit_be.gathering.dto.request.GatheringSessionRequest;
import com.jammit_be.gathering.dto.response.GatheringCreateResponse;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringSession;
import com.jammit_be.gathering.repository.GatheringRepository;
import com.jammit_be.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<GatheringSummary> findGatherings(List<Genre> genres, List<BandSession> sessions, Pageable pageable) {

        // 리스트 페이징 조회
        return null;
    }

}
