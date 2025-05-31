package com.carrental.repository;

import com.carrental.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
}