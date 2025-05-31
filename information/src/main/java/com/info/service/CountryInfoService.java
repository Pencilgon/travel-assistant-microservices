package com.info.service;

import com.info.dto.*;
import com.info.entity.CountryInfo;
import com.info.entity.EmergencyContact;
import com.info.entity.InfoCategory;
import com.info.entity.RecommendedApp;
import com.info.repository.CountryInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CountryInfoService {

    private final CountryInfoRepository countryInfoRepository;

    public CountryInfoService(CountryInfoRepository countryInfoRepository) {
        this.countryInfoRepository = countryInfoRepository;
    }

    @Transactional(readOnly = true)
    public CountryInfoDto getInfo(String countryCode, String lang) {
        CountryInfo countryInfo = countryInfoRepository
                .findByCountryCodeAndLang(countryCode, lang)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Information not found for " + countryCode + " in language " + lang));

        Map<String, CategoryDto> usefulInfo = countryInfo.getCategories().stream()
                .collect(Collectors.toMap(
                        InfoCategory::getCategoryKey,
                        this::toCategoryDto
                ));

        Map<String, String> contactsMap = countryInfo.getEmergencyContacts().stream()
                .collect(Collectors.toMap(
                        EmergencyContact::getType,
                        EmergencyContact::getPhone
                ));

        CountryInfoDto dto = new CountryInfoDto();
        dto.setCountry(countryInfo.getCountryCode());
        dto.setLang(countryInfo.getLang());
        dto.setUsefulInfo(usefulInfo);
        dto.setEmergencyContacts(contactsMap);

        return dto;
    }

    @Transactional
    public void saveInfo(CountryInfoDto request) {
        CountryInfo info = new CountryInfo();
        info.setCountryCode(request.getCountry());
        info.setLang(request.getLang());

        List<InfoCategory> categories = new ArrayList<>();

        for (Map.Entry<String, CategoryDto> entry : request.getUsefulInfo().entrySet()) {
            InfoCategory category = new InfoCategory();
            category.setCategoryKey(entry.getKey());
            category.setText(entry.getValue().getText());
            category.setCountryInfo(info);

            List<RecommendedApp> apps = entry.getValue().getRecommendedApps().stream().map(appDto -> {
                RecommendedApp app = new RecommendedApp();
                app.setName(appDto.getName());
                app.setIconUrl(appDto.getIconUrl());
                app.setAppStoreUrl(appDto.getAppStoreUrl());
                app.setGooglePlayUrl(appDto.getGooglePlayUrl());
                app.setCategory(category);
                return app;
            }).toList();

            category.setRecommendedApps(apps);
            categories.add(category);
        }

        List<EmergencyContact> contacts = request.getEmergencyContacts().entrySet().stream()
                .map(e -> {
                    EmergencyContact ec = new EmergencyContact();
                    ec.setType(e.getKey());
                    ec.setPhone(e.getValue());
                    ec.setCountryInfo(info);
                    return ec;
                }).toList();

        info.setCategories(categories);
        info.setEmergencyContacts(contacts);

        countryInfoRepository.save(info);
    }

    private CategoryDto toCategoryDto(InfoCategory category) {
        List<RecommendedAppDto> appDtos = category.getRecommendedApps().stream()
                .map(this::toAppDto)
                .toList();

        CategoryDto dto = new CategoryDto();
        dto.setText(category.getText());
        dto.setRecommendedApps(appDtos);
        return dto;
    }

    private RecommendedAppDto toAppDto(RecommendedApp app) {
        RecommendedAppDto dto = new RecommendedAppDto();
        dto.setName(app.getName());
        dto.setIconUrl(app.getIconUrl());
        dto.setAppStoreUrl(app.getAppStoreUrl());
        dto.setGooglePlayUrl(app.getGooglePlayUrl());
        return dto;
    }
}
