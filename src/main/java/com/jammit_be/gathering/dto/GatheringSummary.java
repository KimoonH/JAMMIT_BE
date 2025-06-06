package com.jammit_be.gathering.dto;

import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.GatheringSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "모임 요약 정보")
public class GatheringSummary {

    @Schema(description = "모임 ID (PK)", example = "1")
    private final Long id; // 모임 PK
    @Schema(description = "모임 이름", example = "신촌 밴드 모집")
    private final String name; // 모임 이름
    @Schema(description = "모임 장소", example = "신촌 연습실")
    private final String place; // 모임 장소
    @Schema(description = "모임 대표 이미지 URL & 파일", example = "https://cdn.example.com/img.jpg")
    private final String thumbnail; // 모임 썸네일
    @Schema(description = "모임 일시(시작 시간)", example = "2025-05-29T19:00:00")
    private final LocalDateTime gatheringDateTime; // 실제 모임 일시(합주 시간)
    @Schema(description = "총 모집 정원", example = "5")
    private final int totalRecruit; // 총 모집 인원
    @Schema(description = "현재 참가자 수", example = "3")
    private final int totalCurrent; // 현재 모집된 인원
    @Schema(description = "조회수", example = "123")
    private final int viewCount; // 해당 모임의 조회수
    @Schema(description = "모집 마감일시", example = "2025-06-30T23:59:59")
    private final LocalDateTime recruitDeadline; // 모집 마감 일시(마감일 기준)
    @Schema(description = "모임 상태", example = "RECRUITING", 
            allowableValues = {"RECRUITING", "CONFIRMED", "COMPLETED", "CANCELED"},
            implementation = GatheringStatus.class)
    private final GatheringStatus status; // 모임 상태
    @Schema(description = "모임의 장르 목록", example = "[\"ROCK\", \"JAZZ\"]")
    private final Set<Genre> genres; // 장르들 추가
    @Schema(description = "모임 생성자(주최자) 정보")
    private CreatorInfo creator; // 주최자 정보
    @Schema(description = "밴드 세션별 모집 정보")
    private final List<GatheringSessionInfo> sessions;

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
                .status(gathering.getStatus())
                .genres(gathering.getGenres())
                .creator(CreatorInfo.of(gathering.getCreatedBy()))
                .sessions(
                        gathering.getGatheringSessions().stream()
                                .map(session -> GatheringSessionInfo.builder()
                                        .bandSession(session.getName())
                                        .recruitCount(session.getRecruitCount())
                                        .currentCount(session.getCurrentCount())
                                        .build()
                                ).collect(Collectors.toList())
                )
                .build();
    }
}
