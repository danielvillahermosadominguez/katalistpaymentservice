package com.codurance.katalyst.payment.application.api;

import com.codurance.katalyst.payment.application.ports.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;
import com.codurance.katalyst.payment.application.usecases.CourseNotExists;
import com.codurance.katalyst.payment.application.usecases.HoldedIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.usecases.MoodleIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.NoPriceAvailable;
import com.codurance.katalyst.payment.application.usecases.SubscriptionUseCase;
import com.codurance.katalyst.payment.application.usecases.UserIsEnroledInTheCourse;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class PaymentControllerShould {
    private static final String ENDPOINT_HEALTHCHECK = "/healthcheck";
    private static final String ENDPOINT_SUBSCRIPTION = "/subscription";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MoodleApiClient moodleAPIClient;
    @MockBean
    private HoldedApiClient holdedAPIClient;
    @MockBean
    private SubscriptionUseCase useCase;

    @Test
    void return_Ok_200_when_healthcheck_is_called() throws Exception {
        var request = get(ENDPOINT_HEALTHCHECK);

        var result = this.mockMvc.perform(request);

        result.andDo(print()).andExpect(status().isOk());
        result.andExpect(content().string("OK! Working"));
    }

    @Test
    void return_Ok_200_when_subscribe_is_called_and_the_subscription_is_success() throws Exception {
        var customerData = new PotentialCustomerData();
        var gson = new Gson();
        var body = gson.toJson(customerData);
        var request = post(ENDPOINT_SUBSCRIPTION)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        var result = this.mockMvc.perform(request);

        result.andExpect(status().isOk());
    }

    @Test
    void return_bad_request_when_the_course_not_exist() throws Exception, CourseNotExists, HoldedIsNotAvailable, MoodleIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData {
        var customerData = new PotentialCustomerData();
        doThrow(CourseNotExists.class).when(this.useCase).subscribe(any());
        var gson = new Gson();
        var body = gson.toJson(customerData);
        var request = post(ENDPOINT_SUBSCRIPTION)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        var result = this.mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn();
        var json = result.getResponse().getContentAsString();
        var error = gson.fromJson(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.ERROR_CODE_COURSE_DOESNT_EXIST);
    }

    @Test
    void return_unprocesable_entity_when_the_customer_is_enroled() throws Exception, CourseNotExists, HoldedIsNotAvailable, MoodleIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData {
        var customerData = new PotentialCustomerData();
        doThrow(UserIsEnroledInTheCourse.class).when(this.useCase).subscribe(any());
        var gson = new Gson();
        var body = gson.toJson(customerData);
        var request = post(ENDPOINT_SUBSCRIPTION)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        var result = this.mockMvc.perform(request)
                .andExpect(
                        status().isUnprocessableEntity()
                )
                .andReturn();
        var json = result.getResponse().getContentAsString();
        var error = gson.fromJson(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_USER_HAS_ALREADY_A_SUSCRIPTION_TO_THIS_COURSE);
    }

    @Test
    void return_bad_request_when_the_price_is_not_available() throws Exception, CourseNotExists, HoldedIsNotAvailable, MoodleIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData {
        var customerData = new PotentialCustomerData();
        doThrow(NoPriceAvailable.class).when(this.useCase).subscribe(any());
        var gson = new Gson();
        var body = gson.toJson(customerData);
        var request = post(ENDPOINT_SUBSCRIPTION)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        var result = this.mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn();
        var json = result.getResponse().getContentAsString();
        var error = gson.fromJson(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_PRICE_NOT_FOUND);
    }

    @Test
    void return_general_error_when_the_customer_data_is_invalid() throws Exception, CourseNotExists, HoldedIsNotAvailable, MoodleIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData {
        var customerData = new PotentialCustomerData();
        doThrow(InvalidInputCustomerData.class).when(this.useCase).subscribe(any());
        var gson = new Gson();
        var body = gson.toJson(customerData);
        var request = post(ENDPOINT_SUBSCRIPTION)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        var result = this.mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn();
        var json = result.getResponse().getContentAsString();
        var error = gson.fromJson(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_GENERAL_SUBSCRIPTION);
    }

    @Test
    void return_general_error_when_the_moodle_is_not_available() throws Exception, CourseNotExists, HoldedIsNotAvailable, MoodleIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData {
        var customerData = new PotentialCustomerData();
        doThrow(MoodleIsNotAvailable.class).when(this.useCase).subscribe(any());
        var gson = new Gson();
        var body = gson.toJson(customerData);
        var request = post(ENDPOINT_SUBSCRIPTION)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        var result = this.mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn();
        var json = result.getResponse().getContentAsString();
        var error = gson.fromJson(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_GENERAL_SUBSCRIPTION);
    }

    @Test
    void return_general_error_when_the_holded_is_not_available() throws Exception, CourseNotExists, HoldedIsNotAvailable, MoodleIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData {
        var customerData = new PotentialCustomerData();
        doThrow(HoldedIsNotAvailable.class).when(this.useCase).subscribe(any());
        var gson = new Gson();
        var body = gson.toJson(customerData);
        var request = post(ENDPOINT_SUBSCRIPTION)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        var result = this.mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn();
        var json = result.getResponse().getContentAsString();
        var error = gson.fromJson(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_GENERAL_SUBSCRIPTION);
    }
}
