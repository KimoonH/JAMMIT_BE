package com.jammit_be.review.dto.response;

import com.jammit_be.gathering.dto.GatheringParticipantSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "내가 리뷰를 작성하지 않은 참가자 목록 응답")
public class UnwrittenReviewListResponse {
    @Schema(description = "모임 ID", example = "1")
    private Long gatheringId;
    @Schema(description = "모임 이름", example = "락밴드 모임")
    private String gatheringName;
    @Schema(description = "모임 썸네일", example = "https://example.com/thumbnail.jpg")
    private String gatheringThumbnail;
    @Schema(description = "리뷰를 작성하지 않은 참가자 목록")
    private List<GatheringParticipantSummary> unwrittenParticipants;

    public UnwrittenReviewListResponse(Long gatheringId, String gatheringName, String gatheringThumbnail, List<GatheringParticipantSummary> unwrittenParticipants) {
        this.gatheringId = gatheringId;
        this.gatheringName = gatheringName;
        this.gatheringThumbnail = gatheringThumbnail;
        this.unwrittenParticipants = unwrittenParticipants;
    }
} 