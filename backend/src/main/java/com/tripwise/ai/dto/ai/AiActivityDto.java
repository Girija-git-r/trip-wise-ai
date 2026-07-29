package com.tripwise.ai.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiActivityDto {
    public String description;
    public String category;
}
