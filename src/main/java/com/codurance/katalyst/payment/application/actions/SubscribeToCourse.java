package com.codurance.katalyst.payment.application.actions;

import com.codurance.katalyst.payment.application.actions.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.actions.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.actions.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.actions.exception.UserIsEnroledInTheCourse;
import com.codurance.katalyst.payment.application.apirest.payment.dto.CustomerData;
import com.codurance.katalyst.payment.application.model.learning.LearningService;
import com.codurance.katalyst.payment.application.model.payment.PaymentService;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentTransaction;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.model.purchase.Purchase;
import com.codurance.katalyst.payment.application.model.purchase.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscribeToCourse {
    private final PaymentService paymentService;
    private final LearningService learningService;
    private final PurchaseService purchaseService;

    @Autowired
    public SubscribeToCourse(PaymentService paymentService,
                             LearningService learningService,
                             PurchaseService purchaseService) {
        this.paymentService = paymentService;
        this.learningService = learningService;
        this.purchaseService = purchaseService;
    }

    public PaymentStatus subscribe(CustomerData customerData) throws CourseNotExists, InvalidInputCustomerData, NoPriceAvailable, UserIsEnroledInTheCourse, LearningPlatformIsNotAvailable, FinancialPlatformIsNotAvailable, TPVTokenIsRequired, CreditCardNotValid {
        var course = learningService.getCourse(customerData.getCourseId());
        if (course == null) {
            throw new CourseNotExists();
        }
        if (!learningService.isThereASeatFor(course.getId() + "", customerData.getEmail())) {
            throw new UserIsEnroledInTheCourse();
        }

        var paymentTransaction = paymentService.authorizeTransaction(
                customerData.getIp(),
                customerData.getPaytpvToken()
        );

        if (paymentTransaction.getPaymentStatus() == null) {
            return null;
        }

        var purchase = createPurchase(
                paymentTransaction,
                course.getId() + "",
                customerData,
                false,
                false
        );
        purchaseService.save(purchase);
        return paymentTransaction.getPaymentStatus();
    }

    private Purchase createPurchase(PaymentTransaction paymentTransaction,
                                    String courseId,
                                    CustomerData customerData,
                                    boolean finantialStep,
                                    boolean learningStep) {
        return new Purchase(
                paymentTransaction.getId(),
                paymentTransaction.getOrder(),
                courseId,
                customerData.getEmail(),
                customerData.getName(),
                customerData.getSurname(),
                customerData.getDnicif(),
                customerData.getIsCompany(),
                customerData.getCompany(),
                customerData.getAddress(),
                customerData.getPostalCode(),
                customerData.getCity(),
                customerData.getRegion(),
                customerData.getCountry(),
                false,
                false
        );
    }

}
