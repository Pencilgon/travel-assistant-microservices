package com.events.controller;

import com.events.dto.EventDto;
import com.events.entity.Event;
import com.events.entity.Location;
import com.events.repository.EventRepository;
import com.events.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private LocationRepository locationRepository;

    @GetMapping
    public List<Event> searchEvents(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return eventRepository.findAll().stream()
                .filter(e -> country == null || e.getLocation().getCountry().equalsIgnoreCase(country))
                .filter(e -> city == null || e.getLocation().getCity().equalsIgnoreCase(city))
                .filter(e -> minPrice == null || e.getPrice() >= minPrice)
                .filter(e -> maxPrice == null || e.getPrice() <= maxPrice)
                .filter(e -> startDate == null || !e.getDateTime().isBefore(startDate))
                .filter(e -> endDate == null || !e.getDateTime().isAfter(endDate))
                .toList();
    }

    @PostMapping("/add")
    public ResponseEntity<String> createEvents(@RequestBody List<EventDto> dtoList) {
        List<Event> createdEvents = dtoList.stream().map(dto -> {
            Location location = locationRepository
                    .findByCountryAndCityAndAddress(dto.getCountry(), dto.getCity(), dto.getAddress())
                    .orElseGet(() -> locationRepository.save(
                            new Location(dto.getCountry(), dto.getCity(), dto.getAddress())
                    ));

            // Создание и сохранение события
            Event event = new Event();
            event.setTitle(dto.getTitle());
            event.setImage(dto.getImage());
            event.setPrice(dto.getPrice());
            event.setCurrency(dto.getCurrency());
            event.setDateTime(dto.getDate_time());
            event.setLink(dto.getLink());
            event.setLocation(location);

            return eventRepository.save(event);
        }).toList();

        String message = createdEvents.size() + " events have been successfully added!";
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}
