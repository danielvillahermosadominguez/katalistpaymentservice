package com.codurance.katalyst.payment.application.integration.wiremock;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@Component
public class MoodleWireMockServer extends WireMockServerExtension {
    private String token;
    private final static String URL_BASE = "/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json";

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, Object> createResponseBodyGetCoursesOk(int id, String displayName, double price) {
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

    public Map<String, Object> createResponseBodyGetUserByFieldOk(int userId, String userName, String email) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("id", userId);
        responseBody.put("username", userName);
        responseBody.put("email", email);

        return responseBody;
    }


    public Map<String, Object> createResponseBodyCreateUserOk(Integer userId, String userName, String email) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("id", userId);
        responseBody.put("username", userName);
        responseBody.put("email", email);
        return responseBody;
    }

    public Map<String, String> createRequestBodyParametersCreateUser(String userName, String email, String firstName, String lastName, String createPassword) {
        Map<String, String> requestBodyParameters = new LinkedHashMap<>();
        requestBodyParameters.put("users[0][username]", userName);
        requestBodyParameters.put("users[0][createpassword]", createPassword);
        requestBodyParameters.put("users[0][email]", email);
        requestBodyParameters.put("users[0][firstname]", firstName);
        requestBodyParameters.put("users[0][lastname]", lastName);
        return requestBodyParameters;
    }

    public Map<String, String> createRequestBodyParametersEnroleAnUserToCourse(String userId, int courseId, String studentRolId) {
        Map<String, String> requestBody = new LinkedHashMap<>();
        requestBody.put("enrolments[0][roleid]", studentRolId);
        requestBody.put("enrolments[0][userid]", userId);
        requestBody.put("enrolments[0][courseid]", courseId + "");
        return requestBody;
    }

    private void stubForPostWithStatusOk(String function, String responseBody) {
        this.wireMockServer.stubFor(
                post(urlEqualTo(generateURL(function)))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withBody(responseBody)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                        )
        );
    }

    private void stubForPostWithStatusOkAndBodyParameters(String function, String requestBody, String responseBody) {
        this.wireMockServer.stubFor(
                post(urlEqualTo(generateURL(function)))
                        .withRequestBody(containing(requestBody))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withBody(responseBody)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE
                                        )
                        )
        );
    }

    public String generateURL(String function) {
        return String.format(URL_BASE, token, function);
    }

    public void stubForEnrollUsersWithStatusOk(Map<String, String> requestBodyMap, List<Map<String, Object>> responseBody) throws UnsupportedEncodingException, JsonProcessingException {
        var jsonResponse = objectMapper.writeValueAsString(responseBody.toArray());
        String requestBody = joinParameters(requestBodyMap);
        stubForPostWithStatusOkAndBodyParameters("enrol_manual_enrol_users",
                requestBody,
                jsonResponse);
    }

    public void stubForCreateUsersWithStatusOk(Map<String, String> requestBodyParameters, Map<String, Object> responseBody) throws UnsupportedEncodingException, JsonProcessingException {
        var json = objectMapper.writeValueAsString(Arrays.asList(responseBody).toArray());
        stubForPostWithStatusOkAndBodyParameters(
                "core_user_create_users",
                joinParameters(requestBodyParameters),
                json);
    }

    public void stubForGetUsersByFieldWithStatusOk(List<Map<String, Object>> responseBody) throws JsonProcessingException {
        var json = objectMapper.writeValueAsString(responseBody.toArray());
        stubForPostWithStatusOk("core_user_get_users_by_field", json);
    }

    public void stubForGetEnrolledUsersWithStatusOK(List<Map<String, Object>> responseBody) throws JsonProcessingException {
        var json = objectMapper.writeValueAsString(responseBody.toArray());
        stubForPostWithStatusOk("core_enrol_get_enrolled_users", json);
    }

    public void stubForGetCoursesWithStatusOk(List<Map<String, Object>> responseBody) throws JsonProcessingException {
        var jsonResponseBody = objectMapper.writeValueAsString(responseBody.toArray());
        stubForPostWithStatusOk("core_course_get_courses", jsonResponseBody);
    }

    public void verify(int quantity, String function, String requestBody) {
        this.wireMockServer.verify(quantity,
                postRequestedFor(
                        urlEqualTo(
                                generateURL(function)
                        )
                ).withRequestBody(containing(requestBody))
        );
    }

    public void verifyEnrolUsersIsCalled(int quantity, Map<String, String> requestBodyMap) throws UnsupportedEncodingException {
        String requestBody = joinParameters(requestBodyMap);
        verify(quantity, "enrol_manual_enrol_users", requestBody);
    }
}
