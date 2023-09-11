package com.codurance.katalyst.payment.katalistpayment.moodle;

public class MoodleCourseDTO {
    private int id;
    private String displayname;

    private double price;

    public int getId() {
        return id;
    }

    public String getDisplayname() {
        return displayname;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
}
