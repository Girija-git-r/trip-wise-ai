package com.tripwise.ai.dto.trip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackingItemResponse {
    private Long id;
    private String name;
    private String category;
    private Boolean checked;
    private String tip;
}
