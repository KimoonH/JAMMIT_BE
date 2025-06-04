package com.jammit_be.gathering.dto.request;

import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.common.enums.Genre;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GatheringCreateRequest {

    @Schema(description = "모임 이름", example = "무한동력")
    private String name; // 모임 이름
    @Schema(description = "모임 이미지 URL")
    private String thumbnail; // 모임 이미지
    @Schema(description = "모임 장소", example = "홍대 합주실")
    private String place; // 모임 장소
    @Schema(description = "모임 소개", example = "락음악을 사랑하는 분 환영")
    private String description; // 모임 소개
    @Schema(description = "모임 일시", example = "2025-07-20T19:00:00")
    private LocalDateTime gatheringDateTime; // 모임 일시
    @Schema(description = "모집 마감일", example = "2025-07-15T23:59:59")
    private LocalDateTime recruitDateTime; // 모임 마감 일시
    @ArraySchema(schema = @Schema(implementation = Genre.class))
    private Set<Genre> genres; // 밴드 장르
    @Schema(description = "모임 상태", example = "RECRUITING")
    private GatheringStatus status;
    @Schema(description = "총 모집 인원", example = "6")
    private int totalRecruitCount; // 총 모집 인원
    @ArraySchema(schema = @Schema(implementation = GatheringSessionRequest.class))
    private List<GatheringSessionRequest> gatheringSessions; // 모임 세션들
}
