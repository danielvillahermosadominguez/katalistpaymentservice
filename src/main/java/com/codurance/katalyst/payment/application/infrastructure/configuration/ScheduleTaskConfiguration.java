package com.codurance.katalyst.payment.application.infrastructure.configuration;

import com.codurance.katalyst.payment.application.actions.RetryPendingPayments;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.financial.FinancialService;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
public class ScheduleTaskConfiguration {
    @Bean
    public RetryPendingPayments scheduledJob(PaymentService paymentService,
                                             PurchaseService purchaseService,
                                             FinancialService financialService,
                                             LearningService learningService,
                                             AbstractLog log) {
        return new RetryPendingPayments(paymentService, purchaseService, financialService,learningService, log);
    }
}
