package com.codurance.katalyst.payment.application.model.purchase;

public class Purchase {
    private String courseId;
    private String order;
    private String email;
    private String name;
    private String surname;
    private String nifCif;
    private boolean isCompany;
    private String city;
    private String region;
    private String country;
    private boolean finantialState;
    private boolean learningState;
    private int transactionId;

    public Purchase(int transactionId, String order, String courseId, String email, String name, String surname, String nifCif, boolean isCompany, String company, String address, String postalCode, String city, String region, String country, boolean finantialStep, boolean learningStep) {
        this.transactionId = transactionId;
        this.order = order;
        this.courseId = courseId;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.nifCif = nifCif;
        this.isCompany = isCompany;
        this.city = city;
        this.region = region;
        this.country = country;
        this.finantialState = finantialStep;
        this.learningState = learningStep;
    }

    public Purchase(int transactionId, String order) {
        this.transactionId = transactionId;
        this.order = order;
    }

    public String getOrder() {
        return order;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getNifCif() {
        return nifCif;
    }

    public boolean isCompany() {
        return isCompany;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }

    public boolean isFinantialState() {
        return finantialState;
    }

    public boolean isLearningState() {
        return learningState;
    }

    public int getTransactionId() {
        return transactionId;
    }
}
