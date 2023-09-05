package com.codurance.katalyst.payment.katalistpayment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MoodleAPIClient {
    private static String URL_BASE = "https://codurance.moodlecloud.com/webservice/rest/server.php?";
    private String token = "3ef9b832fd76a6cac0c67c053d0a38d5";
    private String format = "json";

    @Autowired
    private RestTemplate restTemplate;

    public List<MoodleUserDTO> getUsersForCourse(String courseId) {
        String url = URL_BASE+ "wstoken=" + token + "&wsfunction=core_enrol_get_enrolled_users"+"&moodlewsrestformat="+format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("courseid", courseId);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<MoodleUserDTO[]> response = restTemplate.postForEntity(url,request, MoodleUserDTO[].class);

        return Arrays.stream(response.getBody()).toList();
    }
}
