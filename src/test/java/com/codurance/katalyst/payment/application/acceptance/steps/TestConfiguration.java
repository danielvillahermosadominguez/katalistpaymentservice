package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.doubles.HoldedApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.MoodleApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.PayCometApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.TestDateService;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;
import com.codurance.katalyst.payment.application.ports.PayCometApiClient;
import com.codurance.katalyst.payment.application.utils.DateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfiguration {
    private MoodleApiClientFake moodleApiClientFake = new MoodleApiClientFake();

    private HoldedApiClientFake holdedApiClientFake = new HoldedApiClientFake();

    private PayCometApiClientFake payCometApiClientFake = new PayCometApiClientFake();

    @Bean
    @Primary
    public DateService getTestingDateService() {
        return new TestDateService();
    }

    @Bean
    @Primary
    public MoodleApiClient getMoodleApiClient() {
        return moodleApiClientFake;
    }

    @Bean
    @Primary
    public HoldedApiClientFake getHoldedApiClient() {
        return holdedApiClientFake;
    }

    @Bean
    @Primary
    public PayCometApiClient getPayCometApiClient() {
        return payCometApiClientFake;
    }

}
