package com.codurance.katalyst.payment.katalistpayment;

import com.codurance.katalyst.payment.katalistpayment.courses.DBCourse;
import com.codurance.katalyst.payment.katalistpayment.courses.DBCourseRepository;
import com.codurance.katalyst.payment.katalistpayment.holded.HoldedAPIClient;
import com.codurance.katalyst.payment.katalistpayment.holded.HoldedContactDTO;
import com.codurance.katalyst.payment.katalistpayment.holded.HoldedInvoiceDTO;
import com.codurance.katalyst.payment.katalistpayment.inputform.PotentialCustomerData;
import com.codurance.katalyst.payment.katalistpayment.moodle.MoodleAPIClient;
import com.codurance.katalyst.payment.katalistpayment.moodle.MoodleCourseDTO;
import com.codurance.katalyst.payment.katalistpayment.moodle.MoodleUserDTO;
import com.codurance.katalyst.payment.katalistpayment.courses.Course;
import com.codurance.katalyst.payment.katalistpayment.responses.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

@RestController
public class PaymentController {

    @Autowired
    private MoodleAPIClient moodleAPIClient;

    @Autowired
    private HoldedAPIClient holdedAPIClient;

    @Autowired
    private DBCourseRepository courseRepository;

    @GetMapping("/healthcheck")
    public String heatlhCheck() {
        return String.format("OK! Working");
    }

    @GetMapping(value = "/courses/{id}")
    @ResponseBody
    public ResponseEntity<?> getCourse(@PathVariable("id") String id) {
        MoodleCourseDTO course = moodleAPIClient.getCourse(id);
        if(course == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The course with the id " + id + " doesn't exists" );
        }

        double price = getPriceFromDB(id);
        course.setPrice(price);
        return new ResponseEntity<>(new Course(course.getId(), course.getDisplayname(),66.99), HttpStatus.OK);
    }

    private double getPriceFromDB(String id) {
        Long idNumber = Long.parseLong(id);
        Optional<DBCourse> dbCourse = courseRepository.findByCourseId(idNumber);
        if(!dbCourse.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "We don't have available to subscriptions the course with the id " + id );
        }
        return dbCourse.get().getPrice();
    }

    @RequestMapping(value = "/freesubscription", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity freeSubscription(@RequestBody PotentialCustomerData customer) throws UnsupportedEncodingException {
        MoodleCourseDTO course = moodleAPIClient.getCourse(customer.getCourseId());
        if(course == null) {
            return new ResponseEntity<>(new Error(1,"The course with the id " + customer.getCourseId() + " doesn't exists"), HttpStatus.BAD_REQUEST);
        }

        if (moodleAPIClient.existsAnUserinThisCourse(customer.getCourseId(), customer.getEmail())) {
            return new ResponseEntity<>(new Error(2,"The user has a subscription for this course"), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        MoodleUserDTO user = moodleAPIClient.getUserByMail(customer.getEmail());
        if(user == null) {
            user = moodleAPIClient.createUser(customer.getName(), customer.getSurname(), customer.getEmail());
        }
        moodleAPIClient.enroleToTheCourse(course, user);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @RequestMapping(value = "/invoicing", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity onlyHoldedTest(@RequestBody PotentialCustomerData customer) throws UnsupportedEncodingException {
        try {
            MoodleCourseDTO course = moodleAPIClient.getCourse(customer.getCourseId());
            if(course == null) {
                return new ResponseEntity<>(new Error(1,"The course with the id " + customer.getCourseId() + " doesn't exists"), HttpStatus.BAD_REQUEST);
            } else {
                double price = getPriceFromDB(customer.getCourseId());
                course.setPrice(price);
            }

            String customId = holdedAPIClient.createCustomId(customer.getDnicif(), customer.getEmail());
            HoldedContactDTO contact = holdedAPIClient.getContactByCustomId(customId);
            if (contact == null) {
                contact = holdedAPIClient.createContact(customer.getName(),
                        customer.getSurname(),
                        customer.getEmail(),
                        customer.getCompany(),
                        customer.getDnicif());
            }

            String concept = course.getDisplayname();
            String description =  "";
            int amount = 1;
            double price = course.getPrice();
            HoldedInvoiceDTO invoice = holdedAPIClient.createInvoice(contact,
                    concept,
                    description,
                    amount,
                    price);
            holdedAPIClient.sendInvoice(invoice, contact.getEmail());
        } catch (Exception ex) {
            return new ResponseEntity<>(new Error(3,"We have had a problem with the creation of the contact and the invoicing"), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
