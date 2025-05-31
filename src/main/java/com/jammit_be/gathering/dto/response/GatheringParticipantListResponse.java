package com.jammit_be.gathering.dto.response;

import com.jammit_be.gathering.dto.GatheringParticipantSummary;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GatheringParticipantListResponse {
    private final List<GatheringParticipantSummary> participants;
    private final int total; // 신청한 총 인원 수
}
