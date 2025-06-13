package com.carrental.repository;

import com.carrental.entity.City;
import com.carrental.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
    Optional<City> findByNameIgnoreCaseAndCountry(String city, Country country);
}
