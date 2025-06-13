package com.events.controller;

import com.events.dto.EventDto;
import com.events.entity.*;
import com.events.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired private EventRepository eventRepository;
    @Autowired private CountryRepository countryRepo;
    @Autowired private CityRepository cityRepo;
    @Autowired private AddressRepository addressRepo;

    @GetMapping("/locations/countries")
    public ResponseEntity<List<String>> getAllCountries() {
        List<String> countries = countryRepo.findAll()
                .stream()
                .map(Country::getName)
                .distinct()
                .toList();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/locations/cities")
    public ResponseEntity<List<String>> getCitiesByCountry(@RequestParam String country) {
        return countryRepo.findByName(country)
                .map(c -> cityRepo.findByCountry(c)
                        .stream()
                        .map(City::getName)
                        .distinct()
                        .toList())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(List.of()));
    }

    @GetMapping
    public List<Event> searchEvents(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return eventRepository.findAll().stream()
                .filter(e -> country == null || e.getAddress().getCity().getCountry().getName().equalsIgnoreCase(country))
                .filter(e -> city == null || e.getAddress().getCity().getName().equalsIgnoreCase(city))
                .filter(e -> minPrice == null || e.getPrice() >= minPrice)
                .filter(e -> maxPrice == null || e.getPrice() <= maxPrice)
                .filter(e -> startDate == null || !e.getDateTime().isBefore(startDate))
                .filter(e -> endDate == null || !e.getDateTime().isAfter(endDate))
                .toList();
    }

    @PostMapping("/add")
    public ResponseEntity<String> createEvents(@RequestBody List<EventDto> dtoList) {
        List<Event> createdEvents = dtoList.stream().map(dto -> {
            // Step 1: Get or create Country
            Country country = countryRepo.findByName(dto.getCountry())
                    .orElseGet(() -> countryRepo.save(new Country(dto.getCountry())));

            // Step 2: Get or create City
            City city = cityRepo.findByNameAndCountry(dto.getCity(), country)
                    .orElseGet(() -> cityRepo.save(new City(dto.getCity(), country)));

            // Step 3: Get or create Address
            Address address = addressRepo.findByDetailsAndCity(dto.getAddress(), city)
                    .orElseGet(() -> addressRepo.save(new Address(dto.getAddress(), city)));

            // Step 4: Create and save Event
            Event event = new Event();
            event.setTitle(dto.getTitle());
            event.setImage(dto.getImage());
            event.setPrice(dto.getPrice());
            event.setCurrency(dto.getCurrency());
            event.setDateTime(dto.getDate_time());
            event.setLink(dto.getLink());
            event.setAddress(address);

            return eventRepository.save(event);
        }).toList();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdEvents.size() + " events have been successfully added!");
    }
}
