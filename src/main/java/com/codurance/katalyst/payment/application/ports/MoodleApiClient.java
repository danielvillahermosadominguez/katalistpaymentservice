package com.codurance.katalyst.payment.application.ports;

import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.moodle.exception.MoodleNotRespond;

public interface MoodleApiClient {
    boolean existsAnUserinThisCourse(String courseId, String email) throws MoodleNotRespond;

    MoodleUser getUserByMail(String email) throws MoodleNotRespond;

    MoodleUser createUser(String name, String surname, String email) throws MoodleNotRespond;

    void enrolToTheCourse(MoodleCourse course, MoodleUser user) throws MoodleNotRespond;

    MoodleCourse getCourse(String courseId) throws MoodleNotRespond;

}
