package com.codurance.katalyst.payment.application.ports.moodle.dto;

import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoodleCourse {
    public static final String THE_CUSTOM_FIELD_PRICE_NOT_EXIST = "The custom field 'price' must be created for a Course in your Moodle application";

    protected int id;
    protected String displayname;
    protected List<MoodleCustomField> customfields;

    public MoodleCourse() {

    }

    public MoodleCourse(int id, String displayName, MoodlePrice price) throws CustomFieldNotExists {
        this.id = id;
        this.displayname = displayName;
        initCustomFieldForPrice();
        this.setPrice(price);
    }

    public int getId() {
        return id;
    }

    public String getDisplayname() {
        return displayname;
    }

    public List<MoodleCustomField> getCustomfields() {
        return customfields;
    }

    public MoodlePrice getPrice() throws CustomFieldNotExists {

        var customField = getCustomField();

        return new MoodlePrice(customField.get().getValue());
    }

    private void initCustomFieldForPrice() {
        var customField = new MoodleCustomField(
                "price",
                "price",
                "text",
                "",
                "0"
        );
        this.customfields = new ArrayList<>();
        this.customfields.add(customField);
    }

    private Optional<MoodleCustomField> getCustomField() throws CustomFieldNotExists {
        checkCustomFieldsExist();
        var customField = findCustomField("price");
        if (!customField.isPresent()) {
            throw new CustomFieldNotExists(THE_CUSTOM_FIELD_PRICE_NOT_EXIST);
        }
        return customField;
    }

    private Optional<MoodleCustomField> findCustomField(String price) {
        return customfields
                .stream()
                .filter(cf -> cf.getShortname().equals(price))
                .findFirst();
    }

    public void setPrice(MoodlePrice price) throws CustomFieldNotExists {
        var customField = getCustomField();
        var field = customField.get();
        field.setValue(price.getValueRaw());
    }

    private void checkCustomFieldsExist() throws CustomFieldNotExists {
        if (customfields == null || customfields.isEmpty()) {
            throw new CustomFieldNotExists(THE_CUSTOM_FIELD_PRICE_NOT_EXIST);
        }
    }
}
