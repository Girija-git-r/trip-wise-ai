package com.tripwise.ai.repository;

import com.tripwise.ai.entity.PackingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackingItemRepository extends JpaRepository<PackingItem, Long> {
    List<PackingItem> findByTripId(Long tripId);
}
