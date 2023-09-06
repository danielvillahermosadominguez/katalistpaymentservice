package com.codurance.katalyst.payment.katalistpayment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MoodleAPIClient {
    private static String URL_BASE = "https://codurance.moodlecloud.com/webservice/rest/server.php?";
    private String token = "3ef9b832fd76a6cac0c67c053d0a38d5";
    private String format = "json";

    @Autowired
    private RestTemplate restTemplate;

    private List<MoodleUserDTO> getUsersForCourse(String courseId) {
        String url = URL_BASE+ "wstoken=" + token + "&wsfunction=core_enrol_get_enrolled_users"+"&moodlewsrestformat="+format;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("courseid", courseId);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<MoodleUserDTO[]> response = restTemplate.postForEntity(url,request, MoodleUserDTO[].class);

        return Arrays.stream(response.getBody()).toList();
    }

    public boolean existsAnUserinThisCourse(String courseId, String email) {
        List<MoodleUserDTO> users = getUsersForCourse(courseId);
        List<MoodleUserDTO> filtered = users.stream().filter( c-> c.getEmail().toLowerCase().equals(email.toLowerCase())).collect(Collectors.toList());
        return !filtered.isEmpty();
    }
}
