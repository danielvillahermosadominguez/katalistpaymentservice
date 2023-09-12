package com.codurance.katalyst.payment.katalistpayment.moodle;

import com.codurance.katalyst.payment.katalistpayment.utils.StringToDouble;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoodleCourseDTO {
    private int id;
    private String displayname;

    private List<MoodleCustomField> customfields;

    public int getId() {
        return id;
    }

    public String getDisplayname() {
        return displayname;
    }

    public List<MoodleCustomField> getCustomfields() {
        return customfields;
    }

    public double getPrice() throws CustomFieldNotExists {
        if(customfields == null || customfields.isEmpty()) {
           throw new CustomFieldNotExists("The custom field 'price' must be created for a Course in your Moodle application");
        }

        Optional<MoodleCustomField> customField = customfields.stream().filter(cf-> cf.getShortname().equals("price")).findFirst();
        if(!customField.isPresent()) {
            throw new CustomFieldNotExists("The custom field 'price' must be created for a Course in your Moodle application");
        }

      return new StringToDouble(customField.get().getValue()).convert();
    }

}
