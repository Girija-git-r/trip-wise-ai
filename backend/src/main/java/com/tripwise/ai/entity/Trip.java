package com.tripwise.ai.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 160)
    private String destination;

    @Column(nullable = false)
    private Integer days;

    @Column(nullable = false)
    private Double budget;

    @Column(name = "travel_type", nullable = false, length = 40)
    private String travelType;

    @Type(JsonType.class)
    @Column(name = "interests", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> interests = new ArrayList<>();

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean saved = false;

    @Column(name = "ai_generated", nullable = false)
    @Builder.Default
    private Boolean aiGenerated = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("dayNumber ASC")
    @Builder.Default
    private List<ItineraryDay> itineraryDays = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    @Builder.Default
    private List<PackingItem> packingItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
