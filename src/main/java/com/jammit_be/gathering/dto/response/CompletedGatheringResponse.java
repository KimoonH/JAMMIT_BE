package com.jammit_be.gathering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CompletedGatheringResponse {


    @Schema(description = "모임 ID", example = "1")
    private final Long id;

    @Schema(description = "모임 이름", example = "신촌 밴드 모집")
    private final String name;

    @Schema(description = "모임 대표 이미지 URL", example = "https://cdn.example.com/images/thumbnail1.jpg")
    private final String thumbnail;

    @Schema(description = "모임 일시", example = "2025-07-01T19:00:00")
    private final LocalDateTime gatheringDateTime;

    @Schema(description = "모임 장소", example = "신촌 연습실 1호점")
    private final String place;

    @Schema(description = "총 모집 인원", example = "5")
    private final int totalRecruit;

    @Schema(description = "현재 참여자 수", example = "5")
    private final int totalCurrent;

    @Schema(description = "모임 상태", example = "COMPLETED")
    private final String status;
}
