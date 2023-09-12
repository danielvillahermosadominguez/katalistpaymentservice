package com.codurance.katalyst.payment.application.courses;

public class Course {
    private int id;
    private String name;

    private double price;

    public Course(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrize() {
        return price;
    }
}
