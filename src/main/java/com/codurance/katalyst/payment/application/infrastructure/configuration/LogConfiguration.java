package com.codurance.katalyst.payment.application.infrastructure.configuration;

import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.common.logs.AbstractLogFactory;
import com.codurance.katalyst.payment.application.infrastructure.logs.AbstractLogFactorySlf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfiguration {
    @Bean
    public AbstractLogFactory getLogFactory() {
        return new AbstractLogFactorySlf4j();
    }

    @Bean
    public AbstractLog getLog(AbstractLogFactory getLogFactory) {
        return getLogFactory.getLogger(AbstractLog.class);
    }
}
