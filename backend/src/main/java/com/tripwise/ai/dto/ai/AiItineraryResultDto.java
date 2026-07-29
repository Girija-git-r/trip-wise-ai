package com.tripwise.ai.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiItineraryResultDto {
    public List<AiDayDto> days;
    public List<AiPackingItemDto> packingList;
}
