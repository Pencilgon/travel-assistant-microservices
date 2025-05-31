package com.info.entity;
import jakarta.persistence.*;
@Entity
public class RecommendedApp {
    @Id @GeneratedValue
    private Long id;

    private String name;
    private String iconUrl;
    private String appStoreUrl;
    private String googlePlayUrl;

    @ManyToOne
    private InfoCategory category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getAppStoreUrl() {
        return appStoreUrl;
    }

    public void setAppStoreUrl(String appStoreUrl) {
        this.appStoreUrl = appStoreUrl;
    }

    public String getGooglePlayUrl() {
        return googlePlayUrl;
    }

    public void setGooglePlayUrl(String googlePlayUrl) {
        this.googlePlayUrl = googlePlayUrl;
    }

    public InfoCategory getCategory() {
        return category;
    }

    public void setCategory(InfoCategory category) {
        this.category = category;
    }
}
