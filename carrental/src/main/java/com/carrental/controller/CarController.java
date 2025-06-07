package com.carrental.controller;

import com.carrental.cloud.CloudinaryService;
import com.carrental.dto.*;
import com.carrental.entity.*;
import com.carrental.repository.*;
import com.carrental.security.JwtGenerator;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.auth0.jwt.JWT;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/rental")
public class CarController {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private CarRepository carRepository;
    private RentalRequestRepository rentalRequestRepository;
    private LocationRepository locationRepository;
    private CloudinaryService cloudinaryService;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtGenerator jwtGenerator;
    @Autowired
    public CarController(UserRepository userRepository, RoleRepository roleRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtGenerator jwtGenerator, CarRepository carRepository, LocationRepository locationRepository, RentalRequestRepository rentalRequestRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
        this.carRepository = carRepository;
        this.locationRepository = locationRepository;
        this.rentalRequestRepository = rentalRequestRepository;
        this.cloudinaryService = cloudinaryService;
    }
    @PostMapping("/auth/register")
    public ResponseEntity<String> register(@RequestBody AuthDto authDto){
        if (authDto.getUsername() == null || authDto.getUsername().trim().isEmpty()) {
            return new ResponseEntity<>("Username cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getPhone_number() == null || !authDto.getPhone_number().matches("\\+?\\d{10,15}")) {
            return new ResponseEntity<>("Invalid phone number format!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getEmail() == null || !authDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return new ResponseEntity<>("Invalid email format!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getFirst_name() == null || authDto.getFirst_name().trim().isEmpty()) {
            return new ResponseEntity<>("First name cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getLast_name() == null || authDto.getLast_name().trim().isEmpty()) {
            return new ResponseEntity<>("Last name cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getDate_of_birth() == null || !authDto.getDate_of_birth().matches("\\d{4}-\\d{2}-\\d{2}")) {
            return new ResponseEntity<>("Invalid date format! Use YYYY-MM-DD.", HttpStatus.BAD_REQUEST);
        }

        LocalDate dob = LocalDate.parse(authDto.getDate_of_birth());
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusYears(100); // Max age limit: 100 years old

        if (dob.isAfter(today)) {
            return new ResponseEntity<>("Date of birth cannot be in the future!", HttpStatus.BAD_REQUEST);
        }
        if (dob.isBefore(minDate)) {
            return new ResponseEntity<>("Date of birth is too far in the past!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getPreferred_language() == null || authDto.getPreferred_language().trim().isEmpty()) {
            return new ResponseEntity<>("Preferred language cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getPassword() == null || authDto.getPassword().length() < 6) {
            return new ResponseEntity<>("Password must be at least 6 characters long!", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByUsername(authDto.getUsername())){
            return new ResponseEntity<>("User with username: "+authDto.getUsername()+" already exists!!!", HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        user.setUsername(authDto.getUsername());
        user.setPhone_number(authDto.getPhone_number());
        user.setEmail(authDto.getEmail());
        user.setFirst_name(authDto.getFirst_name());
        user.setLast_name(authDto.getLast_name());
        user.setDate_of_birth(authDto.getDate_of_birth());
        user.setPreferred_language(authDto.getPreferred_language());
        user.setPassword(passwordEncoder.encode(authDto.getPassword()));
        Role role = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(role));
        userRepository.save(user);
        return new ResponseEntity<>("User has been registered success!!!", HttpStatus.OK);
    }
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new RuntimeException("User is not found!"));
        return new ResponseEntity<>(new AuthResponseDto(user.getId(), token), HttpStatus.OK);
    }
    @GetMapping("/user/{id}")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> new ResponseEntity<>(new UserProfileDto(user), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @PatchMapping("/user/{id}/update")
    public ResponseEntity<String> updateUser(@PathVariable UUID id, @RequestBody UpdateUserDto updateUserDto) {
        return userRepository.findById(id)
                .map(user -> {
                    List<String> errors = new ArrayList<>();

                    // Validate Phone Number
                    if (updateUserDto.getPhone_number() != null) {
                        if (!updateUserDto.getPhone_number().matches("\\+?\\d{10,15}")) {
                            errors.add("Invalid phone number format! It should be 10-15 digits long.");
                        } else {
                            user.setPhone_number(updateUserDto.getPhone_number());
                        }
                    }

                    // Validate Email
                    if (updateUserDto.getEmail() != null) {
                        if (!updateUserDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                            errors.add("Invalid email format!");
                        } else {
                            user.setEmail(updateUserDto.getEmail());
                        }
                    }

                    // Validate Preferred Language
                    if (updateUserDto.getPreferred_language() != null) {
                        if (updateUserDto.getPreferred_language().trim().isEmpty()) {
                            errors.add("Preferred language cannot be empty!");
                        } else {
                            user.setPreferred_language(updateUserDto.getPreferred_language());
                        }
                    }

                    // Validate First Name
                    if (updateUserDto.getFirst_name() != null) {
                        if (updateUserDto.getFirst_name().trim().isEmpty()) {
                            errors.add("First name cannot be empty!");
                        } else {
                            user.setFirst_name(updateUserDto.getFirst_name());
                        }
                    }

                    // Validate Last Name
                    if (updateUserDto.getLast_name() != null) {
                        if (updateUserDto.getLast_name().trim().isEmpty()) {
                            errors.add("Last name cannot be empty!");
                        } else {
                            user.setLast_name(updateUserDto.getLast_name());
                        }
                    }

                    if (updateUserDto.getDate_of_birth() != null) {
                        if (!updateUserDto.getDate_of_birth().matches("\\d{4}-\\d{2}-\\d{2}")) {
                            errors.add("Invalid date format! Use YYYY-MM-DD.");
                        } else {
                            LocalDate dob = LocalDate.parse(updateUserDto.getDate_of_birth());
                            LocalDate today = LocalDate.now();
                            LocalDate minDate = today.minusYears(100); // At most 100 years old

                            if (dob.isAfter(today)) {
                                errors.add("Date of birth cannot be in the future!");
                            } else if (dob.isBefore(minDate)) {
                                errors.add("Date of birth is too far in the past!");
                            } else {
                                user.setDate_of_birth(updateUserDto.getDate_of_birth());
                            }
                        }
                    }

                    // If there are validation errors, return them instead of updating the user
                    if (!errors.isEmpty()) {
                        return new ResponseEntity<>(String.join("\n", errors), HttpStatus.BAD_REQUEST);
                    }

                    userRepository.save(user);
                    return new ResponseEntity<>("Profile updated successfully!", HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND));
    }

    @PutMapping("/user/{id}/update/password")
    public ResponseEntity<String> updatePassword(@PathVariable UUID id, @RequestBody UpdatePasswordDto updatePasswordDto) {
        return userRepository.findById(id)
                .map(user -> {
                    // Validate old password
                    if (!passwordEncoder.matches(updatePasswordDto.getOldPassword(), user.getPassword())) {
                        return new ResponseEntity<>("Old password is incorrect", HttpStatus.BAD_REQUEST);
                    }

                    // Validate new password length
                    if (updatePasswordDto.getNewPassword().length() < 6) {
                        return new ResponseEntity<>("New password must be at least 6 characters long", HttpStatus.BAD_REQUEST);
                    }

                    // Update password
                    user.setPassword(passwordEncoder.encode(updatePasswordDto.getNewPassword()));
                    userRepository.save(user);
                    return new ResponseEntity<>("Password updated successfully!", HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND));
    }
    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.audience}")
    private String auth0Audience;

    @PostMapping("/social-login")
    public ResponseEntity<?> socialLogin(@RequestBody SocialLoginDto dto) {
        String idToken = dto.getId_token();
        if (idToken == null || idToken.isEmpty()) {
            return ResponseEntity.badRequest().body("id_token is required");
        }

        try {
            // 1. Проверка подписи токена через Auth0
            URL jwksUrl = new URL("https://" + auth0Domain + "/.well-known/jwks.json");
            JwkProvider provider = new UrlJwkProvider(jwksUrl);
            DecodedJWT decoded = JWT.decode(idToken);
            Jwk jwk = provider.get(decoded.getKeyId());

            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withAudience(auth0Audience)
                    .withIssuer("https://" + auth0Domain + "/")
                    .build();

            DecodedJWT jwt = verifier.verify(idToken);

            // 2. Извлечение данных
            String email = jwt.getClaim("email").asString();
            String sub = jwt.getSubject();
            String authMethod = sub.startsWith("google") ? "google"
                    : sub.startsWith("apple") ? "apple"
                    : "auth0";

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("Email not found in token");
            }

            // 3. Найти или создать пользователя
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername(email.split("@")[0] + UUID.randomUUID().toString().substring(0, 5));
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setFirst_name("");
                user.setLast_name("");
                user.setPreferred_language("en");
                user.setDate_of_birth("2000-01-01");
                user.setPhone_number("");

                Role role = roleRepository.findByName("USER").orElseThrow();
                user.setRoles(Collections.singletonList(role));
                userRepository.save(user);
            }

            // 4. Сформировать список прав (authorities)
            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 5. Сгенерировать JWT
            String token = jwtGenerator.generateToken(authToken);

            return ResponseEntity.ok(new AuthResponseDto(user.getId(), token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    // POST /rental/add/cars — добавить машину
    @PostMapping(value = "/user/{id}/add/cars", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addCar(@PathVariable UUID id,
                                         @ModelAttribute CarRequest carRequest,
                                         @RequestPart("images") List<MultipartFile> images) throws IOException {

        if (carRepository.countByOwnerId(id) >= 5) {
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

        Location location = locationRepository.findByCountryAndCity(carRequest.country, carRequest.city)
                .orElseGet(() -> {
                    Location newLocation = new Location();
                    newLocation.setCountry(carRequest.country);
                    newLocation.setCity(carRequest.city);
                    newLocation.setLatitude(carRequest.latitude);
                    newLocation.setLongitude(carRequest.longitude);
                    return locationRepository.save(newLocation);
                });

        Car car = new Car();
        car.setBrand(carRequest.brand);
        car.setModel(carRequest.model);
        car.setYear(carRequest.year);
        car.setColor(carRequest.color);
        car.setDescription(carRequest.description);
        car.setPricePerDay(carRequest.pricePerDay);
        car.setLocation(location);

        User owner = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        car.setOwner(owner);

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

    // GET /rental/getall/cars — все машины
    @GetMapping("/getall/cars")
    public ResponseEntity<List<CarResponseDto>> getAllCars() {
        List<Car> cars = carRepository.findAll();
        List<CarResponseDto> response = cars.stream()
                .map(CarResponseDto::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    // GET /rental/get/cars?country=Kazakhstan&city=Almaty — по локации
    @GetMapping("/get/cars")
    public ResponseEntity<List<CarResponseDto>> getCarsByLocation(@RequestParam String country, @RequestParam String city) {
        List<Car> cars = carRepository.findByLocation_CountryAndLocation_City(country, city);
        List<CarResponseDto> response = cars.stream()
                .map(CarResponseDto::new)
                .toList();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/locations/countries")
    public ResponseEntity<List<String>> getAllCountries() {
        List<String> countries = locationRepository.findAll()
                .stream()
                .map(Location::getCountry)
                .distinct()
                .toList();
        return ResponseEntity.ok(countries);
    }
    @GetMapping("/locations/cities")
    public ResponseEntity<List<String>> getCitiesByCountry(@RequestParam String country) {
        List<String> cities = locationRepository.findByCountry(country)
                .stream()
                .map(Location::getCity)
                .distinct()
                .toList();
        return ResponseEntity.ok(cities);
    }
    @GetMapping("/user/{id}/cars")
    public ResponseEntity<List<CarResponseDto>> getCarsByUserId(@PathVariable UUID id) {
        List<Car> cars = carRepository.findByOwner_Id(id);
        List<CarResponseDto> response = cars.stream()
                .map(CarResponseDto::new)
                .toList();
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/user/{userId}/delete/car/{carId}")
    public ResponseEntity<String> deleteCar(@PathVariable UUID userId, @PathVariable UUID carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));

        if (!car.getOwner().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this car");
        }

        carRepository.delete(car);
        return ResponseEntity.ok("Car deleted successfully");
    }
    @PatchMapping(value = "/user/{userId}/update/car/{carId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> patchCar(@PathVariable UUID userId,
                                           @PathVariable UUID carId,
                                           @ModelAttribute CarRequest carRequest,
                                           @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));

        if (!car.getOwner().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to edit this car");
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

            Location location = locationRepository.findByCountryAndCity(carRequest.country, carRequest.city)
                    .orElseGet(() -> {
                        Location newLocation = new Location();
                        newLocation.setCountry(carRequest.country);
                        newLocation.setCity(carRequest.city);
                        newLocation.setLatitude(carRequest.latitude);
                        newLocation.setLongitude(carRequest.longitude);
                        return locationRepository.save(newLocation);
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






    @PostMapping(value = "/request/user/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> requestRental(
            @PathVariable UUID id,
            @ModelAttribute RentalRequestDto dto,
            @RequestPart("drivingLicense") MultipartFile drivingLicense,
            @RequestPart("selfie") MultipartFile selfie
    ) throws IOException {

        updateExpiredRentals(id);

        long activeRequests = rentalRequestRepository.countByUserIdAndStatusIn(id, List.of("pending", "approved"));
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
        request.setUserId(id);
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


    @GetMapping("/my-requests/{userId}")
    public ResponseEntity<List<RentalRequestUserResponseDto>> getMyRequestsByUserId(@PathVariable UUID userId) {
        List<RentalRequest> requests = rentalRequestRepository.findByUserId(userId);
        List<RentalRequestUserResponseDto> response = requests.stream()
                .map(RentalRequestUserResponseDto::new)
                .toList();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/owner-requests/{ownerId}")
    public ResponseEntity<List<RentalRequestOwnerResponseDto>> getOwnerRequestsByOwnerId(@PathVariable UUID ownerId) {
        List<RentalRequest> requests = rentalRequestRepository.findByCar_Owner_Id(ownerId);
        List<RentalRequestOwnerResponseDto> response = requests.stream()
                .map(RentalRequestOwnerResponseDto::new)
                .toList();
        return ResponseEntity.ok(response);
    }



    // Владелец одобряет заявку
    @PostMapping("/user/{ownerId}/approve/{id}")
    public ResponseEntity<String> approveRental(@PathVariable UUID id, @PathVariable UUID ownerId) {
        RentalRequest request = rentalRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!request.getCar().getOwner().getId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not own this car");
        }

        request.setStatus("approved");
        request.getCar().setAvailable(false); // 🚗 Машина теперь занята
        rentalRequestRepository.save(request);
        carRepository.save(request.getCar());
        return ResponseEntity.ok("Rental request approved");
    }

    // Отклонить можно так же...
    @PostMapping("/user/{ownerId}/reject/{id}")
    public ResponseEntity<String> rejectRental(@PathVariable UUID id, @PathVariable UUID ownerId) {
        RentalRequest request = rentalRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!request.getCar().getOwner().getId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not own this car");
        }

        request.setStatus("rejected");
        rentalRequestRepository.save(request);
        return ResponseEntity.ok("Rental request rejected");
    }
}