package com.codurance.katalyst.payment.application;

import com.codurance.katalyst.payment.application.moodle.MoodleCourseDTO;
import com.codurance.katalyst.payment.application.moodle.MoodleUserDTO;

public interface MoodleApiClient {
    boolean existsAnUserinThisCourse(String courseId, String email);

    MoodleUserDTO getUserByMail(String email);

    MoodleUserDTO createUser(String name, String surname, String email);

    void enroleToTheCourse(MoodleCourseDTO course, MoodleUserDTO user);

    MoodleCourseDTO getCourse(String courseId);

}
