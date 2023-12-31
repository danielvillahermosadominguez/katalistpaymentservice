package com.codurance.katalyst.payment.application.acceptance.steps;

import com.codurance.katalyst.payment.application.acceptance.doubles.ClockStub;
import com.codurance.katalyst.payment.application.acceptance.doubles.HoldedApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.MoodleApiClientFake;
import com.codurance.katalyst.payment.application.acceptance.doubles.PayCometApiClientFake;
import com.codurance.katalyst.payment.application.model.ports.clock.Clock;
import com.codurance.katalyst.payment.application.model.ports.moodle.MoodleApiClient;
import com.codurance.katalyst.payment.application.model.ports.paycomet.PayCometApiClient;
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
    public Clock getTestingDateService() {
        return new ClockStub();
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
