package com.codurance.katalyst.payment.application.moodle.dto;

import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.utils.StringUtil;

import java.util.List;

public class MoodleCourse {
    public static final String THE_CUSTOM_FIELD_PRICE_NOT_EXIST = "The custom field 'price' must be created for a Course in your Moodle application";
    protected int id;
    protected String displayname;
    protected List<MoodleCustomField> customfields;

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayname;
    }

    public List<MoodleCustomField> getCustomfields() {
        return customfields;
    }

    public double getPrice() throws CustomFieldNotExists {
        if(customfields == null || customfields.isEmpty()) {
           throw new CustomFieldNotExists(THE_CUSTOM_FIELD_PRICE_NOT_EXIST);
        }

        var customField = customfields.stream().filter(cf-> cf.getShortname().equals("price")).findFirst();
        if(!customField.isPresent()) {
            throw new CustomFieldNotExists(THE_CUSTOM_FIELD_PRICE_NOT_EXIST);
        }

      return new StringUtil(customField.get().getValue()).tryToConvertToDouble();
    }

}
