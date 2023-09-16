package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.MoodleApiClient;
import com.codurance.katalyst.payment.application.acceptance.utils.MoodleApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.utils.TestDateService;
import com.codurance.katalyst.payment.application.utils.DateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfiguration {
    private MoodleApiClientFake moodleApiClientFake = new MoodleApiClientFake();

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

}
