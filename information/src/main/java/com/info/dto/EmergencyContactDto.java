package com.info.dto;

import java.util.Map;

public class EmergencyContactDto {
    private Map<String, String> contacts;

    public Map<String, String> getContacts() {
        return contacts;
    }

    public void setContacts(Map<String, String> contacts) {
        this.contacts = contacts;
    }
}