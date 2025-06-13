package com.carrental.dto;

import com.carrental.entity.Car;
import com.carrental.entity.CarImage;

import java.util.List;
import java.util.UUID;

public class CarResponseDto {

    private UUID id;
    private String brand;
    private String model;
    private int year;
    private String color;
    private String description;
    private List<String> imageUrls;
    private double pricePerDay;
    private String country;
    private String city;
    public Double latitude;
    public Double longitude;
    private String ownerLastName;
    private String ownerFirstName;
    private String ownerEmail;
    private String ownerPhoneNumber;
    private boolean available;

    // Конструктор для быстрого маппинга
    public CarResponseDto(Car car, UserProfileDto ownerInfo) {
        this.id = car.getId();
        this.brand = car.getBrand();
        this.model = car.getModel();
        this.year = car.getYear();
        this.color = car.getColor();
        this.description = car.getDescription();
        this.imageUrls = car.getImages().stream()
                .map(CarImage::getUrl)
                .toList();
        this.pricePerDay = car.getPricePerDay();

        this.city = car.getLocation().getCity().getName();
        this.country = car.getLocation().getCity().getCountry().getName();

        this.latitude = car.getLocation().getLatitude();
        this.longitude = car.getLocation().getLongitude();

        this.ownerFirstName   = ownerInfo.getFirstName();
        this.ownerLastName    = ownerInfo.getLastName();
        this.ownerEmail       = ownerInfo.getEmail();
        this.ownerPhoneNumber = ownerInfo.getPhoneNumber();
        this.available        = car.isAvailable();
    }

    // геттеры и сеттеры если нужны

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getOwnerLastName() {
        return ownerLastName;
    }

    public void setOwnerLastName(String ownerLastName) {
        this.ownerLastName = ownerLastName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerPhoneNumber() {
        return ownerPhoneNumber;
    }

    public void setOwnerPhoneNumber(String ownerPhoneNumber) {
        this.ownerPhoneNumber = ownerPhoneNumber;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    public void setOwnerFirstName(String ownerFirstName) {
        this.ownerFirstName = ownerFirstName;
    }
}
