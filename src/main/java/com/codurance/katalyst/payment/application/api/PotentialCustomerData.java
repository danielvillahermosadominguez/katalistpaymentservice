package com.codurance.katalyst.payment.application.api;

public class PotentialCustomerData {
    private String courseId;

    private String email;

    private String name;

    private String surname;

    private String company;

    private String dnicif;

    private String paymentMethod;

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
}
