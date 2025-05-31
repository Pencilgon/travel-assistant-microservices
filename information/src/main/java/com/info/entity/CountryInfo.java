package com.info.entity;

import jakarta.persistence.*;

import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
public class CountryInfo {
    @Id @GeneratedValue
    private Long id;

    private String countryCode;  // "Germany"
    private String lang;         // "ru"

    @OneToMany(mappedBy = "countryInfo", cascade = ALL)
    private List<InfoCategory> categories;

    @OneToMany(mappedBy = "countryInfo", cascade = ALL)
    private List<EmergencyContact> emergencyContacts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public List<InfoCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<InfoCategory> categories) {
        this.categories = categories;
    }

    public List<EmergencyContact> getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(List<EmergencyContact> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }
}
