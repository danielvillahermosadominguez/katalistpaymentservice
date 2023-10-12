package com.codurance.katalyst.payment.application.model.ports.moodle;

import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleUser;

public interface MoodleApiClient {
    MoodleUser createUser(MoodleUser user) throws MoodleNotRespond;

    void enrolToTheCourse(MoodleCourse course, MoodleUser user) throws MoodleNotRespond;

    boolean existsAnUserinThisCourse(String courseId, String email) throws MoodleNotRespond;

    MoodleCourse getCourse(String courseId) throws MoodleNotRespond;

    MoodleUser getUserByMail(String email) throws MoodleNotRespond;

    MoodleUser getUserByUserName(String userName) throws MoodleNotRespond;
}
