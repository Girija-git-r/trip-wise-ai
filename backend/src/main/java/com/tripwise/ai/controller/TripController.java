package com.tripwise.ai.controller;

import com.tripwise.ai.dto.trip.*;
import com.tripwise.ai.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    /** AI-like itinerary + packing list generation, persisted as a new trip. */
    @PostMapping("/plan")
    public ResponseEntity<TripResponse> planTrip(Authentication authentication,
                                                  @Valid @RequestBody TripPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.planTrip(authentication, request));
    }

    @GetMapping
    public ResponseEntity<List<TripSummaryResponse>> getMyTrips(Authentication authentication,
                                                                  @RequestParam(value = "saved", required = false, defaultValue = "false") boolean savedOnly) {
        return ResponseEntity.ok(tripService.getMyTrips(authentication, savedOnly));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTrip(Authentication authentication, @PathVariable Long tripId) {
        return ResponseEntity.ok(tripService.getTripById(authentication, tripId));
    }

    @PatchMapping("/{tripId}/saved")
    public ResponseEntity<TripResponse> updateSaved(Authentication authentication,
                                                      @PathVariable Long tripId,
                                                      @Valid @RequestBody SavedUpdateRequest request) {
        return ResponseEntity.ok(tripService.updateSavedStatus(authentication, tripId, request));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(Authentication authentication, @PathVariable Long tripId) {
        tripService.deleteTrip(authentication, tripId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{tripId}/packing-items/{itemId}")
    public ResponseEntity<PackingItemResponse> updatePackingItem(Authentication authentication,
                                                                   @PathVariable Long tripId,
                                                                   @PathVariable Long itemId,
                                                                   @Valid @RequestBody PackingItemUpdateRequest request) {
        return ResponseEntity.ok(tripService.updatePackingItem(authentication, tripId, itemId, request));
    }
}
