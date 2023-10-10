package com.codurance.katalyst.payment.application.ports.moodle;

import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.ports.moodle.exception.MoodleNotRespond;

public interface MoodleApiClient {
    MoodleUser createUser(MoodleUser user) throws MoodleNotRespond;

    void enrolToTheCourse(MoodleCourse course, MoodleUser user) throws MoodleNotRespond;

    boolean existsAnUserinThisCourse(String courseId, String email) throws MoodleNotRespond;

    MoodleCourse getCourse(String courseId) throws MoodleNotRespond;

    MoodleUser getUserByMail(String email) throws MoodleNotRespond;

    MoodleUser getUserByUserName(String userName) throws MoodleNotRespond;
}
