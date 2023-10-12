package com.codurance.katalyst.payment.application.apirest;

import com.codurance.katalyst.payment.application.model.learning.entity.Course;
import com.codurance.katalyst.payment.application.apirest.courses.CourseController;
import com.codurance.katalyst.payment.application.apirest.payment.dto.Error;
import com.codurance.katalyst.payment.application.actions.ObtainTheCourse;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CourseController.class)
public class CourseControllerShould {
    public static final String ID_COURSE_EXIST = "1";
    public static final String ID_COURSE_DOESNT_EXIST = "2";
    public static final String ID_COURSE_WITHOUT_PRICE = "3";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ObtainTheCourse obtainTheCourseUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() throws NoPriceAvailable, LearningPlatformIsNotAvailable {
        when(obtainTheCourseUseCase.getCourse(ID_COURSE_EXIST)).thenReturn(
                new Course(1,
                        "random_name",
                        50.1
                )
        );
        when(obtainTheCourseUseCase.getCourse(ID_COURSE_DOESNT_EXIST)).thenReturn(null);
        when(obtainTheCourseUseCase.getCourse(ID_COURSE_WITHOUT_PRICE)).thenThrow(NoPriceAvailable.class);
    }

    @Test
    void return_Ok_200_with_the_course_when_the_course_exists_success() throws Exception {
        var request = get("/courses/" + ID_COURSE_EXIST)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        var result = this.mockMvc.perform(request);

        result.andExpect(status().isOk());
    }

    @Test
    void return_bad_request_400_with_error_when_course_doesnt_exist() throws Exception {
        var request = get("/courses/" + ID_COURSE_DOESNT_EXIST)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        var result = this.mockMvc
                .perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn();

        var json = result.getResponse().getContentAsString();
        var error = objectMapper.readValue(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.ERROR_CODE_COURSE_DOESNT_EXIST);
    }

    @Test
    void return_bad_request_400_with_error_when_price_doesnt_exist() throws Exception {
        var request = get("/courses/" + ID_COURSE_WITHOUT_PRICE)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        var result = this.mockMvc
                .perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn();

        var json = result.getResponse().getContentAsString();
        var error = objectMapper.readValue(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_PRICE_NOT_FOUND);
    }

    @Test
    void return_bad_request_400_with_error_when_moodle_doesnt_respond() throws Exception, NoPriceAvailable, LearningPlatformIsNotAvailable {
        when(obtainTheCourseUseCase.getCourse("4")).thenThrow(LearningPlatformIsNotAvailable.class);
        var request = get("/courses/4")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        var result = this.mockMvc
                .perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn();

        var json = result.getResponse().getContentAsString();
        var error = objectMapper.readValue(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_GENERAL_SUBSCRIPTION);
    }
}
