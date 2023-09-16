package com.codurance.katalyst.payment.application.moodle.dto;

public class MoodleCustomField {
    protected String name;
    protected String shortname;
    protected String type;
    protected String valueraw;

    protected String value;

    public String getName() {
        return name;
    }

    public String getShortname() {
        return shortname;
    }

    public String getType() {
        return type;
    }

    public String getValueraw() {
        return valueraw;
    }

    public String getValue() {
        return value;
    }
}
