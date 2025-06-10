package com.portfolio.food_delivery.common.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {

    private String city;
    private String district;
    private String street;
    private String detail;
    private String zipCode;

    public String getFullAddress() {
        return String.format("%s %s %s %s", city, district, street, detail);
    }
}