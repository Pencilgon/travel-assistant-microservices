package com.carrental.repository;

import com.carrental.entity.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CarImageRepository extends JpaRepository<CarImage, UUID> {
}
