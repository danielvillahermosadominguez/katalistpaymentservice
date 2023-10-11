package com.codurance.katalyst.payment.application.api;

import com.codurance.katalyst.payment.application.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.ports.holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.usecases.ConfirmPaymentUseCase;
import com.codurance.katalyst.payment.application.usecases.NotValidNotification;
import com.codurance.katalyst.payment.application.usecases.SubscriptionUseCase;
import com.codurance.katalyst.payment.application.usecases.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.usecases.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.usecases.exception.HoldedIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.usecases.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.usecases.exception.UserIsEnroledInTheCourse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
public class PaymentControllerShould {

    private static final String ENDPOINT_SUBSCRIPTION = "/subscription";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MoodleApiClient moodleAPIClient;
    @MockBean
    private HoldedApiClient holdedAPIClient;
    @MockBean
    private SubscriptionUseCase subscription;

    @MockBean
    private ConfirmPaymentUseCase confirmPayment;

    @Test
    void return_Ok_200_when_subscribe_is_called_and_the_subscription_is_success() throws Exception, CourseNotExists, HoldedIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid {
        var customerData = new CustomerData();
        var gson = new Gson();
        var body = gson.toJson(customerData);
        when(this.subscription.subscribe(any())).thenReturn(new PaymentStatus());
        var request = post(ENDPOINT_SUBSCRIPTION)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        var result = this.mockMvc.perform(request);

        result.andExpect(status().isOk());
    }

    @Test
    void return_Ok_200_when_a_payment_is_confirmed_and_accepted_by_the_customer() throws Exception, NotValidNotification {
        var request = post("/confirmation")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("MethodId","1")
                .param("TransactionType","1")
                .param("Order","RANDOM_ORDER")
                .param("TpvID","12345")
                .param("Amount","RANDOM_AMOUNT")
                .param("Response","OK");

        var result = this.mockMvc.perform(request);

        result.andExpect(status().isOk());
    }

    @Test
    void return_bad_request_when_the_course_not_exist() throws Exception, CourseNotExists, HoldedIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid {
        var customerData = new CustomerData();
        doThrow(CourseNotExists.class).when(this.subscription).subscribe(any());
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
    void return_unprocesable_entity_when_the_customer_is_enroled() throws Exception, CourseNotExists, HoldedIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid {
        var customerData = new CustomerData();
        doThrow(UserIsEnroledInTheCourse.class).when(this.subscription).subscribe(any());
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
    void return_bad_request_when_the_price_is_not_available() throws Exception, CourseNotExists, HoldedIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid {
        var customerData = new CustomerData();
        doThrow(NoPriceAvailable.class).when(this.subscription).subscribe(any());
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
    void return_general_error_when_the_customer_data_is_invalid() throws Exception, CourseNotExists, HoldedIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid {
        var customerData = new CustomerData();
        doThrow(InvalidInputCustomerData.class).when(this.subscription).subscribe(any());
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
    void return_general_error_when_the_moodle_is_not_available() throws Exception, CourseNotExists, HoldedIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid {
        var customerData = new CustomerData();
        doThrow(LearningPlatformIsNotAvailable.class).when(this.subscription).subscribe(any());
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
    void return_general_error_when_the_holded_is_not_available() throws Exception, CourseNotExists, HoldedIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid {
        var customerData = new CustomerData();
        doThrow(HoldedIsNotAvailable.class).when(this.subscription).subscribe(any());
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
