package com.codurance.katalyst.payment.application.model.ports.moodle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MoodleUser {
    @JsonProperty
    protected String id;

    @JsonProperty("username")
    protected String userName;

    @JsonProperty
    protected String email;

    @JsonProperty("firstname")
    private String name;

    @JsonProperty("lastname")
    private String lastName;

    public MoodleUser() {

    }

    public MoodleUser(String id,
                      String name,
                      String lastName,
                      String userName,
                      String email) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.userName = userName;
        this.email = email;
    }

    public MoodleUser(String name,
                      String lastName,
                      String userName,
                      String email) {
        this.name = name;
        this.userName = userName;
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
        return userName;
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
        return Objects.equals(id, that.id) && Objects.equals(userName, that.userName) && Objects.equals(email, that.email) && Objects.equals(name, that.name) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, email, name, lastName);
    }

    public boolean haveSameMainData(MoodleUser currentUser) {
        var result = userName.equals(currentUser.getUserName());
        result &= name.equals(currentUser.getName());
        result &= lastName.equals(currentUser.getLastName());
        result &= email.equals(currentUser.getEmail());
        return result;
    }
}
