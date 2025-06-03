package com.jammit_be.gathering.dto;

import com.jammit_be.common.enums.BandSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatheringSessionInfo {
    @Schema(description = "밴드 세션", example = "KEYBOARD")
    private final BandSession bandSession; // 파트(VOCAL, GUITAR 등)
    @Schema(description = "모집 정원", example = "2")
    private final int recruitCount; // 모집 정원
    @Schema(description = "현재 인원", example = "1")
    private final int currentCount; // 현재 충원 인원
}
