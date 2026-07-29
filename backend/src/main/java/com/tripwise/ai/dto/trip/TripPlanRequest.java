package com.tripwise.ai.dto.trip;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record TripPlanRequest(
        @NotBlank(message = "Destination is required")
        @Size(max = 160, message = "Destination must be at most 160 characters")
        String destination,

        @NotNull(message = "Number of days is required")
        @Min(value = 1, message = "Trip must be at least 1 day")
        @Max(value = 30, message = "Trip cannot exceed 30 days")
        Integer days,

        @NotNull(message = "Budget is required")
        @Positive(message = "Budget must be greater than 0")
        Double budget,

        @NotBlank(message = "Travel type is required")
        String travelType,

        @NotNull(message = "Interests are required")
        @Size(min = 1, message = "Select at least one interest")
        List<String> interests,

        LocalDate startDate
) {
}
