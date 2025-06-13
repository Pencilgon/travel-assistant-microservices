package com.events.repository;

import com.events.entity.Address;
import com.events.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    Optional<Address> findByDetailsAndCity(String details, City city);
}
