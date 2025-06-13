package com.carrental.repository;

import com.carrental.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {
    int countByOwnerId(UUID ownerId);

    List<Car> findByOwnerId(UUID ownerId);
}
