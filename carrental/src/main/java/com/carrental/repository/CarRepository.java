package com.carrental.repository;

import com.carrental.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {
    List<Car> findByLocation_CountryAndLocation_City(String country, String city);
    int countByOwnerId(UUID ownerId);
    List<Car> findByOwner_Id(UUID ownerId);
}
