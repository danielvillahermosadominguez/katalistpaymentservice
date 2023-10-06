package com.codurance.katalyst.payment.application.ports.Holded.dto;

import java.util.Objects;

public class HoldedBillAddress {
    private String address;
    private String postalCode;
    private String city;
    private String province;
    private String country;

    public HoldedBillAddress(String address, String postalCode, String city, String province, String countryCode) {
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.province = province;
        this.country = countryCode;
    }

    public String getAddress() {
        return address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoldedBillAddress that = (HoldedBillAddress) o;
        return Objects.equals(address, that.address) && Objects.equals(postalCode, that.postalCode) && Objects.equals(city, that.city) && Objects.equals(province, that.province) && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, postalCode, city, province, country);
    }
}
