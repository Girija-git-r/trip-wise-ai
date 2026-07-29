package com.tripwise.ai.dto.trip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSummaryResponse {
    private Long id;
    private String destination;
    private Integer days;
    private Double budget;
    private String travelType;
    private List<String> interests;
    private LocalDate startDate;
    private Boolean saved;
    private Boolean aiGenerated;
    private LocalDateTime createdAt;
}
