package com.codurance.katalyst.payment.application.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfiguration {

    public static final int TIMEOUT_LIMIT = 3000;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(TIMEOUT_LIMIT))
                .setReadTimeout(Duration.ofMillis(TIMEOUT_LIMIT))
                .build();
    }
}