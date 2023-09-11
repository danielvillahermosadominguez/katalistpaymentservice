package com.codurance.katalyst.payment.katalistpayment.courses;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "COURSES")
public class DBCourse {

    private Long courseId;

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    @Id
    public Long getCourseId() {
        return courseId;
    }

    private String name;

    private double price;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price =  price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
