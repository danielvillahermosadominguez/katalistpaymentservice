package com.codurance.katalyst.payment.katalistpayment.moodle;

import com.codurance.katalyst.payment.katalistpayment.utils.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MoodleAPIClient {
    public static final String WSTOKEN = "wstoken=";
    public static final String WSFUNCTION = "&wsfunction=";
    public static final String MOODLEWSRESTFORMAT = "&moodlewsrestformat=";
    public static final String STUDENT_ROLE_ID = "5";
    @Value("${moodle.urlbase}")
    private String URL_BASE;

    @Value("${moodle.token}")
    private String token;
    private String format = "json";

    @Autowired
    private RestTemplate restTemplate;

    private HttpEntity<MultiValueMap<String, String>> createRequest(MultiValueMap<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return  request;
    }
    private String createURL(String moodleWsFunction) {
        return URL_BASE+ WSTOKEN + token + WSFUNCTION +moodleWsFunction+ MOODLEWSRESTFORMAT +format;
    }
    private List<MoodleUserDTO> getUsersForCourse(String courseId) {
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        String url = createURL("core_enrol_get_enrolled_users");
        map.add("courseid", courseId);
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);
        ResponseEntity<MoodleUserDTO[]> response = restTemplate.postForEntity(url, request, MoodleUserDTO[].class);
        return Arrays.stream(response.getBody()).toList();
    }

    public boolean existsAnUserinThisCourse(String courseId, String email) {
        //TODO: We need to review this function - performance issue
        String lowerCaseEmailInput = email.toLowerCase();
        List<MoodleUserDTO> users = getUsersForCourse(courseId);
        List<MoodleUserDTO> filtered = users
                .stream()
                .filter(user -> {
                    String userEmail = user.getEmail();
                    String lowerCaseEmail = userEmail.toLowerCase();
                    return lowerCaseEmail.equals(lowerCaseEmailInput);
                })
                .collect(Collectors.toList());
        return !filtered.isEmpty();
    }

    private <T> T getFirst(ResponseEntity<T[]> response) {
        List<T> resultList = Arrays.stream(response.getBody()).toList();
        if(resultList.isEmpty()) {
            return null;
        }

        return resultList.get(0);
    }
    public MoodleUserDTO getUserByMail(String email) throws UnsupportedEncodingException {
        ResponseEntity<MoodleUserDTO[]> response = null;
        MoodleUserDTO result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        String url = createURL("core_user_get_users_by_field");
        map.add("field", "email");
        map.add("values[0]",  email);
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);

        try {
            response = restTemplate.postForEntity(url, request, MoodleUserDTO[].class);
            result = getFirst(response);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public MoodleUserDTO createUser(String name, String surname, String email) throws UnsupportedEncodingException {
        ResponseEntity<MoodleUserDTO[]> response = null;
        MoodleUserDTO result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        String url = createURL("core_user_create_users");
        Mail mail = new Mail(email);
        map.add("users[0][username]", mail.getUserName());
        map.add("users[0][createpassword]", "1");
        map.add("users[0][email]",  email);
        map.add("users[0][firstname]",name);
        map.add("users[0][lastname]", surname);
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);

        try {
            response = restTemplate.postForEntity(url, request, MoodleUserDTO[].class);
            result = getFirst(response);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }

    public void enroleToTheCourse(MoodleCourseDTO course, MoodleUserDTO user) {
        MoodleUserDTO result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        String url = createURL("enrol_manual_enrol_users");
        String roleId = STUDENT_ROLE_ID;
        String userId = user.getId();
        map.add("enrolments[0][roleid]", roleId);
        map.add("enrolments[0][userid]", userId);
        map.add("enrolments[0][courseid]=", course.getId()+"");
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }
    }

    public MoodleCourseDTO getCourse(String courseId) {
        ResponseEntity<MoodleCourseDTO[]> response = null;
        MoodleCourseDTO result = null;
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        String url = createURL("core_course_get_courses");
        map.add("options[ids][0]", courseId);
        HttpEntity<MultiValueMap<String, String>> request = createRequest(map);

        try {
            response = restTemplate.postForEntity(url, request, MoodleCourseDTO[].class);
            result = getFirst(response);
        } catch (Exception ex) {
            //TODO: Include log and Throw Exception
            String errorMessage = ex.getMessage();
        }

        return result;
    }
}
