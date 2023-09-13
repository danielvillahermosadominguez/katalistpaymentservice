package com.codurance.katalyst.payment.application.acceptance.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Component
public class MoodleServiceFake {

    private String URL_BASE = "/webservice/rest/server.php?wstoken=%s&wsfunction=%s&moodlewsrestformat=json";
    private WireMockServer wireMockServer = null;

    private List<Object> courses = new ArrayList<>();
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public void start() {
        this.wireMockServer = new WireMockServer(port);
        this.wireMockServer.start();
        this.init();
    }

    private void init() {
        this.wireMockServer.addMockServiceRequestListener(new RequestListener() {
            @Override
            public void requestReceived(Request request, Response response) {

            }
        });
    }

    public void resetAndConfigure() {
        this.reset();
        WireMock.configureFor(port);
    }

    public void addCourse(int id, String displayName, double price) {
        Map<String, Object> customField = new HashMap<>();
        customField.put("name", "price");
        customField.put("shortname", "price");
        customField.put("value", price + "");
        List<Map> customFields = Arrays.asList(customField);
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("displayname", displayName);
        map.put("customfields", customFields.toArray());
        this.courses.add(map);
    }

    public void configureStubs() throws JsonProcessingException {
        Gson gson = new Gson();
        String json = gson.toJson(courses.toArray());

        WireMock.stubFor(
                post(
                        urlEqualTo(String.format(URL_BASE, "AN_INVENTED_TOKEN", "core_course_get_courses"))
                )
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody(json)
                                        .withHeader("Content-Type", "application/json")
                        )
        );
    }

    public void reset() {
        this.wireMockServer.resetRequests();
        courses.clear();
    }
}
