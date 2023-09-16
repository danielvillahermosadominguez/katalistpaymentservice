package com.codurance.katalyst.payment.application.integration;

import com.codurance.katalyst.payment.application.moodle.MoodleAPIClientImpl;
import com.codurance.katalyst.payment.application.moodle.MoodleCourseDTO;
import com.codurance.katalyst.payment.application.moodle.MoodleUserDTO;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MoodleAPIClientShould {
    public static final String STUDENT_ROL_ID = "5";
    public static final String EQUAL_SYMBOL = "=";
    public static final String JOIN_SYMBOL = "&";

    private MoodleWireMockHelper moodleService = null;
    public MoodleAPIClientImpl moodleClient = new MoodleAPIClientImpl(new RestTemplate());
    public static final int WIREMOCK_MOODLE_PORT = 9000;

    private String moodleToken = "RANDOM_TOKEN";

    private String urlBase = "http://localhost:9000/webservice/rest/server.php?";

    Gson gson = new Gson();


    @BeforeEach
    void beforeEach() {
        if (this.moodleService == null) {
            this.moodleService = new MoodleWireMockHelper();
            this.moodleClient.setURLBase(urlBase);
            this.moodleClient.setToken(moodleToken);
            this.moodleService.setPort(WIREMOCK_MOODLE_PORT);
            this.moodleService.setToken(moodleToken);
            this.moodleService.start();
        }
        this.moodleService.resetAndConfigure();
    }

    @AfterEach
    void afterEach() {
        this.moodleService.stop();
    }

    @Test
    public void get_a_course_by_id_when_the_course_exists() {
        Map<String, Object> courseBody = createCourse(1, "RANDOM_DISPLAY_NAME", 90.5);
        String json = gson.toJson(Arrays.asList(courseBody).toArray());
        moodleService.stubForPostWithStatusOK("core_course_get_courses", json);

        MoodleCourseDTO course = moodleClient.getCourse("1");

        assertThat(course).isNotNull();
    }

    @Test
    public void get_users_by_field_when_there_are_not_users() throws UnsupportedEncodingException {
        String json = gson.toJson(Arrays.asList());

        moodleService.stubForPostWithStatusOK("core_user_get_users_by_field", json);

        MoodleUserDTO user = moodleClient.getUserByMail("random@example.com");

        assertThat(user).isNull();
    }

    @Test
    public void create_user_when_the_user_not_exists() throws UnsupportedEncodingException {
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("id", 1);
        bodyMap.put("username", "RANDOM_USERNAME");
        bodyMap.put("email", "RANDOM_USERNAME@email.com");
        String json = gson.toJson(Arrays.asList(bodyMap).toArray());

        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        requestBodyMap.put("users[0][username]", "RANDOM_USERNAME");
        requestBodyMap.put("users[0][createpassword]", "1");
        requestBodyMap.put("users[0][email]", "RANDOM_USERNAME@email.com");
        requestBodyMap.put("users[0][firstname]", "RANDOM_FIRST_NAME");
        requestBodyMap.put("users[0][lastname]", "RANDOM_LAST_NAME");

        moodleService.stubForPostWithStatusOKAndBodyParameters("core_user_create_users",
                joinParameters(requestBodyMap),
                json);

        MoodleUserDTO user = moodleClient.createUser("RANDOM_FIRST_NAME", "RANDOM_LAST_NAME", "RANDOM_USERNAME@email.com");

        assertThat(user.getId()).isEqualTo("1");
    }

    @Test
    public void enrole_an_user_to_a_course() throws UnsupportedEncodingException {
        String json = gson.toJson(Arrays.asList().toArray());
        Map<String, String> requestBodyMap = new LinkedHashMap<>();
        requestBodyMap.put("enrolments[0][roleid]", STUDENT_ROL_ID);
        requestBodyMap.put("enrolments[0][userid]", "1");
        requestBodyMap.put("enrolments[0][courseid]", "9");
        String requestBody = joinParameters(requestBodyMap);
        moodleService.stubForPostWithStatusOKAndBodyParameters("enrol_manual_enrol_users",
                requestBody,
                json);

        MoodleCourseDTO course = mock(MoodleCourseDTO.class);
        when(course.getId()).thenReturn(9);
        MoodleUserDTO user = mock(MoodleUserDTO.class);
        when(user.getId()).thenReturn("1");

        moodleClient.enroleToTheCourse(course, user);

        moodleService.verify(1, "enrol_manual_enrol_users", requestBody);
    }

    @Test
    public void get_enrolled_users_when_are_not_users_enrolled() throws UnsupportedEncodingException {
        String json = gson.toJson(Arrays.asList().toArray());
        moodleService.stubForPostWithStatusOK("core_enrol_get_enrolled_users", json);

        boolean exists = moodleClient.existsAnUserinThisCourse("RANDOM_COURSE_ID", "RANDOM_EMAIL");

        assertThat(exists).isFalse();
    }

    protected String joinParameters(Map<String, String> requestBodyMap) throws UnsupportedEncodingException {
        String requestBody = "";

        for (Map.Entry<String, String> parameter : requestBodyMap.entrySet()) {
            requestBody += unicode(parameter.getKey()) + EQUAL_SYMBOL + unicode(parameter.getValue()) + JOIN_SYMBOL;
        }
        if (!requestBody.isEmpty()) {
            requestBody = requestBody.substring(0, requestBody.length() - 1);
        }
        return requestBody;
    }


    protected String unicode(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }

    private Map<String, Object> createCourse(int id, String displayName, double price) {
        Map<String, Object> customField = new HashMap<>();
        customField.put("name", "price");
        customField.put("shortname", "price");
        customField.put("value", price + "");
        List<Map> customFields = Arrays.asList(customField);

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("displayname", displayName);
        map.put("customfields", customFields.toArray());

        return map;
    }
}
