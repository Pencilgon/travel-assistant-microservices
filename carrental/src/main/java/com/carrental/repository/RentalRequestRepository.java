package com.carrental.repository;

import com.carrental.entity.RentalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, UUID> {
    List<RentalRequest> findByUserId(UUID userId);
    List<RentalRequest> findByCar_Owner_Id(UUID ownerId);
    long countByUserIdAndStatusIn(UUID userId, List<String> statuses);
}
