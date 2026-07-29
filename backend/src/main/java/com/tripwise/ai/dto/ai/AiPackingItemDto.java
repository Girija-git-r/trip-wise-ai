package com.tripwise.ai.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiPackingItemDto {
    public String name;
    public String category;
    public String tip;
}
