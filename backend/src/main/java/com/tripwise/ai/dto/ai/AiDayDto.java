package com.tripwise.ai.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiDayDto {
    public int dayNumber;
    public String title;
    public List<AiActivityDto> activities;
}
