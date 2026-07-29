package com.tripwise.ai.service;

import com.tripwise.ai.entity.Activity;
import com.tripwise.ai.entity.ItineraryDay;
import com.tripwise.ai.entity.PackingItem;
import com.tripwise.ai.entity.Trip;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Rule-based "AI-like" generator. Produces a day-wise itinerary and a smart,
 * categorized packing list from the trip's destination, budget, travel type
 * and interests. No external AI call is made; everything is deterministic
 * so results are reproducible and fast.
 */
@Service
public class ItineraryGeneratorService {

    private static final List<String> MORNING_TEMPLATES = List.of(
            "Explore the iconic landmarks of %s",
            "Visit a top-rated museum or cultural site in %s",
            "Wander through the historic old town of %s",
            "Take a guided walking tour around %s",
            "Relax at a scenic viewpoint overlooking %s"
    );

    private static final List<String> AFTERNOON_TEMPLATES = List.of(
            "Sample local street food and specialties in %s",
            "Browse local markets and pick up souvenirs in %s",
            "Enjoy an outdoor adventure activity near %s",
            "Visit a nearby nature spot or park in %s",
            "Take a boat ride or scenic drive around %s"
    );

    private static final List<String> EVENING_TEMPLATES = List.of(
            "Dine at a highly-rated local restaurant in %s",
            "Watch the sunset at a popular spot in %s",
            "Enjoy the nightlife or a cultural show in %s",
            "Take a relaxed evening stroll through %s",
            "Try a rooftop cafe with a view of %s"
    );

    private static final Map<String, List<String>> INTEREST_ACTIVITIES = new HashMap<>();

    static {
        INTEREST_ACTIVITIES.put("adventure", List.of(
                "Go hiking or trekking near %s",
                "Try zip-lining or a canopy walk near %s",
                "Book a water sports session near %s"));
        INTEREST_ACTIVITIES.put("culture", List.of(
                "Visit a heritage temple or monument in %s",
                "Attend a traditional dance or music performance in %s",
                "Explore a local history museum in %s"));
        INTEREST_ACTIVITIES.put("food", List.of(
                "Join a food-tasting walking tour in %s",
                "Take a local cooking class in %s",
                "Visit the most famous food market in %s"));
        INTEREST_ACTIVITIES.put("nature", List.of(
                "Visit a botanical garden or nature reserve near %s",
                "Take a scenic nature trail near %s",
                "Go wildlife spotting near %s"));
        INTEREST_ACTIVITIES.put("relaxation", List.of(
                "Enjoy a spa and wellness session in %s",
                "Spend a relaxed afternoon at a beach or lakeside near %s",
                "Unwind with a yoga session near %s"));
        INTEREST_ACTIVITIES.put("shopping", List.of(
                "Explore the best shopping districts of %s",
                "Visit a local artisan market in %s",
                "Shop for local crafts and souvenirs in %s"));
        INTEREST_ACTIVITIES.put("nightlife", List.of(
                "Experience the nightlife scene in %s",
                "Visit a rooftop bar or lounge in %s",
                "Catch a live music venue in %s"));
        INTEREST_ACTIVITIES.put("history", List.of(
                "Tour a historic fort or palace in %s",
                "Visit an archaeological site near %s",
                "Take a guided heritage walk in %s"));
    }

    public List<ItineraryDay> generateItinerary(Trip trip) {
        String destination = trip.getDestination();
        List<String> interests = normalizeInterests(trip.getInterests());
        Random random = new Random(seedFor(trip));

        List<ItineraryDay> days = new ArrayList<>();
        for (int day = 1; day <= trip.getDays(); day++) {
            ItineraryDay itineraryDay = ItineraryDay.builder()
                    .trip(trip)
                    .dayNumber(day)
                    .title(buildDayTitle(day, destination, trip.getDays()))
                    .build();

            List<Activity> activities = new ArrayList<>();
            activities.add(activity(itineraryDay, pick(random, MORNING_TEMPLATES, destination), "SIGHTSEEING"));

            // Weave in an interest-specific activity when available.
            String interestActivity = interestActivityFor(interests, day, destination);
            if (interestActivity != null) {
                activities.add(activity(itineraryDay, interestActivity, categoryForInterestSlot(day)));
            } else {
                activities.add(activity(itineraryDay, pick(random, AFTERNOON_TEMPLATES, destination), "SIGHTSEEING"));
            }

            activities.add(activity(itineraryDay, pick(random, AFTERNOON_TEMPLATES, destination), "FOOD"));
            activities.add(activity(itineraryDay, pick(random, EVENING_TEMPLATES, destination), "FOOD"));

            itineraryDay.setActivities(activities);
            days.add(itineraryDay);
        }
        return days;
    }

    public List<PackingItem> generatePackingList(Trip trip) {
        List<String> interests = normalizeInterests(trip.getInterests());
        String travelType = trip.getTravelType() == null ? "" : trip.getTravelType().toLowerCase();
        List<PackingItem> items = new ArrayList<>();

        // Documents - always required
        addItem(items, trip, "Passport / Government ID", "DOCUMENTS", "Keep a photocopy and a digital backup.");
        addItem(items, trip, "Travel insurance documents", "DOCUMENTS", "Save a digital copy on your phone.");
        addItem(items, trip, "Printed/digital booking confirmations", "DOCUMENTS", null);

        // Clothing - based on trip length
        addItem(items, trip, "Comfortable walking shoes", "CLOTHING", "Break them in before the trip if new.");
        addItem(items, trip, trip.getDays() + " days worth of outfits", "CLOTHING", "Pack versatile, mix-and-match pieces.");
        addItem(items, trip, "Light jacket or layering piece", "CLOTHING", "Evenings can be cooler than expected.");

        // Electronics
        addItem(items, trip, "Phone charger & power bank", "ELECTRONICS", null);
        addItem(items, trip, "Universal travel adapter", "ELECTRONICS", "Check the destination's plug type in advance.");
        addItem(items, trip, "Camera", "ELECTRONICS", interests.contains("nature") ? "A zoom lens helps for wildlife shots." : null);

        // Toiletries
        addItem(items, trip, "Toiletry kit (toothbrush, toothpaste, etc.)", "TOILETRIES", null);
        addItem(items, trip, "Sunscreen", "TOILETRIES", "Essential even on cloudy days.");
        addItem(items, trip, "Reusable water bottle", "TOILETRIES", null);

        // Health
        addItem(items, trip, "Basic first-aid kit", "HEALTH", null);
        addItem(items, trip, "Personal medication", "HEALTH", "Carry a copy of prescriptions if needed.");
        addItem(items, trip, "Hand sanitizer", "HEALTH", null);

        // Interest / travel-type specific
        if (interests.contains("adventure") || interests.contains("nature")) {
            addItem(items, trip, "Hiking boots", "CLOTHING", "Waterproof if trails may be wet.");
            addItem(items, trip, "Insect repellent", "HEALTH", null);
            addItem(items, trip, "Refillable hydration pack", "MISC", null);
        }
        if (interests.contains("relaxation")) {
            addItem(items, trip, "Swimwear", "CLOTHING", null);
            addItem(items, trip, "Beach towel", "MISC", null);
        }
        if (interests.contains("culture") || interests.contains("history")) {
            addItem(items, trip, "Modest clothing for religious/heritage sites", "CLOTHING", "Shoulders and knees covered where required.");
        }
        if (interests.contains("nightlife")) {
            addItem(items, trip, "An evening/going-out outfit", "CLOTHING", null);
        }
        if (interests.contains("shopping")) {
            addItem(items, trip, "Foldable extra bag for souvenirs", "MISC", null);
        }
        if (travelType.contains("business")) {
            addItem(items, trip, "Formal outfit / business attire", "CLOTHING", null);
            addItem(items, trip, "Laptop & charger", "ELECTRONICS", null);
        }
        if (travelType.contains("family")) {
            addItem(items, trip, "Entertainment for kids (books, games)", "MISC", null);
            addItem(items, trip, "Snacks for the journey", "MISC", null);
        }
        if (trip.getBudget() != null && trip.getBudget() < 40000) {
            addItem(items, trip, "Reusable snacks/food to save on costs", "MISC", "Budget trip tip: cook/self-cater where possible.");
        }

        addItem(items, trip, "Local currency / travel card", "MISC", "Notify your bank of travel dates.");
        addItem(items, trip, "Daypack for daily excursions", "MISC", null);

        return items;
    }

    private String interestActivityFor(List<String> interests, int day, String destination) {
        if (interests.isEmpty()) return null;
        String interest = interests.get((day - 1) % interests.size());
        List<String> templates = INTEREST_ACTIVITIES.get(interest);
        if (templates == null || templates.isEmpty()) return null;
        String template = templates.get((day - 1) % templates.size());
        return String.format(template, destination);
    }

    private String categoryForInterestSlot(int day) {
        return "EXPERIENCE";
    }

    private String buildDayTitle(int day, String destination, int totalDays) {
        if (day == 1) return "Day " + day + ": Arrival & First Impressions of " + destination;
        if (day == totalDays) return "Day " + day + ": Farewell to " + destination;
        return "Day " + day + ": Discovering " + destination;
    }

    private Activity activity(ItineraryDay day, String description, String category) {
        return Activity.builder()
                .itineraryDay(day)
                .description(description)
                .category(category)
                .build();
    }

    private void addItem(List<PackingItem> items, Trip trip, String name, String category, String tip) {
        items.add(PackingItem.builder()
                .trip(trip)
                .name(name)
                .category(category)
                .checked(false)
                .tip(tip)
                .build());
    }

    private List<String> normalizeInterests(List<String> interests) {
        if (interests == null) return List.of();
        List<String> normalized = new ArrayList<>();
        for (String interest : interests) {
            if (interest != null && !interest.isBlank()) {
                normalized.add(interest.trim().toLowerCase());
            }
        }
        return normalized;
    }

    private String pick(Random random, List<String> templates, String destination) {
        String template = templates.get(random.nextInt(templates.size()));
        return String.format(template, destination);
    }

    private long seedFor(Trip trip) {
        String basis = (trip.getDestination() == null ? "" : trip.getDestination())
                + "|" + trip.getDays() + "|" + trip.getTravelType();
        return basis.hashCode();
    }
}
