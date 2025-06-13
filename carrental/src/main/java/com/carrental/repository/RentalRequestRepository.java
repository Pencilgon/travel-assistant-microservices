package com.carrental.repository;

import com.carrental.entity.RentalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, UUID> {
    List<RentalRequest> findByUserId(UUID userId);
    long countByUserIdAndStatusIn(UUID userId, List<String> statuses);

    List<RentalRequest> findByCar_OwnerId(UUID ownerId);
}
