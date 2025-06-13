package com.auth.dto;

import com.auth.entity.User;

public class UserDataDto {
    private String FirstName;
    private String LastName;
    private String Email;
    private String PhoneNumber;

    public UserDataDto(String firstName, String lastName, String email, String phoneNumber) {
        FirstName = firstName;
        LastName = lastName;
        Email = email;
        PhoneNumber = phoneNumber;
    }

    public UserDataDto(User value) {
        this.FirstName = value.getFirst_name();
        this.LastName = value.getLast_name();
        this.Email = value.getEmail();
        this.PhoneNumber = value.getPhone_number();
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }
}
