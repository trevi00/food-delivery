package com.portfolio.food_delivery.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "주소 정보")
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {

    @Schema(description = "시/도", example = "서울시")
    private String city;

    @Schema(description = "구/군", example = "강남구")
    private String district;

    @Schema(description = "도로명", example = "테헤란로")
    private String street;

    @Schema(description = "상세주소", example = "123")
    private String detail;

    @Schema(description = "우편번호", example = "12345")
    private String zipCode;

    public String getFullAddress() {
        return String.format("%s %s %s %s", city, district, street, detail);
    }
}