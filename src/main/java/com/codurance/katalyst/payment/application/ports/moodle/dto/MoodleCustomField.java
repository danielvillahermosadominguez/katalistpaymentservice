package com.codurance.katalyst.payment.application.ports.moodle.dto;

public class MoodleCustomField {
    protected String name;
    protected String shortname;
    protected String type;
    protected String valueraw;

    protected String value;

    public MoodleCustomField() {

    }
    public MoodleCustomField(String name, String shortName, String type, String valueRaw, String value) {
        this.name =name;
        this.shortname = shortName;
        this.type = type;
        this.valueraw = valueRaw;
        this.value = value;
    }

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

    public void setValue(String value) {
        this.value = value;
    }
}
