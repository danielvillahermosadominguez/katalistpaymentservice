package com.codurance.katalyst.payment.application.api;

import com.codurance.katalyst.payment.application.holded.dto.HoldedInvoice;
import com.codurance.katalyst.payment.application.holded.exception.HoldedNotRespond;
import com.codurance.katalyst.payment.application.moodle.exception.CustomFieldNotExists;
import com.codurance.katalyst.payment.application.moodle.exception.MoodleNotRespond;
import com.codurance.katalyst.payment.application.ports.HoldedApiClient;
import com.codurance.katalyst.payment.application.ports.MoodleApiClient;
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
                            course.getPrice()),
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

    @RequestMapping(value = "/freesubscription", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity freeSubscription(@RequestBody PotentialCustomerData customer) {
        try {
            var course = moodleAPIClient.getCourse(customer.getCourseId());
            if (course == null) {
                return new ResponseEntity<>(
                        new Error(
                                Error.ERROR_CODE_COURSE_DOESNT_EXIST,
                                "The course with the id " + customer.getCourseId() + " doesn't exists"
                        ),
                        HttpStatus.BAD_REQUEST
                );
            }

            if (moodleAPIClient.existsAnUserinThisCourse(customer.getCourseId(), customer.getEmail())) {
                return new ResponseEntity<>(
                        new Error(
                                Error.CODE_ERROR_USER_HAS_ALREADY_A_SUSCRIPTION_TO_THIS_COURSE,
                                "The user has a subscription for this course"
                        ),
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }

            var user = moodleAPIClient.getUserByMail(customer.getEmail());
            if (user == null) {
                user = moodleAPIClient.createUser(customer.getName(), customer.getSurname(), customer.getEmail());
            }
            moodleAPIClient.enrolToTheCourse(course, user);
            return ResponseEntity.ok(HttpStatus.OK);
        } catch (MoodleNotRespond e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "It has been not possible to get the course. Please try to connect later"
            );
        }
    }

    @RequestMapping(value = "/invoicing", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity onlyHoldedTest(@RequestBody PotentialCustomerData customer) {
        try {
            var course = moodleAPIClient.getCourse(customer.getCourseId());
            if (course == null) {
                return new ResponseEntity<>(
                        new Error(
                                Error.ERROR_CODE_COURSE_DOESNT_EXIST,
                                "The course with the id " + customer.getCourseId() + " doesn't exists"),
                        HttpStatus.BAD_REQUEST
                );
            }

            var customId = holdedAPIClient.createCustomId(customer.getDnicif(), customer.getEmail());
            var contact = holdedAPIClient.getContactByCustomId(customId);
            if (contact == null) {
                contact = holdedAPIClient.createContact(customer.getName(),
                        customer.getSurname(),
                        customer.getEmail(),
                        customer.getCompany(),
                        customer.getDnicif());
            }

            var concept = course.getDisplayname();
            var description = "";
            var amount = Error.ERROR_CODE_COURSE_DOESNT_EXIST;
            var price = course.getPrice();
            HoldedInvoice invoice = holdedAPIClient.createInvoice(contact,
                    concept,
                    description,
                    amount,
                    price);
            holdedAPIClient.sendInvoice(invoice, contact.getEmail());
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    new Error(
                            Error.CODE_ERROR_PROBLEM_WITH_MOODLE,
                            "We have had a problem with the creation of the contact and the invoicing"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        } catch (CustomFieldNotExists exception) {
            return new ResponseEntity<>(
                    new Error(
                            Error.CODE_ERROR_PRICE_NOT_FOUND,
                            "Price custom field not found in Moodle. Please, contact with the administrator to create this custom field"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        } catch (MoodleNotRespond exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "It has been not possible to get the course. Please try to connect later"
            );
        } catch (HoldedNotRespond exception) {
            return new ResponseEntity<>(
                    new Error(
                            Error.CODE_ERROR_PROBLEM_WITH_MOODLE,
                            "We have had a problem with the creation of the contact and the invoicing"
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @RequestMapping(value = "/subscription", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity subscription(@RequestBody PotentialCustomerData customer) {
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
