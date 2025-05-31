package com.carrental.dto;

import com.carrental.entity.CarImage;
import com.carrental.entity.RentalRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RentalRequestUserResponseDto {

    private UUID id;
    private UUID carId;
    private String carBrand;
    private String carModel;
    private int carYear;
    private String carColor;
    private String carDescription;
    private List<String> carImageUrls;
    private double carPricePerDay;
    private String carCountry;
    private String carCity;
    private String carOwnerLastName;
    private String carOwnerFirstName;
    private String carOwnerEmail;
    private String carOwnerPhoneNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String drivingLicenseUrl;
    private String selfieUrl;

    public RentalRequestUserResponseDto(RentalRequest request) {
        this.id = request.getId();
        this.carId = request.getCar().getId();
        this.carBrand = request.getCar().getBrand();
        this.carModel = request.getCar().getModel();
        this.carYear = request.getCar().getYear();
        this.carColor = request.getCar().getColor();
        this.carDescription = request.getCar().getDescription();
        this.carImageUrls = request.getCar().getImages().stream()
                .map(CarImage::getUrl)
                .toList();
        this.carPricePerDay = request.getCar().getPricePerDay();
        this.carCountry = request.getCar().getLocation().getCountry();
        this.carCity = request.getCar().getLocation().getCity();
        this.carOwnerLastName = request.getCar().getOwner().getLast_name();
        this.carOwnerFirstName = request.getCar().getOwner().getFirst_name();
        this.carOwnerEmail = request.getCar().getOwner().getEmail();
        this.carOwnerPhoneNumber = request.getCar().getOwner().getPhone_number();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.status = request.getStatus();
        this.drivingLicenseUrl = request.getDrivingLicenseUrl();
        this.selfieUrl = request.getSelfieUrl();
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

    public int getCarYear() {
        return carYear;
    }

    public void setCarYear(int carYear) {
        this.carYear = carYear;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    public String getCarDescription() {
        return carDescription;
    }

    public void setCarDescription(String carDescription) {
        this.carDescription = carDescription;
    }

    public List<String> getCarImageUrls() {
        return carImageUrls;
    }

    public void setCarImageUrls(List<String> carImageUrls) {
        this.carImageUrls = carImageUrls;
    }

    public double getCarPricePerDay() {
        return carPricePerDay;
    }

    public void setCarPricePerDay(double carPricePerDay) {
        this.carPricePerDay = carPricePerDay;
    }

    public String getCarCountry() {
        return carCountry;
    }

    public void setCarCountry(String carCountry) {
        this.carCountry = carCountry;
    }

    public String getCarCity() {
        return carCity;
    }

    public void setCarCity(String carCity) {
        this.carCity = carCity;
    }

    public String getCarOwnerLastName() {
        return carOwnerLastName;
    }

    public void setCarOwnerLastName(String carOwnerLastName) {
        this.carOwnerLastName = carOwnerLastName;
    }

    public String getCarOwnerFirstName() {
        return carOwnerFirstName;
    }

    public void setCarOwnerFirstName(String carOwnerFirstName) {
        this.carOwnerFirstName = carOwnerFirstName;
    }

    public String getCarOwnerEmail() {
        return carOwnerEmail;
    }

    public void setCarOwnerEmail(String carOwnerEmail) {
        this.carOwnerEmail = carOwnerEmail;
    }

    public String getCarOwnerPhoneNumber() {
        return carOwnerPhoneNumber;
    }

    public void setCarOwnerPhoneNumber(String carOwnerPhoneNumber) {
        this.carOwnerPhoneNumber = carOwnerPhoneNumber;
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
