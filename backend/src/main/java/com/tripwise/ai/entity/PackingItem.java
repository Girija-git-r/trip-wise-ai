package com.tripwise.ai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "packing_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonIgnore
    private Trip trip;

    @Column(nullable = false, length = 160)
    private String name;

    /** e.g. CLOTHING, TOILETRIES, ELECTRONICS, DOCUMENTS, HEALTH, MISC */
    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private Boolean checked = false;

    @Column(columnDefinition = "TEXT")
    private String tip;
}
