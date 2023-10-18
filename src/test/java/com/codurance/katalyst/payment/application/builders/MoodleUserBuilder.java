package com.codurance.katalyst.payment.application.builders;

import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleUser;

import java.util.Map;

public class MoodleUserBuilder {

    private MoodleUser item;

    public MoodleUserBuilder createFromMap(Map<String, String> data) {
        item = createMoodleUserFromMap(data);
        return this;
    }

    private MoodleUser createMoodleUserFromMap(Map<String, String> data) {
        var name = data.get("NAME");
        var surname = data.get("SURNAME");
        var userName = data.get("USERNAME");
        var email = data.get("EMAIL");
        return new MoodleUser(name, surname, userName, email);
    }

    public MoodleUser getItem() {
        return item;
    }
}
