package com.tripwise.ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_day_id", nullable = false)
    @JsonIgnore
    private ItineraryDay itineraryDay;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** e.g. SIGHTSEEING, FOOD, ADVENTURE, RELAXATION, CULTURE, SHOPPING, TRANSPORT */
    @Column(nullable = false, length = 40)
    private String category;
}
