package com.codurance.katalyst.payment.application.ports;

import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;

public interface MoodleApiClient {
    boolean existsAnUserinThisCourse(String courseId, String email);

    MoodleUser getUserByMail(String email);

    MoodleUser createUser(String name, String surname, String email);

    void enroleToTheCourse(MoodleCourse course, MoodleUser user);

    MoodleCourse getCourse(String courseId);

}
