package com.events.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Address {

    @Id
    @GeneratedValue
    private UUID id;

    private String details;

    @ManyToOne
    private City city;

    public Address() {}
    public Address(String details, City city) {
        this.details = details;
        this.city = city;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }
}
