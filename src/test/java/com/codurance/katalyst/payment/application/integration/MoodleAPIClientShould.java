package com.codurance.katalyst.payment.application.integration;

import com.codurance.katalyst.payment.application.integration.wiremock.MoodleWireMockServer;
import com.codurance.katalyst.payment.application.moodle.MoodleAPIClientAdapter;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.moodle.exception.MoodleNotRespond;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
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
    public MoodleAPIClientAdapter apiClient = new MoodleAPIClientAdapter(new RestTemplate());
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
    public void get_a_course_by_id_when_the_course_exists() throws MoodleNotRespond {
        Integer courseId = 1;
        var responseBody = wireMock.createResponseBodyGetCoursesOk(
                courseId,
                "RANDOM_DISPLAY_NAME",
                90.5);
        wireMock.stubForGetCoursesWithStatusOk(Arrays.asList(responseBody));

        var course = apiClient.getCourse(courseId.toString());

        assertThat(course).isNotNull();
    }

    @Test
    public void get_course_by_id_when_the_course_not_exists() throws MoodleNotRespond {
        Integer courseId = 1;
        wireMock.stubForGetCoursesWithStatusOk(Arrays.asList());

        var course = apiClient.getCourse(courseId.toString());

        assertThat(course).isNull();
    }

    @Test
    public void throw_an_moodle_exception_when_the_get_courses_endpoint_not_exist() {
        Integer courseId = 1;
        var thrown = Assertions.assertThrows(MoodleNotRespond.class, ()-> {
            apiClient.getCourse(courseId.toString());
        });

        assertThat(thrown).isNotNull();
        assertThat(thrown.getRequestBody()).isEqualTo("{options[ids][0]=[1]}");
        assertThat(thrown.getFunction()).isEqualTo( "core_course_get_courses");
        assertThat(thrown.getEndPoint()).isEqualTo(
                apiClient.generateEndPoint("core_course_get_courses")
        );
    }

    @Test
    public void get_users_by_field_when_the_user_not_exist() {
        wireMock.stubForGetUsersByFieldWithStatusOk(Arrays.asList());

        var user = apiClient.getUserByMail("random@example.com");

        assertThat(user).isNull();
    }

    @Test
    public void create_user_when_the_user_not_exists() throws UnsupportedEncodingException {
        Integer userId = 1;
        String userName = "RANDOM_USERNAME";
        String email = "RANDOM_USERNAME@email.com";
        String firstName = "RANDOM_FIRST_NAME";
        String lastName = "RANDOM_LAST_NAME";
        String createPassword = "1";
        Map<String, Object> responseBody = wireMock.createResponseBodyCreateUserOk(
                userId,
                userName,
                email
        );

        Map<String, String> requestBodyParameters = wireMock.createRequestBodyParametersCreateUser(
                userName,
                email,
                firstName,
                lastName,
                createPassword
        );

        wireMock.stubForCreateUsersWithStatusOk(requestBodyParameters, responseBody);

        var user = apiClient.createUser(
                firstName,
                lastName,
                email
        );

        assertThat(user.getId()).isEqualTo(userId + "");
    }

    @Test
    public void enrole_an_user_to_a_course() throws UnsupportedEncodingException {
        var userId = "1";
        var courseId = 9;
        List<Map<String, Object>> responseBody = Arrays.asList();
        var requestBody = wireMock.createRequestBodyParametersEnroleAnUserToCourse(
                userId,
                courseId,
                STUDENT_ROL_ID
        );
        wireMock.stubForEnrollUsersWithStatusOk(requestBody, responseBody);

        var course = mock(MoodleCourse.class);
        when(course.getId()).thenReturn(courseId);
        var user = mock(MoodleUser.class);
        when(user.getId()).thenReturn(userId);

        apiClient.enroleToTheCourse(course, user);

        wireMock.verifyEnrolUsersIsCalled(1, requestBody);
    }

    @Test
    public void get_enrolled_users_when_are_not_users_enrolled() {

        List<Map<String, Object>> responseBody = Arrays.asList();
        wireMock.stubForGetEnrolledUsersWithStatusOK(responseBody);

        var exists = apiClient.existsAnUserinThisCourse(
                "RANDOM_COURSE_ID",
                "RANDOM_EMAIL"
        );

        assertThat(exists).isFalse();
    }
}
