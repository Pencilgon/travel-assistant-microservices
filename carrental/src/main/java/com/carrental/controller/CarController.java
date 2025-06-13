package com.carrental.controller;

import com.carrental.client.AuthServiceClient;
import com.carrental.cloud.CloudinaryService;
import com.carrental.dto.*;
import com.carrental.entity.*;
import com.carrental.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/rental")
public class CarController {
    private CarRepository carRepository;
    private RentalRequestRepository rentalRequestRepository;
    private LocationRepository locationRepository;
    private CloudinaryService cloudinaryService;
    private AuthServiceClient authServiceClient;
    private CountryRepository countryRepository;
    private CityRepository cityRepository;

    @Autowired
    public CarController(CarRepository carRepository, RentalRequestRepository rentalRequestRepository, LocationRepository locationRepository, CloudinaryService cloudinaryService, AuthServiceClient authServiceClient, CountryRepository countryRepository, CityRepository cityRepository) {
        this.carRepository = carRepository;
        this.rentalRequestRepository = rentalRequestRepository;
        this.locationRepository = locationRepository;
        this.cloudinaryService = cloudinaryService;
        this.authServiceClient = authServiceClient;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
    }
    @PostMapping(value = "/add/cars", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addCar(@RequestHeader("X-User-Id") UUID ownerId,
                                         @ModelAttribute CarRequest carRequest,
                                         @RequestPart("images") List<MultipartFile> images) throws IOException {

        if (carRepository.countByOwnerId(ownerId) >= 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have reached the limit of 5 cars");
        }
        if (carRequest.brand == null || carRequest.brand.isBlank()) {
            return ResponseEntity.badRequest().body("Brand is required");
        }
        if (carRequest.model == null || carRequest.model.isBlank()) {
            return ResponseEntity.badRequest().body("Model is required");
        }
        if (carRequest.year <= 1900 || carRequest.year > LocalDate.now().getYear()) {
            return ResponseEntity.badRequest().body("Year is invalid");
        }
        if (carRequest.pricePerDay <= 0) {
            return ResponseEntity.badRequest().body("Price per day must be greater than 0");
        }
        if (carRequest.country == null || carRequest.country.isBlank() ||
                carRequest.city == null || carRequest.city.isBlank()) {
            return ResponseEntity.badRequest().body("Country and city are required");
        }
        if (carRequest.latitude == null || carRequest.latitude < -90 || carRequest.latitude > 90) {
            return ResponseEntity.badRequest().body("Latitude must be between -90 and 90");
        }
        if (carRequest.longitude == null || carRequest.longitude < -180 || carRequest.longitude > 180) {
            return ResponseEntity.badRequest().body("Longitude must be between -180 and 180");
        }
        if (images == null || images.isEmpty()) {
            return ResponseEntity.badRequest().body("At least one image is required");
        }

        // 1. Find or create Country
        Country country = countryRepository.findByNameIgnoreCase(carRequest.country)
                .orElseGet(() -> {
                    Country c = new Country();
                    c.setName(carRequest.country);
                    return countryRepository.save(c);
                });

        // 2. Find or create City
        City city = cityRepository.findByNameIgnoreCaseAndCountry(carRequest.city, country)
                .orElseGet(() -> {
                    City c = new City();
                    c.setName(carRequest.city);
                    c.setCountry(country);
                    return cityRepository.save(c);
                });

        // 3. Find or create Location
        Location location = locationRepository.findByCityAndLatitudeAndLongitude(city, carRequest.latitude, carRequest.longitude)
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setCity(city);
                    loc.setLatitude(carRequest.latitude);
                    loc.setLongitude(carRequest.longitude);
                    return locationRepository.save(loc);
                });

        Car car = new Car();
        car.setBrand(carRequest.brand);
        car.setModel(carRequest.model);
        car.setYear(carRequest.year);
        car.setColor(carRequest.color);
        car.setDescription(carRequest.description);
        car.setPricePerDay(carRequest.pricePerDay);
        car.setLocation(location);
        car.setOwnerId(ownerId);

        // загрузка фото в Cloudinary и сохранение ссылок
        List<CarImage> carImages = new ArrayList<>();
        for (MultipartFile file : images) {
            String url = cloudinaryService.uploadImage(file);
            CarImage image = new CarImage();
            image.setUrl(url);
            image.setCar(car);
            carImages.add(image);
        }
        car.setImages(carImages);

        carRepository.save(car);

        return ResponseEntity.ok("Your car was added successfully!");
    }

    @GetMapping("/get/cars")
    public ResponseEntity<List<CarResponseDto>> searchCars(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model
    ) {
        List<Car> cars = carRepository.findAll().stream()
                .filter(car -> country == null || car.getLocation().getCity().getCountry().getName().equalsIgnoreCase(country))
                .filter(car -> city == null || car.getLocation().getCity().getName().equalsIgnoreCase(city))
                .filter(car -> minPrice == null || car.getPricePerDay() >= minPrice)
                .filter(car -> maxPrice == null || car.getPricePerDay() <= maxPrice)
                .filter(car -> available == null || car.isAvailable() == available)
                .filter(car -> year == null || car.getYear() == year)
                .filter(car -> brand == null || car.getBrand().equalsIgnoreCase(brand))
                .filter(car -> model == null || car.getModel().equalsIgnoreCase(model))
                .toList();

        List<CarResponseDto> response = cars.stream()
                .map(car -> {
                    UserProfileDto ownerProfile = authServiceClient.getUserProfile(car.getOwnerId(), token);
                    return new CarResponseDto(car, ownerProfile);
                })
                .toList();

        return ResponseEntity.ok(response);
    }
    @GetMapping("/owner/cars")
    public ResponseEntity<List<CarResponseDto>> getCarsByUserId(@RequestHeader("X-User-Id") UUID ownerId,
                                                                @RequestHeader("Authorization") String token) {
        List<Car> cars = carRepository.findByOwnerId(ownerId);

        UserProfileDto ownerProfile = authServiceClient.getUserProfile(ownerId, token);
        List<CarResponseDto> dtos = cars.stream()
                .map(car -> new CarResponseDto(car, ownerProfile))
                .toList();

        return ResponseEntity.ok(dtos);
    }
    @DeleteMapping("/delete/car/{carId}")
    public ResponseEntity<String> deleteCar(
            @RequestHeader("X-User-Id") UUID ownerId,
            @PathVariable UUID carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        if (!car.getOwnerId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your car");
        }
        carRepository.delete(car);
        return ResponseEntity.ok("Car deleted successfully");
    }
    @PatchMapping(value = "/update/car/{carId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> patchCar(
            @RequestHeader("X-User-Id") UUID ownerId,
            @PathVariable UUID carId,
            @ModelAttribute CarRequest carRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        if (!car.getOwnerId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your car");
        }

        if (carRequest.brand != null && !carRequest.brand.isBlank()) {
            car.setBrand(carRequest.brand);
        }
        if (carRequest.model != null && !carRequest.model.isBlank()) {
            car.setModel(carRequest.model);
        }
        if (carRequest.year != 0) {
            if (carRequest.year > 1900 && carRequest.year <= LocalDate.now().getYear()) {
                car.setYear(carRequest.year);
            } else {
                return ResponseEntity.badRequest().body("Year is invalid");
            }
        }
        if (carRequest.color != null) {
            car.setColor(carRequest.color);
        }
        if (carRequest.description != null) {
            car.setDescription(carRequest.description);
        }
        if (carRequest.pricePerDay > 0) {
            car.setPricePerDay(carRequest.pricePerDay);
        }
        if (carRequest.country != null && carRequest.city != null) {
            if (carRequest.latitude == null || carRequest.latitude < -90 || carRequest.latitude > 90) {
                return ResponseEntity.badRequest().body("Latitude must be between -90 and 90");
            }
            if (carRequest.longitude == null || carRequest.longitude < -180 || carRequest.longitude > 180) {
                return ResponseEntity.badRequest().body("Longitude must be between -180 and 180");
            }

            Country country = countryRepository.findByNameIgnoreCase(carRequest.country)
                    .orElseGet(() -> {
                        Country c = new Country();
                        c.setName(carRequest.country);
                        return countryRepository.save(c);
                    });

            City city = cityRepository.findByNameIgnoreCaseAndCountry(carRequest.city, country)
                    .orElseGet(() -> {
                        City c = new City();
                        c.setName(carRequest.city);
                        c.setCountry(country);
                        return cityRepository.save(c);
                    });

            Location location = locationRepository.findByCityAndLatitudeAndLongitude(city, carRequest.latitude, carRequest.longitude)
                    .orElseGet(() -> {
                        Location loc = new Location();
                        loc.setCity(city);
                        loc.setLatitude(carRequest.latitude);
                        loc.setLongitude(carRequest.longitude);
                        return locationRepository.save(loc);
                    });

            car.setLocation(location);
        }

        if (images != null && !images.isEmpty()) {
            car.getImages().clear();
            for (MultipartFile file : images) {
                String url = cloudinaryService.uploadImage(file);
                CarImage image = new CarImage();
                image.setUrl(url);
                image.setCar(car);
                car.getImages().add(image);
            }
        }

        carRepository.save(car);
        return ResponseEntity.ok("Car updated successfully");
    }






    @PostMapping(value = "/user/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> requestRental(
            @RequestHeader("X-User-Id") UUID userId,
            @ModelAttribute RentalRequestDto dto,
            @RequestPart("drivingLicense") MultipartFile drivingLicense,
            @RequestPart("selfie") MultipartFile selfie
    ) throws IOException {

        updateExpiredRentals(userId);

        long activeRequests = rentalRequestRepository.countByUserIdAndStatusIn(userId, List.of("pending", "approved"));
        if (activeRequests >= 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have reached the limit of 3 active rentals");
        }

        Car car = carRepository.findById(dto.getCarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));

        if (!car.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car is currently not available for rental");
        }

        LocalDate startDate = dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now();
        LocalDate endDate = dto.getEndDate();

        if (endDate == null || !endDate.isAfter(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        // Загрузка файлов в Cloudinary (или на диск)
        String licenseUrl = cloudinaryService.uploadImage(drivingLicense);
        String selfieUrl = cloudinaryService.uploadImage(selfie);

        RentalRequest request = new RentalRequest();
        request.setUserId(userId);
        request.setCar(car);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setStatus("pending");
        request.setDrivingLicenseUrl(licenseUrl);
        request.setSelfieUrl(selfieUrl);

        rentalRequestRepository.save(request);

        return ResponseEntity.ok("Rental request submitted");
    }


    // 🔥 Метод для обновления истёкших аренд пользователя
    private void updateExpiredRentals(UUID userId) {
        List<RentalRequest> userRequests = rentalRequestRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();

        for (RentalRequest r : userRequests) {
            if ((r.getStatus().equals("pending") || r.getStatus().equals("approved")) && r.getEndDate().isBefore(today)) {
                r.setStatus("completed");
                rentalRequestRepository.save(r);

                Car rentedCar = r.getCar();
                rentedCar.setAvailable(true);
                carRepository.save(rentedCar);
            }
        }
    }


    @GetMapping("/my-requests")
    public ResponseEntity<List<RentalRequestUserResponseDto>> getMyRequests(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("Authorization") String token) {
        List<RentalRequest> reqs = rentalRequestRepository.findByUserId(userId);

        List<RentalRequestUserResponseDto> dtos = reqs.stream()
                .map(request -> {
                    // подгружаем профиль владельца машины для каждой заявки
                    UUID ownerId = request.getCar().getOwnerId();
                    UserProfileDto ownerProfile = authServiceClient.getUserProfile(ownerId, token);
                    return new RentalRequestUserResponseDto(request, ownerProfile);
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }
    @GetMapping("/owner-requests")
    public ResponseEntity<List<RentalRequestOwnerResponseDto>> getOwnerRequests(
            @RequestHeader("X-User-Id") UUID ownerId) {
        var reqs = rentalRequestRepository.findByCar_OwnerId(ownerId);
        var dtos = reqs.stream()
                .map(RentalRequestOwnerResponseDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }



    // Владелец одобряет заявку
    @PostMapping("/owner/request/{reqId}/approve")
    public ResponseEntity<String> approveRental(@RequestHeader("X-User-Id") UUID ownerId, @PathVariable UUID reqId) {
        var req = rentalRequestRepository.findById(reqId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!req.getCar().getOwnerId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your request");
        }
        req.setStatus("approved");
        req.getCar().setAvailable(false);
        rentalRequestRepository.save(req);
        return ResponseEntity.ok("Request approved");
    }

    // Отклонить можно так же...
    @PostMapping("/owner/request/{reqId}/reject")
    public ResponseEntity<String> rejectRental(
            @RequestHeader("X-User-Id") UUID ownerId,
            @PathVariable UUID reqId) {
        var req = rentalRequestRepository.findById(reqId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!req.getCar().getOwnerId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your request");
        }
        req.setStatus("rejected");
        rentalRequestRepository.save(req);
        return ResponseEntity.ok("Request rejected");
    }
}