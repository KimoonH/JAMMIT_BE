package com.jammit_be.gathering.dto.request;

import com.jammit_be.common.enums.Genre;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class GatheringCreateRequest {

    private String name; // 모임 이름
    private String thumbnail; // 모임 이미지
    private String place; // 모임 장소
    private String description; // 모임 소개
    private String song; // 모임 곡
    private LocalDateTime gatheringDateTime; // 모임 일시
    private LocalDateTime recruitDateTime; // 모임 마감 일시
    private Set<Genre> genres; // 밴드 장르
    private int totalRecruitCount; // 총 모집 인원
    private List<GatheringSessionRequest> gatheringSessions; // 모임 세션들

}
