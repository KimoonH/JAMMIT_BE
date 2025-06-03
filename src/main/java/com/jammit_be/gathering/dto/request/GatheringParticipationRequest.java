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
}
