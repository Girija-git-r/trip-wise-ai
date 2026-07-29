package com.tripwise.ai.service;

import com.tripwise.ai.dto.ai.AiActivityDto;
import com.tripwise.ai.dto.ai.AiDayDto;
import com.tripwise.ai.dto.ai.AiItineraryResultDto;
import com.tripwise.ai.dto.ai.AiPackingItemDto;
import com.tripwise.ai.dto.trip.*;
import com.tripwise.ai.entity.*;
import com.tripwise.ai.exception.ForbiddenException;
import com.tripwise.ai.exception.ResourceNotFoundException;
import com.tripwise.ai.repository.PackingItemRepository;
import com.tripwise.ai.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final PackingItemRepository packingItemRepository;
    private final ItineraryGeneratorService itineraryGeneratorService;
    private final GeminiAiService geminiAiService;
    private final UserService userService;

    @Transactional
    public TripResponse planTrip(Authentication authentication, TripPlanRequest request) {
        User user = userService.getCurrentUser(authentication);

        Trip trip = Trip.builder()
                .user(user)
                .destination(request.destination().trim())
                .days(request.days())
                .budget(request.budget())
                .travelType(request.travelType())
                .interests(request.interests())
                .startDate(request.startDate())
                .saved(false)
                .build();

        Optional<AiItineraryResultDto> aiResult = geminiAiService.generate(trip);

        if (aiResult.isPresent()) {
            trip.setItineraryDays(buildItineraryDaysFromAi(trip, aiResult.get().days));
            trip.setPackingItems(buildPackingItemsFromAi(trip, aiResult.get().packingList));
            trip.setAiGenerated(true);
        } else {
            trip.setItineraryDays(itineraryGeneratorService.generateItinerary(trip));
            trip.setPackingItems(itineraryGeneratorService.generatePackingList(trip));
            trip.setAiGenerated(false);
        }

        Trip saved = tripRepository.save(trip);
        return toFullResponse(saved);
    }

    private List<ItineraryDay> buildItineraryDaysFromAi(Trip trip, List<AiDayDto> aiDays) {
        List<ItineraryDay> days = new ArrayList<>();
        for (AiDayDto aiDay : aiDays) {
            ItineraryDay day = ItineraryDay.builder()
                    .trip(trip)
                    .dayNumber(aiDay.dayNumber)
                    .title(aiDay.title)
                    .build();

            List<Activity> activities = new ArrayList<>();
            for (AiActivityDto aiActivity : aiDay.activities) {
                activities.add(Activity.builder()
                        .itineraryDay(day)
                        .description(aiActivity.description)
                        .category(aiActivity.category)
                        .build());
            }
            day.setActivities(activities);
            days.add(day);
        }
        return days;
    }

    private List<PackingItem> buildPackingItemsFromAi(Trip trip, List<AiPackingItemDto> aiItems) {
        List<PackingItem> items = new ArrayList<>();
        for (AiPackingItemDto aiItem : aiItems) {
            items.add(PackingItem.builder()
                    .trip(trip)
                    .name(aiItem.name)
                    .category(aiItem.category)
                    .checked(false)
                    .tip(aiItem.tip)
                    .build());
        }
        return items;
    }

    @Transactional(readOnly = true)
    public List<TripSummaryResponse> getMyTrips(Authentication authentication, boolean savedOnly) {
        User user = userService.getCurrentUser(authentication);
        List<Trip> trips = savedOnly
                ? tripRepository.findByUserIdAndSavedTrueOrderByCreatedAtDesc(user.getId())
                : tripRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return trips.stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(Authentication authentication, Long tripId) {
        Trip trip = findTripOwnedByUser(authentication, tripId);
        return toFullResponse(trip);
    }

    @Transactional
    public TripResponse updateSavedStatus(Authentication authentication, Long tripId, SavedUpdateRequest request) {
        Trip trip = findTripOwnedByUser(authentication, tripId);
        trip.setSaved(request.saved());
        return toFullResponse(tripRepository.save(trip));
    }

    @Transactional
    public void deleteTrip(Authentication authentication, Long tripId) {
        Trip trip = findTripOwnedByUser(authentication, tripId);
        tripRepository.delete(trip);
    }

    @Transactional
    public PackingItemResponse updatePackingItem(Authentication authentication, Long tripId, Long itemId, PackingItemUpdateRequest request) {
        Trip trip = findTripOwnedByUser(authentication, tripId);
        PackingItem item = trip.getPackingItems().stream()
                .filter(pi -> pi.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Packing item not found"));

        item.setChecked(request.checked());
        packingItemRepository.save(item);

        return PackingItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .checked(item.getChecked())
                .tip(item.getTip())
                .build();
    }

    private Trip findTripOwnedByUser(Authentication authentication, Long tripId) {
        User user = userService.getCurrentUser(authentication);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this trip");
        }
        return trip;
    }

    private TripResponse toFullResponse(Trip trip) {
        List<ItineraryDayResponse> itinerary = trip.getItineraryDays().stream()
                .sorted(Comparator.comparing(ItineraryDay::getDayNumber))
                .map(day -> ItineraryDayResponse.builder()
                        .id(day.getId())
                        .dayNumber(day.getDayNumber())
                        .title(day.getTitle())
                        .activities(day.getActivities().stream()
                                .map(a -> ActivityResponse.builder()
                                        .id(a.getId())
                                        .description(a.getDescription())
                                        .category(a.getCategory())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        List<PackingItemResponse> packingList = trip.getPackingItems().stream()
                .map(item -> PackingItemResponse.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .category(item.getCategory())
                        .checked(item.getChecked())
                        .tip(item.getTip())
                        .build())
                .collect(Collectors.toList());

        return TripResponse.builder()
                .id(trip.getId())
                .destination(trip.getDestination())
                .days(trip.getDays())
                .budget(trip.getBudget())
                .travelType(trip.getTravelType())
                .interests(trip.getInterests())
                .startDate(trip.getStartDate())
                .saved(trip.getSaved())
                .aiGenerated(trip.getAiGenerated())
                .createdAt(trip.getCreatedAt())
                .itinerary(itinerary)
                .packingList(packingList)
                .build();
    }

    private TripSummaryResponse toSummaryResponse(Trip trip) {
        return TripSummaryResponse.builder()
                .id(trip.getId())
                .destination(trip.getDestination())
                .days(trip.getDays())
                .budget(trip.getBudget())
                .travelType(trip.getTravelType())
                .interests(trip.getInterests())
                .startDate(trip.getStartDate())
                .saved(trip.getSaved())
                .aiGenerated(trip.getAiGenerated())
                .createdAt(trip.getCreatedAt())
                .build();
    }
}
