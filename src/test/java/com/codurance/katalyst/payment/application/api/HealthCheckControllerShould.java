package com.codurance.katalyst.payment.application.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthCheckController.class)
public class HealthCheckControllerShould {
    private static final String ENDPOINT_HEALTHCHECK = "/healthcheck";
    @Autowired
    private MockMvc mockMvc;

    @Test
    void return_Ok_200_when_healthcheck_is_called() throws Exception {
        var request = get(ENDPOINT_HEALTHCHECK);

        var result = mockMvc.perform(request);

        result.andDo(print()).andExpect(status().isOk());
        result.andExpect(content().string("OK! Working"));
    }
}
