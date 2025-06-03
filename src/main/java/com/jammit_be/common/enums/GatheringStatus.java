package com.jammit_be.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GatheringStatus {
    ACTIVE("활성"),
    CANCELED("취소됨");

    private final String description;
} 