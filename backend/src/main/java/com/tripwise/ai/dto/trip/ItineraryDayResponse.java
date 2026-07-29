package com.tripwise.ai.dto.trip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDayResponse {
    private Long id;
    private Integer dayNumber;
    private String title;
    private List<ActivityResponse> activities;
}
