package com.codurance.katalyst.payment.application.ports.holded.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class HoldedBillAddress {
    private String address;
    private String postalCode;
    private String city;
    private String province;
    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("country")
    private String country;

    public HoldedBillAddress(String address, String postalCode, String city, String province, String countryCode) {
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.province = province;
        this.countryCode = countryCode;
        //------------------ Strange behaviour of the Holded API.---------------------
        //You cannot to send the countryCode with a Country empty or null, because the Country code will be ignored.
        //You need to include also the name.
        //However, the API will take the country code if it is present and it will ignore the Country.
        //If you send the country, sometimes works if it matchs with the name in holded, however
        //if the name for example have ",", it is not been detected.
        //The best approach is to fill both fields, in our case with the Standard ISO CODE
        //A cons in this approach is that in spite of in the Holded user interface is going to be shown correctly
        //the country, when you get the country by API you will receive the same name which you used when you created
        //the contact. The best is to ignore the field Country and base on the standard iso country codes.
        //For this reason, we asign to the country the country code, but we should use this field.
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoldedBillAddress that = (HoldedBillAddress) o;
        return Objects.equals(address, that.address) && Objects.equals(postalCode, that.postalCode) && Objects.equals(city, that.city) && Objects.equals(province, that.province) && Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, postalCode, city, province, countryCode);
    }
}
