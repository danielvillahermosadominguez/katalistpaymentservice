package com.codurance.katalyst.payment.application.infrastructure.configuration;

import com.codurance.katalyst.payment.application.common.requests.AbstractIpCatcher;
import com.codurance.katalyst.payment.application.infrastructure.requests.DevIpCatcher;
import com.codurance.katalyst.payment.application.infrastructure.requests.ServletRequestIpCatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IpCatcherConfiguration {
    @Bean
    @ConditionalOnProperty(value = "ipcatcher.devMode", matchIfMissing = true, havingValue = "false")
    public AbstractIpCatcher servletRequestIpCatcher() {
        return new ServletRequestIpCatcher();
    }


    @Bean
    @ConditionalOnProperty(value = "ipcatcher.devMode", matchIfMissing = false, havingValue = "true")
    public AbstractIpCatcher devIpCatcher() {
        return new DevIpCatcher();
    }
}
