package com.jammit_be.review.dto.response;

import java.time.LocalDateTime;

public interface UnwrittenReviewProjection {
    Long getGatheringId();
    String getGatheringName();
    String getGatheringThumbnail();
    Long getParticipantId();
    Long getUserId();
    String getUserNickname();
    String getUserEmail();
    String getBandSession();
    String getStatus();
    LocalDateTime getCreatedAt();
    String getIntroduction();
} 