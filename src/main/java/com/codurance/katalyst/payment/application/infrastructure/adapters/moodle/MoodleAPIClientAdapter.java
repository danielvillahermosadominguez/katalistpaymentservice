package com.codurance.katalyst.payment.application.infrastructure.adapters.moodle;

import com.codurance.katalyst.payment.application.infrastructure.adapters.common.APIClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MoodleAPIClientAdapter extends APIClient implements MoodleApiClient {
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

    public MoodleAPIClientAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateEndPoint(String moodleWsFunction) {
        return URL_BASE + "wstoken=" + token + "&wsfunction=" + moodleWsFunction + "&moodlewsrestformat=" + "json";
    }
    private List<MoodleUser> getUsersForCourse(String courseId) throws MoodleNotRespond {
        var function = "core_enrol_get_enrolled_users";
        var endPoint = generateEndPoint(function);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(COURSEID, courseId);
        var request = createRequest(
                requestBody,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE
        );

        try {
            var response = restTemplate.postForEntity(
                    endPoint,
                    request,
                    MoodleUser[].class);
            return Arrays.stream(response.getBody()).toList();

        } catch (HttpStatusCodeException httpException) {
            throw new MoodleNotRespond(
                    function,
                    endPoint,
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }
    }

    public boolean existsAnUserinThisCourse(String courseId, String email) throws MoodleNotRespond {
        //TODO: We need to review this function - review the performance
        var lowerCaseEmailInput = email.toLowerCase();
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

    public MoodleUser getUserByMail(String email) throws MoodleNotRespond {
        var function = "core_user_get_users_by_field";
        var endPoint = generateEndPoint(function);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(FIELD, "email");
        requestBody.add(VALUES_ARRAY_0, email);
        var request = createRequest(
                requestBody,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE
        );

        try {
            var response = restTemplate.postForEntity(
                    endPoint,
                    request,
                    MoodleUser[].class);
            return getFirst(response);
        } catch (HttpStatusCodeException httpException) {
            throw new MoodleNotRespond(
                    function,
                    endPoint,
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }
    }

    public MoodleUser createUser(MoodleUser user) throws MoodleNotRespond {
        var function = "core_user_create_users";
        var endPoint = generateEndPoint(function);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(USERS_0_USERNAME, user.getUserName());
        requestBody.add(USERS_0_CREATEPASSWORD, CREATE_PASWORD_AND_SEND);
        requestBody.add(USERS_0_EMAIL, user.getEmail());
        requestBody.add(USERS_0_FIRSTNAME, user.getName());
        requestBody.add(USERS_0_LASTNAME, user.getLastName());
        var request = createRequest(
                requestBody,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE
        );

        try {
            var response = restTemplate.postForEntity(
                    endPoint,
                    request,
                    MoodleUser[].class);
            return getFirst(response);
        } catch (HttpStatusCodeException httpException) {
            throw new MoodleNotRespond(
                    function,
                    endPoint,
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }
    }

    public void enrolToTheCourse(MoodleCourse course, MoodleUser user) throws MoodleNotRespond {
        var function = "enrol_manual_enrol_users";
        var endPoint = generateEndPoint(function);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        var roleId = STUDENT_ROLE_ID;
        var userId = user.getId();
        requestBody.add(ENROLMENTS_0_ROLEID, roleId);
        requestBody.add(ENROLMENTS_0_USERID, userId);
        requestBody.add(ENROLMENTS_0_COURSEID, course.getId() + "");
        var request = createRequest(
                requestBody,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE
        );

        try {
            restTemplate.postForEntity(
                    endPoint,
                    request,
                    String.class);
        } catch (HttpStatusCodeException httpException) {
            throw new MoodleNotRespond(
                    function,
                    endPoint,
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }
    }

    public MoodleCourse getCourse(String courseId) throws MoodleNotRespond {
        var function = "core_course_get_courses";
        var endPoint = generateEndPoint(function);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(OPTIONS_IDS_0, courseId);
        var request = createRequest(
                requestBody,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE
        );

        try {
            var response = restTemplate.postForEntity(
                    endPoint,
                    request,
                    MoodleCourse[].class);
            return getFirst(response);
        } catch (HttpStatusCodeException httpException) {
            throw new MoodleNotRespond(
                    function,
                    endPoint,
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }
    }

    @Override
    public MoodleUser getUserByUserName(String userName) throws MoodleNotRespond {
        var function = "core_user_get_users_by_field";
        var endPoint = generateEndPoint(function);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(FIELD, "username");
        requestBody.add(VALUES_ARRAY_0, userName);
        var request = createRequest(
                requestBody,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE
        );

        try {
            var response = restTemplate.postForEntity(
                    endPoint,
                    request,
                    MoodleUser[].class);
            return getFirst(response);
        } catch (HttpStatusCodeException httpException) {
            throw new MoodleNotRespond(
                    function,
                    endPoint,
                    objectToJSON(requestBody),
                    httpException.getMessage()
            );
        }
    }
}
