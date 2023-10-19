package com.codurance.katalyst.payment.application.apirest;

import com.codurance.katalyst.payment.application.actions.CancelPayment;
import com.codurance.katalyst.payment.application.actions.ConfirmPayment;
import com.codurance.katalyst.payment.application.actions.SubscribeToCourse;
import com.codurance.katalyst.payment.application.actions.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.actions.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.actions.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.actions.exception.UserIsEnroledInTheCourse;
import com.codurance.katalyst.payment.application.apirest.dto.Error;
import com.codurance.katalyst.payment.application.apirest.dto.ErrorResponseFactory;
import com.codurance.katalyst.payment.application.apirest.payment.PaymentController;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.customer.CustomerData;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NoCustomerData;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.ports.holded.HoldedApiClient;
import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.model.ports.paycomet.exception.PayCometNotRespond;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@Import(ErrorResponseFactory.class)
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
    private SubscribeToCourse subscription;

    @MockBean
    private ConfirmPayment confirmPayment;

    @MockBean
    private CancelPayment cancelPayment;

    @MockBean
    private AbstractLog log;

    @Test
    void return_Ok_200_when_subscribe_is_called_and_the_subscription_is_success() throws Exception, CourseNotExists, FinancialPlatformIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid, PayCometNotRespond {
        var customerData = new CustomerData();
        var body = objectMapper.writeValueAsString(customerData);
        when(this.subscription.subscribe(any())).thenReturn(new PaymentStatus());
        var request = post(ENDPOINT_SUBSCRIPTION)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        var result = this.mockMvc.perform(request);

        result.andExpect(status().isOk());
    }

    @Test
    void return_Ok_200_when_a_payment_is_confirmed_and_accepted_by_the_customer() throws Exception, NotValidNotification, LearningPlatformIsNotAvailable, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData {
        var request = post("/confirmation")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("MethodId", "1")
                .param("TransactionType", "1")
                .param("Order", "RANDOM_ORDER")
                .param("TpvID", "12345")
                .param("Amount", "RANDOM_AMOUNT")
                .param("Response", "OK");

        var result = this.mockMvc.perform(request);

        result.andExpect(status().isOk());
        verify(confirmPayment, times(1)).confirm(any());
        verify(cancelPayment, never()).cancel(any());
    }

    @Test
    void return_Ok_200_when_a_payment_is_canceled_by_the_customer() throws Exception, NotValidNotification, LearningPlatformIsNotAvailable, NoCustomerData, FinancialPlatformIsNotAvailable, InvalidInputCustomerData {
        var request = post("/confirmation")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("MethodId", "1")
                .param("TransactionType", "1")
                .param("Order", "RANDOM_ORDER")
                .param("TpvID", "12345")
                .param("Amount", "RANDOM_AMOUNT")
                .param("Response", "KO");

        var result = this.mockMvc.perform(request);

        result.andExpect(status().isOk());
        verify(confirmPayment, never()).confirm(any());
        verify(cancelPayment, times(1)).cancel(any());
    }

    @Test
    void return_bad_request_when_the_course_not_exist() throws Exception, CourseNotExists, FinancialPlatformIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid, PayCometNotRespond {
        var customerData = new CustomerData();
        doThrow(CourseNotExists.class).when(this.subscription).subscribe(any());
        var body = objectMapper.writeValueAsString(customerData);
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

        var error = objectMapper.readValue(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.ERROR_CODE_COURSE_DOESNT_EXIST);
    }

    @Test
    void return_unprocesable_entity_when_the_customer_is_enroled() throws Exception, CourseNotExists, FinancialPlatformIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid, PayCometNotRespond {
        var customerData = new CustomerData();
        doThrow(UserIsEnroledInTheCourse.class).when(this.subscription).subscribe(any());
        var body = objectMapper.writeValueAsString(customerData);
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
        var error = objectMapper.readValue(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_USER_HAS_ALREADY_A_SUSCRIPTION_TO_THIS_COURSE);
    }

    @Test
    void return_bad_request_when_the_price_is_not_available() throws Exception, CourseNotExists, FinancialPlatformIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid, PayCometNotRespond {
        var customerData = new CustomerData();
        doThrow(NoPriceAvailable.class).when(this.subscription).subscribe(any());
        var body = objectMapper.writeValueAsString(customerData);
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
        var error = objectMapper.readValue(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_PRICE_NOT_FOUND);
    }

    @Test
    void return_general_error_when_the_customer_data_is_invalid() throws Exception, CourseNotExists, FinancialPlatformIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid, PayCometNotRespond {
        var customerData = new CustomerData();
        doThrow(InvalidInputCustomerData.class).when(this.subscription).subscribe(any());
        var body = objectMapper.writeValueAsString(customerData);
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
        var error = objectMapper.readValue(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_GENERAL_SUBSCRIPTION);
    }

    @Test
    void return_general_error_when_the_moodle_is_not_available() throws Exception, CourseNotExists, FinancialPlatformIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid, PayCometNotRespond {
        var customerData = new CustomerData();
        doThrow(LearningPlatformIsNotAvailable.class).when(this.subscription).subscribe(any());
        var body = objectMapper.writeValueAsString(customerData);
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
        var error = objectMapper.readValue(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_GENERAL_SUBSCRIPTION);
    }

    @Test
    void return_general_error_when_the_holded_is_not_available() throws Exception, CourseNotExists, FinancialPlatformIsNotAvailable, LearningPlatformIsNotAvailable, NoPriceAvailable, UserIsEnroledInTheCourse, InvalidInputCustomerData, TPVTokenIsRequired, CreditCardNotValid, PayCometNotRespond {
        var customerData = new CustomerData();
        doThrow(FinancialPlatformIsNotAvailable.class).when(this.subscription).subscribe(any());
        var body = objectMapper.writeValueAsString(customerData);
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
        var error = objectMapper.readValue(json, Error.class);
        assertThat(error.getCode()).isEqualTo(Error.CODE_ERROR_GENERAL_SUBSCRIPTION);
    }
}
