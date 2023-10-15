package com.codurance.katalyst.payment.application.actions;

import com.codurance.katalyst.payment.application.actions.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.actions.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.actions.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.actions.exception.UserIsEnroledInTheCourse;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.customer.CustomerData;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.learning.entity.Course;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.model.ports.paycomet.exception.PayCometNotRespond;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscribeToCourse {
    private final PaymentService paymentService;
    private final LearningService learningService;
    private final PurchaseService purchaseService;
    private final AbstractLog log;

    @Autowired
    public SubscribeToCourse(PaymentService paymentService,
                             LearningService learningService,
                             PurchaseService purchaseService,
                             AbstractLog log) {
        this.paymentService = paymentService;
        this.learningService = learningService;
        this.purchaseService = purchaseService;
        this.log = log;
    }

    public PaymentStatus subscribe(CustomerData customerData) throws CourseNotExists, InvalidInputCustomerData, NoPriceAvailable, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, FinancialPlatformIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid, PayCometNotRespond {
        var course = learningService.getCourse(customerData.getCourseId());
        if (course == null) {
            throw new CourseNotExists();
        }
        if (!learningService.isThereASeatFor(course.getId() + "", customerData.getEmail())) {
            refusedSubscription(customerData);
            throw new UserIsEnroledInTheCourse();
        }

        var paymentTransaction = paymentService.authorizeTransaction(
                customerData.getIp(),
                customerData.getPaytpvToken(),
                course.getPrice()
        );

        if (paymentTransaction.getPaymentStatus() == null) {
            return null;
        }

        var purchase = createPurchase(
                paymentTransaction,
                course,
                customerData,
                false,
                false
        );
        purchaseService.save(purchase);
        return paymentTransaction.getPaymentStatus();
    }

    private Purchase createPurchase(PaymentTransaction paymentTransaction,
                                    Course course,
                                    CustomerData customerData,
                                    boolean finantialStep,
                                    boolean learningStep) {
        return new Purchase(
                paymentTransaction.getId(),
                paymentTransaction.getOrder(),
                course.getId()+"",
                course.getName(),
                "",
                course.getPrice(),
                customerData.getEmail(),
                customerData.getName(),
                customerData.getSurname(),
                customerData.getDnicif(),
                customerData.getIsCompany(),
                customerData.getCompany(),
                customerData.getPhoneNumber(),
                customerData.getAddress(),
                customerData.getPostalCode(),
                customerData.getCity(),
                customerData.getRegion(),
                customerData.getCountry(),
                false,
                false
        );
    }

    private void refusedSubscription(CustomerData customerData) {
        try {
            var objectMapper = new ObjectMapper();
            var json = objectMapper.writeValueAsString(customerData);
            log.warn(SubscribeToCourse.class,
                    String.format(
                            "[SUBSCRIPTION FAIL] An user has tried to subscribe a course where he/she is already enrolled. Customer Data : %s",
                            json)
            );
        } catch (JsonProcessingException e) {
            log.warn(SubscribeToCourse.class,
                    "[SUBSCRIPTION FAIL] An user has tried to subscribe a course where he/she is already enrolled. Not serializable Customer Data"
            );
        }
    }
}
