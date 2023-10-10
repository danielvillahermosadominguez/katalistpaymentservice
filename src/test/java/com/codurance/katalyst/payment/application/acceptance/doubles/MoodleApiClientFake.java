package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodlePrice;
import com.codurance.katalyst.payment.application.ports.moodle.dto.MoodleUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodleApiClientFake implements MoodleApiClient {
    public static int idMoodleCourseCounter = 0;
    public static int idMoodleUserCounter = 0;

    private Map<Integer, MoodleCourse> courses = new HashMap<>();

    private List<MoodleUser> users = new ArrayList<>();

    private Map<Integer, List<MoodleUser>> studentsPerCourse = new HashMap<>();

    public void reset() {
        courses.clear();
        users.clear();
        studentsPerCourse.clear();
    }

    public MoodleCourse addCourse(String fixtureDisplayName, MoodlePrice price) throws CustomFieldNotExists {
        var course = new MoodleCourse(++idMoodleCourseCounter, fixtureDisplayName, price);
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
        return !userList
                .stream()
                .filter(
                        enroledUser -> enroledUser.getId().equals(
                                enroledUser.getEmail()
                        )
                ).toList().isEmpty();
    }

    @Override
    public MoodleUser getUserByMail(String email) {
        var filteredUserList = users
                .stream()
                .filter(user -> user.getEmail().equals(email))
                .toList();
        if (filteredUserList.isEmpty()) {
            return null;
        }
        return filteredUserList.get(0);
    }

    public List<MoodleUser> getAllUsers() {
        return users;
    }

    @Override
    public MoodleUser createUser(MoodleUser user) {
        var result = new MoodleUser(
                ++idMoodleUserCounter + "",
                user.getName(),
                user.getLastName(),
                user.getUserName(),
                user.getEmail());
        users.add(result);
        return result;
    }

    @Override
    public void enrolToTheCourse(MoodleCourse course, MoodleUser user) {
        if (!studentsPerCourse.containsKey(course.getId())) {
            return;
        }
        var userList = studentsPerCourse.get(course.getId());
        var isInTheCourse = !userList.stream()
                .filter(enroledUser -> enroledUser.getId().equals(user.getId()))
                .toList()
                .isEmpty();
        if (!isInTheCourse) {
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

    @Override
    public MoodleUser getUserByUserName(String userName) {
        for (var user : users) {
            if (user.getUserName().equals(userName)) {
                return user;
            }
        }
        return null;
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
