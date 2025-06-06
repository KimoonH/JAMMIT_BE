package com.jammit_be.gathering.dto.response;

import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.gathering.entity.Gathering;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GatheringCreateResponse {

    private final Long id; // 생성된 모임 PK
    private final String name; // 모임 이름
    private final String message; // 성공 메시지
    private final LocalDateTime gatheringDateTime; // 실제 모임 시간
    private final LocalDateTime recruitDeadline; // 모집 마감일
    private final String thumbnail; // 이미지 URL
    private final GatheringStatus status; // 모임 상태


    public static GatheringCreateResponse from(Gathering gathering) {
        return GatheringCreateResponse.builder()
                .id(gathering.getId())
                .name(gathering.getName())
                .message("모임이 정상 등록되었습니다.")
                .gatheringDateTime(gathering.getGatheringDateTime())
                .recruitDeadline(gathering.getRecruitDeadline())
                .thumbnail(gathering.getThumbnail())
                .status(gathering.getStatus())
                .build();
    }
}
