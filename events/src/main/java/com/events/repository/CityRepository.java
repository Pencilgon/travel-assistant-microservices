package com.events.repository;

import com.events.entity.City;
import com.events.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
    Optional<City> findByNameAndCountry(String name, Country country);
    List<City> findByCountry(Country country);
}
