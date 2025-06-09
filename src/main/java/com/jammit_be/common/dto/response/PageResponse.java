package com.jammit_be.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페이지네이션 응답")
public class PageResponse<T> {

    @Schema(description = "페이지 컨텐츠")
    private List<T> content;
    
    @Schema(description = "현재 페이지 번호", example = "0")
    private int page;
    
    @Schema(description = "페이지 크기", example = "10")
    private int size;
    
    @Schema(description = "전체 요소 수", example = "100")
    private long totalElements;
    
    @Schema(description = "전체 페이지 수", example = "10")
    private int totalPages;
    
    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;
} 