package com.carrental.repository;

import com.carrental.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    Optional<Location> findByCountryAndCity(String country, String city);
    List<Location> findByCountry(String country);
}
