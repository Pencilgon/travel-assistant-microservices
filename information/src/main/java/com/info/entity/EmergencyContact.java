package com.info.entity;

import jakarta.persistence.*;
@Entity
public class EmergencyContact {
    @Id @GeneratedValue
    private Long id;

    private String type;   // police, ambulance, etc.
    private String phone;

    @ManyToOne
    private CountryInfo countryInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public CountryInfo getCountryInfo() {
        return countryInfo;
    }

    public void setCountryInfo(CountryInfo countryInfo) {
        this.countryInfo = countryInfo;
    }
}
