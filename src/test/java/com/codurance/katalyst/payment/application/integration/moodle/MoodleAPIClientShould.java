package com.codurance.katalyst.payment.application.integration.moodle;

import com.codurance.katalyst.payment.application.infrastructure.adapters.moodle.MoodleAPIClientAdapter;
import com.codurance.katalyst.payment.application.integration.wiremock.MoodleWireMockServer;
import com.codurance.katalyst.payment.application.model.ports.email.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleCourse;
import com.codurance.katalyst.payment.application.model.ports.moodle.dto.MoodleUser;
import com.codurance.katalyst.payment.application.model.ports.moodle.exception.MoodleNotRespond;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
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
    private String token = "RANDOM_TOKEN";
    private MoodleWireMockServer wireMock = null;
    public MoodleAPIClientAdapter apiAdapter = new MoodleAPIClientAdapter(new RestTemplate());
    public static final String STUDENT_ROL_ID = "5";


    @BeforeEach
    void beforeEach() {
        if (this.wireMock == null) {
            this.wireMock = new MoodleWireMockServer();
            this.apiAdapter.setURLBase(urlBase);
            this.apiAdapter.setToken(token);
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
    public void get_a_course_by_id_when_the_course_exists() throws MoodleNotRespond, JsonProcessingException {
        Integer courseId = 1;
        var responseBody = wireMock.createResponseBodyGetCoursesOk(
                courseId,
                "RANDOM_DISPLAY_NAME",
                90.5);
        wireMock.stubForGetCoursesWithStatusOk(Arrays.asList(responseBody));

        var course = apiAdapter.getCourse(courseId.toString());

        assertThat(course).isNotNull();
    }

    @Test
    public void get_course_by_id_when_the_course_not_exists() throws MoodleNotRespond, JsonProcessingException {
        Integer courseId = 1;
        wireMock.stubForGetCoursesWithStatusOk(Arrays.asList());

        var course = apiAdapter.getCourse(courseId.toString());

        assertThat(course).isNull();
    }

    @Test
    public void throw_an_moodle_exception_when_get_courses_not_respond() throws JSONException {
        Integer courseId = 1;
        var expectedBody = String.format("""
                {
                    "options[ids][0]":["%s"]
                }
                """, courseId.toString());
        var thrown = Assertions.assertThrows(MoodleNotRespond.class, () -> {
            apiAdapter.getCourse(courseId.toString());
        });


        assertThat(thrown).isNotNull();
        JSONAssert.assertEquals(thrown.getRequestBody(), expectedBody, false);
        assertThat(thrown.getFunction()).isEqualTo("core_course_get_courses");
        assertThat(thrown.getEndPoint()).isEqualTo(
                apiAdapter.generateEndPoint("core_course_get_courses")
        );
    }

    @Test
    public void get_users_by_email_when_the_user_not_exist() throws MoodleNotRespond, JsonProcessingException {
        wireMock.stubForGetUsersByFieldWithStatusOk(Arrays.asList());

        var user = apiAdapter.getUserByMail("random@example.com");

        assertThat(user).isNull();
    }

    @Test
    public void get_users_by_email_when_the_user_exist() throws MoodleNotRespond, JsonProcessingException {
        var email = "random@example.com";
        var responseBody = wireMock.createResponseBodyGetUserByFieldOk(
                1,
                "RANDOM_USER_NAME",
                email
        );
        wireMock.stubForGetUsersByFieldWithStatusOk(Arrays.asList(responseBody));

        var user = apiAdapter.getUserByMail(email);

        assertThat(user).isNotNull();
    }

    @Test
    public void throw_an_moodle_exception_when_the_get_user_by_email_not_respond() throws JSONException {

        var expectedBody = """
                {
                    "field":["email"],
                    "values[0]":["random@example.com"]
                }
                """;
        var thrown = Assertions.assertThrows(MoodleNotRespond.class, () -> {
            apiAdapter.getUserByMail("random@example.com");
        });

        assertThat(thrown).isNotNull();
        JSONAssert.assertEquals(thrown.getRequestBody(), expectedBody, true);
        assertThat(thrown.getFunction()).isEqualTo("core_user_get_users_by_field");
        assertThat(thrown.getEndPoint()).isEqualTo(
                apiAdapter.generateEndPoint("core_user_get_users_by_field")
        );
    }

    @Test
    public void get_users_by_username_when_the_user_not_exist() throws MoodleNotRespond, JsonProcessingException {
        wireMock.stubForGetUsersByFieldWithStatusOk(Arrays.asList());

        var user = apiAdapter.getUserByUserName("random_username");

        assertThat(user).isNull();
    }

    @Test
    public void get_users_by_username_when_the_user_exist() throws MoodleNotRespond, JsonProcessingException {
        var userName = "random_username";
        var responseBody = wireMock.createResponseBodyGetUserByFieldOk(
                1,
                userName,
                "random_email@email.com"
        );

        wireMock.stubForGetUsersByFieldWithStatusOk(Arrays.asList(responseBody));

        var user = apiAdapter.getUserByUserName(userName);

        assertThat(user).isNotNull();
    }

    @Test
    public void throw_an_moodle_exception_when_the_get_user_by_username_not_respond() throws JSONException {

        var expectedBody = """
                {
                    "field":["username"],
                    "values[0]":["random_username"]
                }
                """;
        var thrown = Assertions.assertThrows(MoodleNotRespond.class, () -> {
            apiAdapter.getUserByUserName("random_username");
        });

        assertThat(thrown).isNotNull();
        JSONAssert.assertEquals(thrown.getRequestBody(), expectedBody, true);
        assertThat(thrown.getFunction()).isEqualTo("core_user_get_users_by_field");
        assertThat(thrown.getEndPoint()).isEqualTo(
                apiAdapter.generateEndPoint("core_user_get_users_by_field")
        );
    }

    @Test
    public void create_user_when_the_user_not_exists() throws UnsupportedEncodingException, MoodleNotRespond, NotValidEMailFormat, JsonProcessingException {
        var userId = 1;
        var userName = "RANDOM_USERNAME";
        var email = "RANDOM_USERNAME@email.com";
        var firstName = "RANDOM_FIRST_NAME";
        var lastName = "RANDOM_LAST_NAME";
        var createPassword = "1";
        var responseBody = wireMock.createResponseBodyCreateUserOk(
                userId,
                userName,
                email
        );

        var requestBodyParameters = wireMock.createRequestBodyParametersCreateUser(
                userName,
                email,
                firstName,
                lastName,
                createPassword
        );

        wireMock.stubForCreateUsersWithStatusOk(requestBodyParameters, responseBody);

        var user = apiAdapter.createUser(
                new MoodleUser(firstName, lastName, userName, email)
        );

        assertThat(user.getId()).isEqualTo(userId + "");
    }

    @Test
    public void throw_an_moodle_exception_when_the_create_user_not_respond() throws JSONException {
        var userName = "RANDOM_USERNAME";
        var createPassword = "1";
        var email = "RANDOM_EMAIL@EMAIL.COM";
        var firstName = "RANDOM_NAME";
        var lastName = "RANDOM SURNAME";

        var expectedBody = String.format("""
                {
                    "users[0][username]":["%s"],
                    "users[0][createpassword]":["%s"],
                    "users[0][email]":["%s"],
                    "users[0][firstname]":["%s"],
                    "users[0][lastname]":["%s"]
                }
                """, userName, createPassword, email, firstName, lastName);

        var thrown = Assertions.assertThrows(MoodleNotRespond.class, () -> {
            apiAdapter.createUser(
                    new MoodleUser(firstName, lastName, userName, email)
            );
        });

        assertThat(thrown).isNotNull();
        JSONAssert.assertEquals(thrown.getRequestBody(), expectedBody, true);

        assertThat(thrown.getFunction()).isEqualTo("core_user_create_users");
        assertThat(thrown.getEndPoint()).isEqualTo(
                apiAdapter.generateEndPoint("core_user_create_users")
        );
    }

    @Test
    public void enrole_an_user_to_a_course() throws UnsupportedEncodingException, MoodleNotRespond, JsonProcessingException {
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

        apiAdapter.enrolToTheCourse(course, user);

        wireMock.verifyEnrolUsersIsCalled(1, requestBody);
    }

    @Test
    public void throw_an_moodle_exception_when_enrol_an_user_not_respond() throws JSONException {
        var courseId = 1;
        var userId = "1";
        var expectedBody = String.format("""
                {
                   "enrolments[0][roleid]":["%s"],
                   "enrolments[0][userid]":["%s"],
                   "enrolments[0][courseid]":["%s"]
                }
                """, STUDENT_ROL_ID, userId, courseId);

        var course = mock(MoodleCourse.class);
        when(course.getId()).thenReturn(courseId);
        var user = mock(MoodleUser.class);
        when(user.getId()).thenReturn(userId);
        var thrown = Assertions.assertThrows(MoodleNotRespond.class, () -> {
            apiAdapter.enrolToTheCourse(course, user);
        });

        assertThat(thrown).isNotNull();
        JSONAssert.assertEquals(thrown.getRequestBody(), expectedBody, true);

        assertThat(thrown.getFunction()).isEqualTo("enrol_manual_enrol_users");
        assertThat(thrown.getEndPoint()).isEqualTo(
                apiAdapter.generateEndPoint("enrol_manual_enrol_users")
        );
    }

    @Test
    public void get_enrolled_users_when_are_not_users_enrolled() throws MoodleNotRespond, JsonProcessingException {

        List<Map<String, Object>> responseBody = Arrays.asList();
        wireMock.stubForGetEnrolledUsersWithStatusOK(responseBody);

        var exists = apiAdapter.existsAnUserinThisCourse(
                "RANDOM_COURSE_ID",
                "RANDOM_EMAIL"
        );

        assertThat(exists).isFalse();
    }

    @Test
    public void get_enrolled_users_when_are_users_enrolled() throws MoodleNotRespond, JsonProcessingException {
        var email = "random@example.com";
        var responseBody = wireMock.createResponseBodyGetUserByFieldOk(
                1,
                "RANDOM_USER_NAME",
                email
        );

        wireMock.stubForGetEnrolledUsersWithStatusOK(Arrays.asList(responseBody));

        var exists = apiAdapter.existsAnUserinThisCourse(
                "RANDOM_COURSE_ID",
                email
        );

        assertThat(exists).isTrue();
    }

    @Test
    public void throw_an_moodle_exception_when_get_enrolled_users_not_respond() throws JSONException {
        var expectedBody = """
                {
                "courseid":["RANDOM_COURSE_ID"]
                }
                        """;
        var thrown = Assertions.assertThrows(MoodleNotRespond.class, () -> {
            apiAdapter.existsAnUserinThisCourse(
                    "RANDOM_COURSE_ID",
                    "RANDOM_EMAIL@EMAIL.COM"
            );
        });

        assertThat(thrown).isNotNull();
        JSONAssert.assertEquals(thrown.getRequestBody(), expectedBody, true);
        assertThat(thrown.getFunction()).isEqualTo("core_enrol_get_enrolled_users");
        assertThat(thrown.getEndPoint()).isEqualTo(
                apiAdapter.generateEndPoint("core_enrol_get_enrolled_users")
        );
    }
}
