package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleCustomField;
import com.codurance.katalyst.payment.application.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.ports.Holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.ports.Holded.exceptions.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;

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
    private class MoodleCourseDTOFake extends MoodleCourse {
        public static int idCounter = 0;
        public MoodleCourseDTOFake(String displayName, double price) {
            super();
            this.displayname = displayName;
            this.customfields = new ArrayList<>();
            var priceField = new MoodleCustomFieldFake("price", "price", "text", "", price + "");
            customfields.add(priceField);
            this.id = ++idCounter;
        }
    }

    private class MoodleUserDTOFake extends MoodleUser {
        public static int idCounter = 0;

        public MoodleUserDTOFake(String name, String surname, String email) throws NotValidEMailFormat {
            super();
            var mail = new HoldedEmail(email);
            this.id = ++idCounter + "";
            this.username = mail.getUserName();
            this.email = email;
        }
    }

    private Map<Integer, MoodleCourse> courses = new HashMap<>();

    private List<MoodleUser> users = new ArrayList<>();

    private Map<Integer, List<MoodleUser>> studentsPerCourse = new HashMap<>();

    public void reset() {
        courses.clear();
        users.clear();
        studentsPerCourse.clear();
    }

    public MoodleCourse addCourse(String fixtureDisplayName, MoodlePrice price) {
        var course = new MoodleCourseDTOFake(fixtureDisplayName, price.getValue());
        this.courses.put(course.getId(), course);
        this.studentsPerCourse.put(course.getId(), new ArrayList<>());
        return course;
    }

    public MoodleCourse updatePrice(int courseId, MoodlePrice price) throws CustomFieldNotExists {
        var course = this.courses.get(courseId);
        course.setPrice(price);
        return course;
    }

    @Override
    public boolean existsAnUserinThisCourse(String courseId, String email) {
        if (!studentsPerCourse.containsKey(courseId)) {
            return false;
        }
        var userList = studentsPerCourse.get(courseId);
        return !userList.stream().filter(enroledUser -> enroledUser.getId().equals(enroledUser.getEmail())).toList().isEmpty();

    }

    @Override
    public MoodleUser getUserByMail(String email) {
        var filteredUserList = users.stream().filter(user -> user.getEmail().equals(email)).toList();
        if (filteredUserList.isEmpty()) {
            return null;
        }
        return filteredUserList.get(0);
    }

    public List<MoodleUser> getAllUsers() {
        return users;
    }

    @Override
    public MoodleUser createUser(String name, String surname, String email) throws NotValidEMailFormat {
        var user = new MoodleUserDTOFake(name, surname, email);
        users.add(user);
        return user;
    }

    @Override
    public void enrolToTheCourse(MoodleCourse course, MoodleUser user) {
        if(!studentsPerCourse.containsKey(course.getId())) {
            return;
        }
        var userList = studentsPerCourse.get(course.getId());
        if(userList.stream().filter(enroledUser-> enroledUser.getId().equals(user.getId())).toList().isEmpty()) {
            userList.add(user);
        }
    }

    @Override
    public MoodleCourse getCourse(String courseId) {
        int courseIdInteger = Integer.parseInt(courseId);
        if (!courses.containsKey(courseIdInteger)) {
            return null;
        }
        return courses.get(courseIdInteger);
    }

    public MoodleCourse getCourseByName(String courseName) {
        for (var course : courses.values()) {
            if (course.getDisplayname().equals(courseName)) {
                return course;
            }
        }
        return null;
    }
}
