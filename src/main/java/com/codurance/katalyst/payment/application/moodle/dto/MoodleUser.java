package com.codurance.katalyst.payment.application.moodle.dto;

import java.util.Objects;

public class MoodleUser {
    protected String id;
    protected String username;

    protected String email;
    private String name;

    private String lastName;

    public MoodleUser() {

    }

    public MoodleUser(String id, String name, String lastName, String userName, String email) {
        this.id = id;
        this.name = name;
        this.username = userName;
        this.email = email;
        this.lastName = lastName;
    }

    public MoodleUser(String name, String lastName, String userName, String email) {
        this.name = name;
        this.username = userName;
        this.email = email;
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoodleUser that = (MoodleUser) o;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username) && Objects.equals(email, that.email) && Objects.equals(name, that.name) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, name, lastName);
    }
}
