package com.info.dto;

import java.util.List;

public class CategoryDto {
    private String text;
    private List<RecommendedAppDto> recommendedApps;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<RecommendedAppDto> getRecommendedApps() {
        return recommendedApps;
    }

    public void setRecommendedApps(List<RecommendedAppDto> recommendedApps) {
        this.recommendedApps = recommendedApps;
    }
}
