package com.info.entity;

import jakarta.persistence.*;

import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
public class InfoCategory {
    @Id @GeneratedValue
    private Long id;

    private String categoryKey; // visa, transport, etc.
    @Column(columnDefinition = "TEXT")
    private String text;

    @ManyToOne
    private CountryInfo countryInfo;

    @OneToMany(mappedBy = "category", cascade = ALL)
    private List<RecommendedApp> recommendedApps;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public CountryInfo getCountryInfo() {
        return countryInfo;
    }

    public void setCountryInfo(CountryInfo countryInfo) {
        this.countryInfo = countryInfo;
    }

    public List<RecommendedApp> getRecommendedApps() {
        return recommendedApps;
    }

    public void setRecommendedApps(List<RecommendedApp> recommendedApps) {
        this.recommendedApps = recommendedApps;
    }
}
