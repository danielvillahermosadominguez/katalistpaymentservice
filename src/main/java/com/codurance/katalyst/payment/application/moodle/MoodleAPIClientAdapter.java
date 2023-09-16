package com.codurance.katalyst.payment.application.moodle;

import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;
import com.codurance.katalyst.payment.application.utils.APIClient;
import com.codurance.katalyst.payment.application.utils.EMail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MoodleAPIClientAdapter extends APIClient implements MoodleApiClient {
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
    public static final String ENROLMENTS_0_COURSEID = "enrolments[0][courseid]";
    public static final String OPTIONS_IDS_0 = "options[ids][0]";
    @Value("${moodle.urlbase}")
    private String URL_BASE;

    public void setURLBase(String urlBase){
        this.URL_BASE = urlBase;
    }

    @Value("${moodle.token}")
    private String token;

    public void setToken(String token){
        this.token = token;
    }

    private String format = "json";

    public MoodleAPIClientAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String generateEndPoint(String moodleWsFunction) {
        return URL_BASE + WSTOKEN + token + WSFUNCTION + moodleWsFunction + MOODLEWSRESTFORMAT + format;
    }
    private List<MoodleUser> getUsersForCourse(String courseId) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(COURSEID, courseId);
        var request = createRequest(map, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        var response = restTemplate.postForEntity(
                generateEndPoint("core_enrol_get_enrolled_users"),
                request,
                MoodleUser[].class);
        return Arrays.stream(response.getBody()).toList();
    }

    public boolean existsAnUserinThisCourse(String courseId, String email) {
        //TODO: We need to review this function - performance issue
        String lowerCaseEmailInput = email.toLowerCase();
        var users = getUsersForCourse(courseId);
        var filtered = users
                .stream()
                .filter(user -> {
                    var userEmail = user.getEmail();
                    var lowerCaseEmail = userEmail.toLowerCase();
                    return lowerCaseEmail.equals(lowerCaseEmailInput);
                })
                .collect(Collectors.toList());
        return !filtered.isEmpty();
    }

    public MoodleUser getUserByMail(String email) {
        ResponseEntity<MoodleUser[]> response = null;
        MoodleUser result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add(FIELD, "email");
        map.add(VALUES_ARRAY_0,  email);
        var request = createRequest(map, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        try {
            response = restTemplate.postForEntity(
                    generateEndPoint("core_user_get_users_by_field"),
                    request,
                    MoodleUser[].class);
            result = getFirst(response);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public MoodleUser createUser(String name, String surname, String email) {
        ResponseEntity<MoodleUser[]> response = null;
        MoodleUser result = null;
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        var mail = new EMail(email);
        map.add(USERS_0_USERNAME, mail.getUserName());
        map.add(USERS_0_CREATEPASSWORD, CREATE_PASWORD_AND_SEND);
        map.add(USERS_0_EMAIL,  email);
        map.add(USERS_0_FIRSTNAME,name);
        map.add(USERS_0_LASTNAME, surname);
        var request = createRequest(map, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        try {
            response = restTemplate.postForEntity(
                    generateEndPoint("core_user_create_users"),
                    request,
                    MoodleUser[].class);
            result = getFirst(response);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public void enroleToTheCourse(MoodleCourse course, MoodleUser user) {
        MoodleUser result = null;
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        var roleId = STUDENT_ROLE_ID;
        var userId = user.getId();
        map.add(ENROLMENTS_0_ROLEID, roleId);
        map.add(ENROLMENTS_0_USERID, userId);
        map.add(ENROLMENTS_0_COURSEID, course.getId() + "");
        var request = createRequest(map, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

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

    public MoodleCourse getCourse(String courseId) {
        ResponseEntity<MoodleCourse[]> response = null;
        MoodleCourse result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add(OPTIONS_IDS_0, courseId);
        var request = createRequest(map, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        try {
            response = restTemplate.postForEntity(
                    generateEndPoint("core_course_get_courses"),
                    request,
                    MoodleCourse[].class);
            result = getFirst(response);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }
}
