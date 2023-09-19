package com.codurance.katalyst.payment.application.api;

public class PotentialCustomerData {
    private String courseId = "";

    private String email = "";

    private String name = "";

    private String surname = "";

    private String company = "";

    private String dnicif = "";

    private String paymentMethod="";

    private boolean isCompany;
    private String phoneNumber = "";
    private String address = "";
    private String postalCode = "";

    private String city = "";
    private String region = "";

    private String payCometUserId = "";

    private String temporalPayCometToken = "";

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getEmail() {
        return email.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDnicif() {
        return dnicif.toUpperCase();
    }

    public void setDnicif(String dnicif) {
        this.dnicif = dnicif;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setIsCompany(boolean isCompany) {
        this.isCompany = isCompany;
    }

    public boolean getIsCompany() {
        return isCompany;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPayCometUserId() {
        return payCometUserId;
    }

    public void setPayCometUserId(String payCometUserId) {
        this.payCometUserId = payCometUserId;
    }

    public String getTemporalPayCometToken() {
        return temporalPayCometToken;
    }

    public void setTemporalPayCometToken(String temporalPayCometToken) {
        this.temporalPayCometToken = temporalPayCometToken;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
