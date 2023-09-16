package com.codurance.katalyst.payment.application.acceptance.utils;

import com.codurance.katalyst.payment.application.MoodleApiClient;
import com.codurance.katalyst.payment.application.moodle.MoodleCourseDTO;
import com.codurance.katalyst.payment.application.moodle.MoodleCustomField;
import com.codurance.katalyst.payment.application.moodle.MoodleUserDTO;
import com.codurance.katalyst.payment.application.utils.Mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodleApiClientFake implements MoodleApiClient {

    private class MoodleCustomFieldFake extends MoodleCustomField {

        public MoodleCustomFieldFake(String name, String shortName, String type, String valueRaw, String value) {
            this.name =name;
            this.shortname = shortName;
            this.type = type;
            this.valueraw = valueRaw;
            this.value = value;
        }
    }
    private class MoodleCourseDTOFake extends MoodleCourseDTO {
        public static int idCounter = 0;
        public MoodleCourseDTOFake(String displayName, double price) {
            super();
            this.displayname = displayName;
            this.customfields = new ArrayList<>();
            MoodleCustomField priceField = new MoodleCustomFieldFake("price", "price", "text","", price+"");
            customfields.add(priceField);
            this.id = ++idCounter;
        }
    }

    private class MoodleUserDTOFake extends MoodleUserDTO {
        public static int idCounter = 0;

        public MoodleUserDTOFake(String name, String surname, String email) {
            super();
            Mail mail = new Mail(email);
            this.id = ++idCounter + "";
            this.username = mail.getUserName();
            this.email = email;
        }
    }

    private Map<Integer, MoodleCourseDTO> courses = new HashMap<>();

    private List<MoodleUserDTO> users = new ArrayList<>();

    private Map<Integer, List<MoodleUserDTO>> studentsPerCourse = new HashMap<>();

    public void reset() {
        courses.clear();
        users.clear();
        studentsPerCourse.clear();
    }

    public MoodleCourseDTO addCourse(String fixtureDisplayName, double fixturePrice) {
        MoodleCourseDTO course = new MoodleCourseDTOFake(fixtureDisplayName, fixturePrice);
        this.courses.put(course.getId(),course);
        this.studentsPerCourse.put(course.getId(), new ArrayList<>());
        return course;
    }

    @Override
    public boolean existsAnUserinThisCourse(String courseId, String email) {
        if(!studentsPerCourse.containsKey(courseId)) {
            return false;
        }
        List<MoodleUserDTO> userList = studentsPerCourse.get(courseId);
        return !userList.stream().filter(enroledUser-> enroledUser.getId().equals(enroledUser.getEmail())).toList().isEmpty();

    }

    @Override
    public MoodleUserDTO getUserByMail(String email) {
        List<MoodleUserDTO> filteredUserList = users.stream().filter(user -> user.getEmail().equals(email)).toList();
        if (filteredUserList.isEmpty()) {
            return null;
        }
        return filteredUserList.get(0);
    }

    @Override
    public MoodleUserDTO createUser(String name, String surname, String email) {
        MoodleUserDTOFake user = new MoodleUserDTOFake(name, surname, email);
        users.add(user);
        return user;
    }

    @Override
    public void enroleToTheCourse(MoodleCourseDTO course, MoodleUserDTO user) {
        if(!studentsPerCourse.containsKey(course.getId())) {
            return;
        }
        List<MoodleUserDTO> userList = studentsPerCourse.get(course.getId());
        if(userList.stream().filter(enroledUser-> enroledUser.getId().equals(user.getId())).toList().isEmpty()) {
            userList.add(user);
        }
    }

    @Override
    public MoodleCourseDTO getCourse(String courseId) {
        int courseIdInteger = Integer.parseInt(courseId);
        if (!courses.containsKey(courseIdInteger)) {
            return null;
        }
        return courses.get(courseIdInteger);
    }
}
