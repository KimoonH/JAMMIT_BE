package com.jammit_be.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailCheckResponse {

    @Schema(description = "이메일 중복 여부", example = "true")
    private Boolean exists;

}
