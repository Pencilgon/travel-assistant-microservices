package com.carrental.dto;

import java.time.LocalDate;
import java.util.UUID;

public class RentalRequestDto {
    private UUID carId;
    private LocalDate startDate;
    private LocalDate endDate;

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}
