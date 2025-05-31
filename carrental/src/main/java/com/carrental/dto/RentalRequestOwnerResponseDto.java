package com.carrental.dto;

import com.carrental.entity.CarImage;
import com.carrental.entity.RentalRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RentalRequestOwnerResponseDto {

    private UUID id;
    private UUID userId;
    private UUID carId;
    private String carBrand;
    private String carModel;
    private int carYear;
    private List<String> carImageUrls;
    private String drivingLicenseUrl;
    private String selfieUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public RentalRequestOwnerResponseDto(RentalRequest request) {
        this.id = request.getId();
        this.userId = request.getUserId();
        this.carId = request.getCar().getId();
        this.carBrand = request.getCar().getBrand();
        this.carModel = request.getCar().getModel();
        this.carYear = request.getCar().getYear();
        this.carImageUrls = request.getCar().getImages().stream()
                .map(CarImage::getUrl)
                .toList();
        this.drivingLicenseUrl = request.getDrivingLicenseUrl();
        this.selfieUrl = request.getSelfieUrl();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.status = request.getStatus();
    }

    // геттеры и сеттеры если нужны

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getCarYear() {
        return carYear;
    }

    public String getDrivingLicenseUrl() {
        return drivingLicenseUrl;
    }

    public void setDrivingLicenseUrl(String drivingLicenseUrl) {
        this.drivingLicenseUrl = drivingLicenseUrl;
    }

    public String getSelfieUrl() {
        return selfieUrl;
    }

    public void setSelfieUrl(String selfieUrl) {
        this.selfieUrl = selfieUrl;
    }

    public void setCarYear(int carYear) {
        this.carYear = carYear;
    }

    public List<String> getCarImageUrls() {
        return carImageUrls;
    }

    public void setCarImageUrls(List<String> carImageUrls) {
        this.carImageUrls = carImageUrls;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
