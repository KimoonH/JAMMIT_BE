package com.jammit_be.gathering.dto.response;

import com.jammit_be.gathering.dto.GatheringSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "모임 리스트 + 페이징 정보 응답")
public class GatheringListResponse {
    @Schema(description = "모임 요약 리스트")
    private final List<GatheringSummary> gatherings; // 현재 페이지에 노출되는 모임 리스트
    @Schema(description = "현재 페이지 번호 (0-base)", example = "0")
    private final int currentPage; // 현제 페이지 번호
    @Schema(description = "전체 페이지 수", example = "5")
    private final int totalPage; // 전체 페이지 수
    @Schema(description = "검색 결과 전체 건수", example = "42")
    private final long totalElements; // 전체 모임 개수
}
