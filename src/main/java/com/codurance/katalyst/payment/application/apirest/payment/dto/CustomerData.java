package com.codurance.katalyst.payment.application.apirest.payment.dto;

public class CustomerData {
    private String courseId = "";

    private String email = "";

    private String name = "";

    private String surname = "";

    private String company = "";

    private String dnicif = "";

    private boolean isCompany;
    private String phoneNumber = "";
    private String address = "";
    private String postalCode = "";

    private String city = "";
    private String region = "";

    private String country = "";

    private String amount="";
    private String username="";
    private String paytpvToken="";
    private String ip;

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPaytpvToken() {
        return paytpvToken;
    }

    public void setPaytpvToken(String paytpvToken) {
        this.paytpvToken = paytpvToken;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }
}
