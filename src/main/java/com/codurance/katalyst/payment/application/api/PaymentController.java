package com.codurance.katalyst.payment.application.api;

import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.ports.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;
import com.codurance.katalyst.payment.application.usecases.SubscriptionUseCase;
import com.codurance.katalyst.payment.application.usecases.exception.CourseNotExists;
import com.codurance.katalyst.payment.application.usecases.exception.CreditCardNotValid;
import com.codurance.katalyst.payment.application.usecases.exception.HoldedIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.InvalidInputCustomerData;
import com.codurance.katalyst.payment.application.usecases.exception.MoodleIsNotAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.NoPriceAvailable;
import com.codurance.katalyst.payment.application.usecases.exception.TPVTokenIsRequired;
import com.codurance.katalyst.payment.application.usecases.exception.UserIsEnroledInTheCourse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PaymentController {
    @Autowired
    private MoodleApiClient moodleAPIClient;

    @Autowired
    private HoldedApiClient holdedAPIClient;

    @Autowired
    private SubscriptionUseCase useCase;

    @GetMapping("/healthcheck")
    public String heatlhCheck() {
        return "OK! Working";
    }

    @GetMapping(value = "/courses/{id}")
    @ResponseBody
    public ResponseEntity<?> getCourse(@PathVariable("id") String id) {
        try {
            var course = moodleAPIClient.getCourse(id);
            if (course == null) {
                return new ResponseEntity<>(
                        new Error(
                                Error.ERROR_CODE_COURSE_DOESNT_EXIST,
                                "The course with the id " + id + " doesn't exists"
                        ),
                        HttpStatus.BAD_REQUEST
                );
            }

            return new ResponseEntity<>(
                    new Course(
                            course.getId(),
                            course.getDisplayname(),
                            course.getPrice().getValue()),
                    HttpStatus.OK
            );
        } catch (CustomFieldNotExists exception) {
            return new ResponseEntity<>(
                    new Error(
                            Error.CODE_ERROR_PRICE_NOT_FOUND,
                            "Price custom field not found in Moodle. Please, contact with the administrator to create this custom field"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "It has been not possible to get the course. Please try to connect later"
            );
        } catch (MoodleNotRespond exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "It has been not possible to get the course. Please try to connect later"
            );
        }
    }

    @RequestMapping(value = "/subscription", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity subscription(@RequestBody PotentialCustomerData customer, HttpServletRequest request) {
        PaymentStatus paymentStatus = null;
        try {
            paymentStatus = this.useCase.subscribe(customer);
            if(paymentStatus == null) {
                return new ResponseEntity<>(
                        new Error(Error.CODE_ERROR_GENERAL_SUBSCRIPTION,
                                "We have had a problem with the payment"),
                        HttpStatus.BAD_REQUEST
                );
            }
        } catch (InvalidInputCustomerData|HoldedIsNotAvailable|MoodleIsNotAvailable exception) {
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
