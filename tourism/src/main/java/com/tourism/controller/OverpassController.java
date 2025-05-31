package com.tourism.controller;

import com.tourism.service.OverpassService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tourism")
public class OverpassController {

    private final OverpassService service;

    public OverpassController(OverpassService service) {
        this.service = service;
    }

    @GetMapping("/attractions")
    public ResponseEntity<String> attractions(@RequestParam String city) {
        return ResponseEntity.ok(service.getAttractions(city));
    }

    @GetMapping("/museums")
    public ResponseEntity<String> museums(@RequestParam String city) {
        return ResponseEntity.ok(service.getMuseums(city));
    }

    @GetMapping("/hotels")
    public ResponseEntity<String> hotels(@RequestParam String city) {
        return ResponseEntity.ok(service.getHotels(city));
    }

    @GetMapping("/drinking-water")
    public ResponseEntity<String> drinkingWater(@RequestParam String city) {
        return ResponseEntity.ok(service.getDrinkingWater(city));
    }

    @GetMapping("/restaurants")
    public ResponseEntity<String> restaurants(@RequestParam String city) {
        return ResponseEntity.ok(service.getRestaurants(city));
    }

    @GetMapping("/cafes")
    public ResponseEntity<String> cafes(@RequestParam String city) {
        return ResponseEntity.ok(service.getCafes(city));
    }

    @GetMapping("/hospitals")
    public ResponseEntity<String> hospitals(@RequestParam String city) {
        return ResponseEntity.ok(service.getHospitals(city));
    }

    @GetMapping("/peaks")
    public ResponseEntity<String> peaks(@RequestParam String city) {
        return ResponseEntity.ok(service.getPeaks(city));
    }

    @GetMapping("/supermarkets")
    public ResponseEntity<String> supermarkets(@RequestParam String city) {
        return ResponseEntity.ok(service.getSupermarkets(city));
    }

    @GetMapping("/clothing-shops")
    public ResponseEntity<String> clothingShops(@RequestParam String city) {
        return ResponseEntity.ok(service.getClothingShops(city));
    }
}
