package com.jammit_be.gathering.dto.response;

import com.jammit_be.gathering.dto.GatheringSummary;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GatheringListResponse {
    private final List<GatheringSummary> gatherings;
    private final int currentPage;
    private final int totalPage;
    private final long totalElements;
}
