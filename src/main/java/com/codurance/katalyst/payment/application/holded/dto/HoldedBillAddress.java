package com.codurance.katalyst.payment.application.holded.dto;

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
}
