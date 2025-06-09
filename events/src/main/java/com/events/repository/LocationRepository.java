package com.events.repository;


import com.events.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    Optional<Location> findByCountryAndCityAndAddress(String country, String city, String address);
}
