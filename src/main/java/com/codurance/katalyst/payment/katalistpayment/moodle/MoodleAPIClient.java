package com.codurance.katalyst.payment.katalistpayment.moodle;

import com.codurance.katalyst.payment.katalistpayment.utils.APIClient;
import com.codurance.katalyst.payment.katalistpayment.utils.Mail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MoodleAPIClient extends APIClient {
    public static final String WSTOKEN = "wstoken=";
    public static final String WSFUNCTION = "&wsfunction=";
    public static final String MOODLEWSRESTFORMAT = "&moodlewsrestformat=";
    public static final String STUDENT_ROLE_ID = "5";
    public static final String COURSEID = "courseid";
    public static final String FIELD = "field";
    public static final String VALUES_ARRAY_0 = "values[0]";
    public static final String USERS_0_USERNAME = "users[0][username]";
    public static final String USERS_0_CREATEPASSWORD = "users[0][createpassword]";
    public static final String USERS_0_EMAIL = "users[0][email]";
    public static final String USERS_0_FIRSTNAME = "users[0][firstname]";
    public static final String USERS_0_LASTNAME = "users[0][lastname]";
    public static final String CREATE_PASWORD_AND_SEND = "1";
    public static final String ENROLMENTS_0_ROLEID = "enrolments[0][roleid]";
    public static final String ENROLMENTS_0_USERID = "enrolments[0][userid]";
    public static final String ENROLMENTS_0_COURSEID = "enrolments[0][courseid]=";
    public static final String OPTIONS_IDS_0 = "options[ids][0]";
    @Value("${moodle.urlbase}")
    private String URL_BASE;

    @Value("${moodle.token}")
    private String token;
    private String format = "json";

    private String generateEndPoint(String moodleWsFunction) {
        return URL_BASE+ WSTOKEN + token + WSFUNCTION +moodleWsFunction+ MOODLEWSRESTFORMAT +format;
    }
    private List<MoodleUserDTO> getUsersForCourse(String courseId) {
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add(COURSEID, courseId);
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);
        ResponseEntity<MoodleUserDTO[]> response = restTemplate.postForEntity(
                generateEndPoint("core_enrol_get_enrolled_users"),
                request,
                MoodleUserDTO[].class);
        return Arrays.stream(response.getBody()).toList();
    }

    public boolean existsAnUserinThisCourse(String courseId, String email) {
        //TODO: We need to review this function - performance issue
        String lowerCaseEmailInput = email.toLowerCase();
        List<MoodleUserDTO> users = getUsersForCourse(courseId);
        List<MoodleUserDTO> filtered = users
                .stream()
                .filter(user -> {
                    String userEmail = user.getEmail();
                    String lowerCaseEmail = userEmail.toLowerCase();
                    return lowerCaseEmail.equals(lowerCaseEmailInput);
                })
                .collect(Collectors.toList());
        return !filtered.isEmpty();
    }

    public MoodleUserDTO getUserByMail(String email) throws UnsupportedEncodingException {
        ResponseEntity<MoodleUserDTO[]> response = null;
        MoodleUserDTO result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add(FIELD, "email");
        map.add(VALUES_ARRAY_0,  email);
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);

        try {
            response = restTemplate.postForEntity(
                    generateEndPoint("core_user_get_users_by_field"),
                    request,
                    MoodleUserDTO[].class);
            result = getFirst(response);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public MoodleUserDTO createUser(String name, String surname, String email) throws UnsupportedEncodingException {
        ResponseEntity<MoodleUserDTO[]> response = null;
        MoodleUserDTO result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        Mail mail = new Mail(email);
        map.add(USERS_0_USERNAME, mail.getUserName());
        map.add(USERS_0_CREATEPASSWORD, CREATE_PASWORD_AND_SEND);
        map.add(USERS_0_EMAIL,  email);
        map.add(USERS_0_FIRSTNAME,name);
        map.add(USERS_0_LASTNAME, surname);
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);

        try {
            response = restTemplate.postForEntity(
                    generateEndPoint("core_user_create_users"),
                    request,
                    MoodleUserDTO[].class);
            result = getFirst(response);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public void enroleToTheCourse(MoodleCourseDTO course, MoodleUserDTO user) {
        MoodleUserDTO result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        String roleId = STUDENT_ROLE_ID;
        String userId = user.getId();
        map.add(ENROLMENTS_0_ROLEID, roleId);
        map.add(ENROLMENTS_0_USERID, userId);
        map.add(ENROLMENTS_0_COURSEID, course.getId()+"");
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);

        try {
            restTemplate.postForEntity(
                    generateEndPoint("enrol_manual_enrol_users"),
                    request,
                    String.class);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }
    }

    public MoodleCourseDTO getCourse(String courseId) {
        ResponseEntity<MoodleCourseDTO[]> response = null;
        MoodleCourseDTO result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add(OPTIONS_IDS_0, courseId);
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);

        try {
            response = restTemplate.postForEntity(
                    generateEndPoint("core_course_get_courses"),
                    request,
                    MoodleCourseDTO[].class);
            result = getFirst(response);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }
}
