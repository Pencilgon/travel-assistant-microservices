package com.carrental.repository;

import com.carrental.entity.City;
import com.carrental.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByCityAndLatitudeAndLongitude(City city, Double latitude, Double longitude);
}
