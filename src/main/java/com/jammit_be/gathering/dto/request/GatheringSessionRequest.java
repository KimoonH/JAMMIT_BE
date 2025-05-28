package com.jammit_be.gathering.dto.request;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.gathering.entity.GatheringSession;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatheringSessionRequest {

    private BandSession bandSession;
    private int recruitCount;

    public GatheringSession toEntity() {
        return GatheringSession.create(bandSession, recruitCount);
    }
}
