package com.codurance.katalyst.payment.application.model.purchase;

public class Purchase {
    private double price;
    private String description;
    private String concept;
    private  String phone;
    private String postalCode;
    private String address;
    private String courseId;
    private String order;
    private String email;
    private String name;
    private String surname;
    private String nifCif;
    private String company;
    private boolean isCompany;
    private String city;
    private String region;
    private String country;
    private boolean finantialState;
    private boolean learningState;
    private int transactionId;
    private int id;

    public Purchase(int transactionId,
                    String order,
                    String courseId,
                    String concept,
                    String description,
                    double price,
                    String email,
                    String name,
                    String surname,
                    String nifCif,
                    boolean isCompany,
                    String company,
                    String phone,
                    String address,
                    String postalCode,
                    String city,
                    String region,
                    String country,
                    boolean finantialStep,
                    boolean learningStep) {
        this.transactionId = transactionId;
        this.order = order;
        this.concept = concept;
        this.description = description;
        this.price = price;
        this.courseId = courseId;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.postalCode = postalCode;
        this.phone = phone;
        this.company = company;
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

    public boolean isProcessedInFinantialState() {
        return finantialState;
    }

    public boolean isProcessedInLearningState() {
        return learningState;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public String getCompany() {
        return company;
    }

    public String getPhone() {
        return phone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getAddress() {
        return address;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConcept() {
        return concept;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public void setEmail(String value) {
        email = value;
    }

    public void setNifCif(String value) {
        nifCif = value;
    }

    public void setCourseId(String value) {
        courseId= value;
    }

    public void setName(String value) {
        name = value;
    }

    public void setSurname(String value) {
        surname = value;
    }

    public void setPostalCode(String value) {
        postalCode = value;
    }

    public void setAddress(String value) {
        address = value;
    }

    public void setPhone(String value) {
        phone = value;
    }

    public void setCompany(String value) {
        company = value;
    }

    public void setIsCompany(boolean value) {
        isCompany = value;
    }

    public void setRegion(String value) {
        region = value;
    }

    public void setCity(String value) {
        city = value;
    }

    public void setFinantialState(boolean value) {
        finantialState = value;
    }

    public int getId() {
        return id;
    }

    public void setLearningState(boolean value) {
        this.learningState = value;
    }
}
