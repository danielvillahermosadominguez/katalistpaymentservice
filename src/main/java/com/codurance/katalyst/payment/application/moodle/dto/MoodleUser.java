package com.codurance.katalyst.payment.application.moodle.dto;

public class MoodleUser {
    protected String id;
    protected String username;

    protected String email;
    private String name;

    public MoodleUser() {

    }

    public MoodleUser(String name, String userName, String email) {
        this.name = name;
        this.username = userName;
        this.email = email;
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
}
