package com.jammit_be.gathering.dto.response;

import com.jammit_be.common.enums.Genre;
import com.jammit_be.gathering.dto.GatheringSessionInfo;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringSession;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class GatheringDetailResponse {
    @Schema(description = "모임 PK", example = "1")
    private final Long id;
    @Schema(description = "모임 이름", example = "터치드")
    private final String name;
    @Schema(description = "모임 이미지")
    private final String thumbnail;
    @Schema(description = "모임 장소", example = "홍대 합주실")
    private final String place;
    @Schema(description = "곡", example = "Hi Bully")
    private final String song;
    @Schema(description = "모임 소개")
    private final String description;
    @Schema(description = "모임 일시")
    private final LocalDateTime gatheringDateTime; // 모임 일시
    @Schema(description = "모집 마감일")
    private final LocalDateTime recruitDeadline; // 마감일
    @ArraySchema(schema = @Schema(implementation = Genre.class))
    private final Set<Genre> genres;
    @ArraySchema(schema = @Schema(implementation = GatheringSessionInfo.class))
    private final List<GatheringSessionInfo> sessions; // 세션별 정보

    public static GatheringDetailResponse from(Gathering gathering) {
        List<GatheringSessionInfo> sessionInfos = new ArrayList<>();
        for (GatheringSession s : gathering.getGatheringSessions()) {
            sessionInfos.add(GatheringSessionInfo.builder()
                    .bandSession(s.getName())
                    .recruitCount(s.getRecruitCount())
                    .currentCount(s.getCurrentCount())
                    .build());
        }
        return GatheringDetailResponse.builder()
                .id(gathering.getId())
                .name(gathering.getName())
                .place(gathering.getPlace())
                .thumbnail(gathering.getThumbnail())
                .description(gathering.getDescription())
                .song(gathering.getSong())
                .gatheringDateTime(gathering.getGatheringDateTime())
                .recruitDeadline(gathering.getRecruitDeadline())
                .genres(gathering.getGenres())
                .sessions(sessionInfos)
                .build();
    }
}
