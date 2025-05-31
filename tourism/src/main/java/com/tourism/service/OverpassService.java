package com.tourism.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class OverpassService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String sendQuery(String query) {
        String url = "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            return client.execute(request, response -> EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to query Overpass API", e);
        }
    }

    private String getAreaIdByCity(String city) {
        String query = String.format("""
            [out:json][timeout:25];
            (
              area["name"="%1$s"]["boundary"="administrative"];
              area["name:en"="%1$s"]["boundary"="administrative"];
              area["name:ru"="%1$s"]["boundary"="administrative"];
              area["name:kz"="%1$s"]["boundary"="administrative"];
            );
            out ids;
        """, city);

        String response = sendQuery(query);

        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.has("elements") && root.get("elements").size() > 0) {
                long id = root.get("elements").get(0).get("id").asLong();
                return String.valueOf(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String fetchByTag(String city, String key, String value) {
        String areaId = getAreaIdByCity(city);
        if (areaId == null) {
            throw new RuntimeException("Could not find area ID for city: " + city);
        }

        String query = String.format("""
            [out:json][timeout:25];
            area(%s)->.searchArea;
            nwr["%s"="%s"](area.searchArea);
            out geom;
        """, areaId, key, value);

        return sendQuery(query);
    }

    public String getAttractions(String city) {
        return fetchByTag(city, "tourism", "attraction");
    }

    public String getMuseums(String city) {
        return fetchByTag(city, "tourism", "museum");
    }

    public String getHotels(String city) {
        return fetchByTag(city, "tourism", "hotel");
    }

    public String getDrinkingWater(String city) {
        return fetchByTag(city, "amenity", "drinking_water");
    }

    public String getRestaurants(String city) {
        return fetchByTag(city, "amenity", "restaurant");
    }

    public String getCafes(String city) {
        return fetchByTag(city, "amenity", "cafe");
    }

    public String getHospitals(String city) {
        return fetchByTag(city, "amenity", "hospital");
    }

    public String getPeaks(String city) {
        return fetchByTag(city, "natural", "peak");
    }

    public String getSupermarkets(String city) {
        return fetchByTag(city, "shop", "supermarket");
    }

    public String getClothingShops(String city) {
        return fetchByTag(city, "shop", "clothes");
    }
}
