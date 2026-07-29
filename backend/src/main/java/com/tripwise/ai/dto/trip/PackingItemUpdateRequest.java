package com.tripwise.ai.dto.trip;

import jakarta.validation.constraints.NotNull;

public record PackingItemUpdateRequest(
        @NotNull(message = "Checked flag is required")
        Boolean checked
) {
}
