package com.info.repository;

import com.info.entity.CountryInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryInfoRepository extends JpaRepository<CountryInfo, Long> {
    Optional<CountryInfo> findByCountryCodeAndLang(String countryCode, String lang);
}
