package com.tripwise.ai.dto.trip;

import jakarta.validation.constraints.NotNull;

public record SavedUpdateRequest(
        @NotNull(message = "Saved flag is required")
        Boolean saved
) {
}
