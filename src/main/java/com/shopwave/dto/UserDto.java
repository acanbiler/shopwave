package com.shopwave.dto;

import com.shopwave.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto extends BaseDto {
    private String firebaseUid;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private User.UserRole role;
} 