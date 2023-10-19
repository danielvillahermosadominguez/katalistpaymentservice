package com.codurance.katalyst.payment.application.apirest.payment;

import com.codurance.katalyst.payment.application.actions.CancelPayment;
import com.codurance.katalyst.payment.application.actions.ConfirmPayment;
import com.codurance.katalyst.payment.application.actions.SubscribeToCourse;
import com.codurance.katalyst.payment.application.actions.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.actions.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.actions.exception.FinancialPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.actions.exception.LearningPlatformIsNotAvailable;
import com.codurance.katalyst.payment.application.actions.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.actions.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.actions.exception.UserIsEnroledInTheCourse;
import com.codurance.katalyst.payment.application.apirest.dto.Error;
import com.codurance.katalyst.payment.application.apirest.dto.ErrorResponseFactory;
import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.model.customer.CustomerData;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NoCustomerData;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.model.ports.paycomet.exception.PayCometNotRespond;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentController {
    @Autowired
    private SubscribeToCourse subscription;

    @Autowired
    private ConfirmPayment confirmPayment;

    @Autowired
    private CancelPayment cancelPayment;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AbstractLog log;

    @Autowired
    private ErrorResponseFactory responseFactory;

    @RequestMapping(value = "/confirmation", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity webhook(@RequestParam Map<String, Object> payload, HttpServletRequest request) {
        //we could check the IPs
        //99.81.26.182
        //99.80.172.90
        //99.81.94.3
        //TODO: https://docs.paycomet.com/en/inicio/seguimiento
        try {
            var json = objectMapper.writeValueAsString(payload);
            var paymentNotification = objectMapper.readValue(json, PaymentNotification.class);
            //IMPORTANT: The way to know if the response is Cancelled or OK is with the Response which is an String
            //In the documentation, there is not any indication about the possibles values https://docs.paycomet.com/en/inicio/seguimiento
            //There is only one example in the documentation with OK
            //The ko value has been gotten with the exploration of the notification when is cancel
            //We should ask to paycomet about if it is the best way to discrimitate between a cancelled or Accepted payment
            if (paymentNotification.isOKResponse()) {
                confirmPayment.confirm(paymentNotification);
            }
            if (paymentNotification.isKOResponse()) {
                cancelPayment.cancel(paymentNotification);
            }
        } catch (NotValidNotification e) {
            return responseFactory.createBadRequest(
                    Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                    "The payload is not correct for this service"
            );
        } catch (NoCustomerData e) {
            return responseFactory.createBadRequest(
                    Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                    "We don't have any customer data related to this transaction");
        } catch (JsonProcessingException e) {
            return responseFactory.createBadRequest(
                    Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                    "We have had problems with the format of the payload"
            );
        } catch (InvalidInputCustomerData | FinancialPlatformIsNotAvailable | LearningPlatformIsNotAvailable e) {
            return responseFactory.createBadRequest(
                    Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                    "We have had a problem with the creation of the contact and the invoicing"
            );
        } catch (Exception e) {
            log.error(PaymentController.class, "Not controlled exception. error: " + e.getMessage());
        }

        return ResponseEntity.ok("");
    }

    @RequestMapping(value = "/subscription", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity subscription(@RequestBody CustomerData customer, HttpServletRequest request) {
        PaymentStatus paymentStatus = null;
        try {
            paymentStatus = this.subscription.subscribe(customer);
            if (paymentStatus == null) {
                return responseFactory.createBadRequest(
                        Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                        "We have had a problem with the payment"
                );
            }
        } catch (InvalidInputCustomerData | FinancialPlatformIsNotAvailable | LearningPlatformIsNotAvailable exception) {
            return responseFactory.createBadRequest(
                    Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                    "We have had a problem with the creation of the contact and the invoicing"
            );
        } catch (NoPriceAvailable exception) {
            return responseFactory.createBadRequest(
                    Error.CODE_ERROR_PRICE_NOT_FOUND,
                    "Price custom field not found in Moodle. Please, contact with the administrator to create this custom field"
            );
        } catch (UserIsEnroledInTheCourse exception) {
            return responseFactory.createUnprocessableRequest(
                    Error.CODE_ERROR_USER_HAS_ALREADY_A_SUSCRIPTION_TO_THIS_COURSE,
                    "The user has a subscription for this course"
            );
        } catch (CourseNotExists exception) {
            return responseFactory.createBadRequest(
                    Error.ERROR_CODE_COURSE_DOESNT_EXIST,
                    "The course with the id " + customer.getCourseId() + " doesn't exists"
            );
        } catch (CreditCardNotValid | TPVTokenIsRequired | PayCometNotRespond e) {
            return responseFactory.createBadRequest(
                    Error.ERROR_PAYMENT_PLATFORM_CANNOT_TO_PROCESS_THIS_CREDIT_CARD,
                    "The payment platform cannot to process this credit card. Payment platform error: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error(PaymentController.class, "Not controlled exception. error: " + e.getMessage());
        }

        return new ResponseEntity<>(paymentStatus,
                HttpStatus.OK
        );
    }
}
