package com.jammit_be.gathering.dto.request;

import com.jammit_be.common.enums.BandSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class GatheringParticipationRequest {

    @Schema(description = "참여할 밴드 세션", example = "KEYBOARD", allowableValues = {"VOCAL", "ELECTRIC_GUITAR", "DRUM", "ACOUSTIC_GUITAR", "BASS", "STRING_INSTRUMENT", "PERCUSSION", "KEYBOARD"})
    private BandSession bandSession;
    
    @Schema(description = "참여자 간단 소개 문구", example = "안녕하세요! 키보드 연주 경력 3년입니다. 함께 연주하고 싶어요.")
    private String introduction;
}
