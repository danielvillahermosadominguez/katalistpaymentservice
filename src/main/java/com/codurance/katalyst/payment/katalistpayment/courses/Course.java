package com.codurance.katalyst.payment.katalistpayment.courses;

public class Course {
    private int id;
    private String name;

    private double prize;

    public Course(int id, String name, double prize) {
        this.id = id;
        this.name = name;
        this.prize = prize;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrize() {
        return prize;
    }
}
