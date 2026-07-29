package com.tripwise.ai.repository;

import com.tripwise.ai.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Trip> findByUserIdAndSavedTrueOrderByCreatedAtDesc(Long userId);
}
