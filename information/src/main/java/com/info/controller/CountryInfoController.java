package com.info.controller;

import com.info.dto.CountryInfoDto;
import com.info.service.CountryInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/information")
public class CountryInfoController {

    private final CountryInfoService countryInfoService;

    public CountryInfoController(CountryInfoService countryInfoService) {
        this.countryInfoService = countryInfoService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<CountryInfoDto> getInfo(
            @RequestParam String country,
            @RequestParam(required = false) String lang,
            @RequestHeader(value = "X-User-Language", required = false) String headerLang
    ) {
        String effectiveLang = (lang != null && !lang.isBlank()) ? lang
                : (headerLang != null && !headerLang.isBlank()) ? headerLang : "en";

        CountryInfoDto response = countryInfoService.getInfo(country, effectiveLang);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/add")
    public ResponseEntity<String> saveInfo(@RequestBody CountryInfoDto request) {
        countryInfoService.saveInfo(request);
        return ResponseEntity.ok("Saved successfully");
    }
}
