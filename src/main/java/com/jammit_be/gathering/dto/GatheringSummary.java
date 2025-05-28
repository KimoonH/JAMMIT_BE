package com.jammit_be.gathering.dto;

import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringSession;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GatheringSummary {

    private final Long id;
    private final String name;
    private final String place;
    private final String thumbnail;
    private final LocalDateTime gatheringDateTime;
    private final int totalRecruit; // 세션 별 모집 정원 합
    private final int totalCurrent; // 세션별 현재 인원 합
    private final int viewCount;
    private final LocalDateTime recruitDeadline;

    public static GatheringSummary of(Gathering gathering) {
        return GatheringSummary.builder()
                .id(gathering.getId())
                .name(gathering.getName())
                .place(gathering.getPlace())
                .thumbnail(gathering.getThumbnail())
                .gatheringDateTime(gathering.getGatheringDateTime())
                .totalCurrent(gathering.getGatheringSessions().stream().mapToInt(GatheringSession::getRecruitCount).sum())
                .totalRecruit(gathering.getGatheringSessions().stream().mapToInt(GatheringSession::getCurrentCount).sum())
                .viewCount(gathering.getViewCount())
                .recruitDeadline(gathering.getRecruitDeadline())
                .build();
    }
}
