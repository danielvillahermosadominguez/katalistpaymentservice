package com.codurance.katalyst.payment.application.apirest.payment;

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
import com.codurance.katalyst.payment.application.apirest.payment.dto.Error;
import com.codurance.katalyst.payment.application.model.customer.CustomerData;
import com.codurance.katalyst.payment.application.model.payment.entity.PaymentNotification;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NoCustomerData;
import com.codurance.katalyst.payment.application.model.payment.exceptions.NotValidNotification;
import com.codurance.katalyst.payment.application.model.ports.paycomet.dto.PaymentStatus;
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
    private ObjectMapper objectMapper;
    @RequestMapping(value = "/confirmation", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity webhook(@RequestParam Map<String, Object> payload, HttpServletRequest request)  {
        //we could check the IPs
        //99.81.26.182
        //99.80.172.90
        //99.81.94.3
        //TODO: https://docs.paycomet.com/en/inicio/seguimiento
        try {
            var json = objectMapper.writeValueAsString(payload);
            var paymentNotification = objectMapper.readValue(json, PaymentNotification.class);
            confirmPayment.confirm(paymentNotification);
        } catch (NotValidNotification e) {
            return new ResponseEntity<>(
                    new Error(Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                            "The payload is not correct for this service"),
                    HttpStatus.BAD_REQUEST
            );
            //TODO: Include log
        } catch (NoCustomerData e) {
            return new ResponseEntity<>(
                    new Error(Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                            "We don't have any customer data related to this transaction"),
                    HttpStatus.BAD_REQUEST
            );
            //TODO: Include log
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(
                    new Error(Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                            "We have had problems with the format of the payload"),
                    HttpStatus.BAD_REQUEST
            );
            //TODO: Include log
        } catch (FinancialPlatformIsNotAvailable e) {
            //TODO: manage this exception and return an error
            throw new RuntimeException(e);
        } catch (InvalidInputCustomerData e) {
            //TODO: manage this exception and return an error
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("");
    }

    @RequestMapping(value = "/subscription", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity subscription(@RequestBody CustomerData customer, HttpServletRequest request) {
        PaymentStatus paymentStatus = null;
        try {
            paymentStatus = this.subscription.subscribe(customer);
            if(paymentStatus == null) {
                return new ResponseEntity<>(
                        new Error(Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                                "We have had a problem with the payment"),
                        HttpStatus.BAD_REQUEST
                );
            }
        } catch (InvalidInputCustomerData | FinancialPlatformIsNotAvailable | LearningPlatformIsNotAvailable exception) {
            return new ResponseEntity<>(
                    new Error(Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                            "We have had a problem with the creation of the contact and the invoicing"),
                    HttpStatus.BAD_REQUEST
            );
        } catch (NoPriceAvailable exception) {
            return new ResponseEntity<>(
                    new Error(
                            Error.CODE_ERROR_PRICE_NOT_FOUND,
                            "Price custom field not found in Moodle. Please, contact with the administrator to create this custom field"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        } catch (UserIsEnroledInTheCourse exception) {
            return new ResponseEntity<>(
                    new Error(
                            Error.CODE_ERROR_USER_HAS_ALREADY_A_SUSCRIPTION_TO_THIS_COURSE,
                            "The user has a subscription for this course"
                    ),
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        } catch (CourseNotExists exception) {
            return new ResponseEntity<>(
                    new Error(
                            Error.ERROR_CODE_COURSE_DOESNT_EXIST,
                            "The course with the id " + customer.getCourseId() + " doesn't exists"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        } catch (TPVTokenIsRequired e) {
            throw new RuntimeException(e);
        } catch (CreditCardNotValid e) {
            throw new RuntimeException(e);
        }

        return new ResponseEntity<>(paymentStatus,
                HttpStatus.OK
        );
    }
}
