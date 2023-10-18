package com.codurance.katalyst.payment.application.builders;

import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.CustomFieldNotExists;

import java.util.Map;

public class MoodleCourseBuilder {

    private MoodleCourse item;

    public MoodleCourseBuilder createFromMap(Map<String, String> data) throws CustomFieldNotExists {
        item = createFromMapMoodleCourse(data);
        return this;
    }

    private MoodleCourse createFromMapMoodleCourse(Map<String, String> data) throws CustomFieldNotExists {

        var id = data.get("ID");
        var name = data.get("NAME");
        var price = data.get("PRICE");
        int numericId = Integer.parseInt(id);
        if (price.equals("<NO PRICE AVAILABLE>")) {
            var moodleCourse = new MoodleCourse();
            moodleCourse.setDisplayName(name);
            moodleCourse.setId(numericId);
            return moodleCourse;
        }
        var moodlePrice = new MoodlePrice(price);
        return new MoodleCourse(
                numericId,
                name,
                moodlePrice
        );
    }

    public MoodleCourse getItem() {
        return item;
    }
}
