package com.jammit_be.gathering.dto.request;

import com.jammit_be.common.enums.Genre;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GatheringUpdateRequest {

    @Schema(description = "모임 이름", example = "홍대 락밴드 모임")
    private String name;                   // 모임 이름
    @Schema(description = "모임 이미지 URL 및 파일", example = "https://example.com/image.jpg")
    private String thumbnail;              // 모임 이미지
    @Schema(description = "모임 장소", example = "홍대 합주실 2호점")
    private String place;                  // 모임 장소
    @Schema(description = "모임 날짜/시간", example = "2025-07-10T19:00:00")
    private LocalDateTime gatheringDateTime; // 모임 날짜/시간
    @Schema(description = "총 모집 인원", example = "5")
    private int totalRecruitCount;         // 모집 인원
    @Schema(description = "모집 마감일", example = "2025-07-08T23:59:59")
    private LocalDateTime recruitDeadline; // 모집 마감일
    @ArraySchema(
            schema = @Schema(implementation = com.jammit_be.common.enums.Genre.class),
            arraySchema = @Schema(description = "밴드 장르(여러개)", example = "[\"ROCK\", \"JAZZ\"]")
    )
    private Set<Genre> genres;             // 밴드 장르(여러개)
    @Schema(description = "간단 소개", example = "경험 많은 드러머 구합니다!")
    private String description;            // 간단 소개
    @ArraySchema(
            schema = @Schema(implementation = GatheringSessionRequest.class),
            arraySchema = @Schema(description = "각 파트별 모집 인원", example = "[{\"bandSession\":\"VOCAL\", \"recruitCount\":1}, {\"bandSession\":\"KEYBOARD\", \"recruitCount\":1}]")
    )
    private List<GatheringSessionRequest> gatheringSessions; // 각 파트별 모집 인원 수정
}
