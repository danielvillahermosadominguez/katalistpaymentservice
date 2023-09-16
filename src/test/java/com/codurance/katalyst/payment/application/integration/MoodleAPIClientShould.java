package com.codurance.katalyst.payment.application.integration;

import com.codurance.katalyst.payment.application.integration.wiremock.MoodleWireMockServer;
import com.codurance.katalyst.payment.application.moodle.MoodleAPIClientImpl;
import com.codurance.katalyst.payment.application.moodle.MoodleCourseDTO;
import com.codurance.katalyst.payment.application.moodle.MoodleUserDTO;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MoodleAPIClientShould {
    public static final int WIREMOCK_PORT = 9000;
    private String urlBase = "http://localhost:9000/webservice/rest/server.php?";
    private Gson gson = new Gson();
    private String token = "RANDOM_TOKEN";
    private MoodleWireMockServer wireMock = null;
    public MoodleAPIClientImpl apiClient = new MoodleAPIClientImpl(new RestTemplate());
    public static final String STUDENT_ROL_ID = "5";


    @BeforeEach
    void beforeEach() {
        if (this.wireMock == null) {
            this.wireMock = new MoodleWireMockServer();
            this.apiClient.setURLBase(urlBase);
            this.apiClient.setToken(token);
            this.wireMock.setPort(WIREMOCK_PORT);
            this.wireMock.setToken(token);
            this.wireMock.start();
        }
        this.wireMock.reset();
    }

    @AfterEach
    void afterEach() {
        this.wireMock.stop();
    }

    @Test
    public void get_a_course_by_id_when_the_course_exists() {
        Integer courseId = 1;
        var courseBody = wireMock.createCourse(
                courseId,
                "RANDOM_DISPLAY_NAME",
                90.5);
        var json = gson.toJson(Arrays.asList(courseBody).toArray());
        wireMock.stubForGetCoursesWithStatusOk(json);

        var course = apiClient.getCourse(courseId.toString());

        assertThat(course).isNotNull();
    }

    @Test
    public void get_users_by_field_when_the_user_not_exist() {
        var json = gson.toJson(Arrays.asList());

        wireMock.stubForGetUsersByFieldWithStatusOk(json);

        var user = apiClient.getUserByMail("random@example.com");

        assertThat(user).isNull();
    }

    @Test
    public void create_user_when_the_user_not_exists() throws UnsupportedEncodingException {
        Integer userId = 1;
        var bodyMap = new LinkedHashMap<>();
        bodyMap.put("id", userId);
        bodyMap.put("username", "RANDOM_USERNAME");
        bodyMap.put("email", "RANDOM_USERNAME@email.com");
        var json = gson.toJson(Arrays.asList(bodyMap).toArray());

        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        requestBodyMap.put("users[0][username]", "RANDOM_USERNAME");
        requestBodyMap.put("users[0][createpassword]", "1");
        requestBodyMap.put("users[0][email]", "RANDOM_USERNAME@email.com");
        requestBodyMap.put("users[0][firstname]", "RANDOM_FIRST_NAME");
        requestBodyMap.put("users[0][lastname]", "RANDOM_LAST_NAME");

        wireMock.stubForCreateUsersWithStatusOK(json, requestBodyMap);

        var user = apiClient.createUser(
                "RANDOM_FIRST_NAME",
                "RANDOM_LAST_NAME",
                "RANDOM_USERNAME@email.com"
        );

        assertThat(user.getId()).isEqualTo(userId + "");
    }

    @Test
    public void enrole_an_user_to_a_course() throws UnsupportedEncodingException {
        var userId = "1";
        var courseId = 9;
        var json = gson.toJson(Arrays.asList().toArray());
        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        requestBodyMap.put("enrolments[0][roleid]", STUDENT_ROL_ID);
        requestBodyMap.put("enrolments[0][userid]", userId);
        requestBodyMap.put("enrolments[0][courseid]", courseId + "");
        wireMock.stubForEnroleUsersWithStatusOK(json, requestBodyMap);

        var course = mock(MoodleCourseDTO.class);
        when(course.getId()).thenReturn(courseId);
        var user = mock(MoodleUserDTO.class);
        when(user.getId()).thenReturn(userId);

        apiClient.enroleToTheCourse(course, user);

        wireMock.verifyEnrolUsersIsCalled(1, requestBodyMap);
    }

    @Test
    public void get_enrolled_users_when_are_not_users_enrolled() {
        var json = gson.toJson(Arrays.asList().toArray());
        wireMock.stubForGetEnrolledUsersWithStatusOK(json);

        var exists = apiClient.existsAnUserinThisCourse(
                "RANDOM_COURSE_ID",
                "RANDOM_EMAIL"
        );

        assertThat(exists).isFalse();
    }
}
