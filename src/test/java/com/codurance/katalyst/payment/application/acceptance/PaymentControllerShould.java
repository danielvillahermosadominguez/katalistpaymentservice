package com.codurance.katalyst.payment.application.acceptance;

import com.codurance.katalyst.payment.application.api.PotentialCustomerData;
import com.codurance.katalyst.payment.application.ports.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;
import com.google.gson.Gson;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class PaymentControllerShould {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MoodleApiClient moodleAPIClient;

    @MockBean
    private HoldedApiClient holdedAPIClient;

    private static final String ENDPOINT_HEALTHCHECK = "/healthcheck";
    private static final String ENDPOINT_SUBSCRIPTION = "/subscription";

    @Test
    void return_Ok_200_when_healhcheck_is_called() throws Exception {
        var request = get(ENDPOINT_HEALTHCHECK);

        var result = this.mockMvc.perform(request);

        result.andDo(print()).andExpect(status().isOk());
        result.andExpect(content().string("OK! Working"));
    }

    @Test
    @Disabled("disabled because outside-in development")
    void return_Ok_200_when_subscribe_is_called_and_the_subscription_is_success() throws Exception {
        PotentialCustomerData customerData = new PotentialCustomerData();
        var gson = new Gson();
        var body = gson.toJson(customerData);
        var request = post(ENDPOINT_SUBSCRIPTION)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);

        var result = this.mockMvc.perform(request);

        result.andExpect(status().isOk());
    }
}
