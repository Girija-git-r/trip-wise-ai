package com.tripwise.ai.repository;

import com.tripwise.ai.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Trip> findByUserIdAndSavedTrueOrderByCreatedAtDesc(UUID userId);
}
