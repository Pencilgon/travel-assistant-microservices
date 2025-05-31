package com.info.dto;

import java.util.Map;

public class CountryInfoDto {
    private String country;
    private String lang;
    private Map<String, CategoryDto> usefulInfo;
    private Map<String, String> emergencyContacts;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Map<String, CategoryDto> getUsefulInfo() {
        return usefulInfo;
    }

    public void setUsefulInfo(Map<String, CategoryDto> usefulInfo) {
        this.usefulInfo = usefulInfo;
    }

    public Map<String, String> getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(Map<String, String> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }
}
