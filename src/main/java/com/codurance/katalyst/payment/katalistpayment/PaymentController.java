package com.codurance.katalyst.payment.katalistpayment;

import com.codurance.katalyst.payment.katalistpayment.inputform.PotentialCustomerData;
import com.codurance.katalyst.payment.katalistpayment.moodle.MoodleAPIClient;
import com.codurance.katalyst.payment.katalistpayment.moodle.MoodleCourseDTO;
import com.codurance.katalyst.payment.katalistpayment.moodle.MoodleUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;

@RestController
public class PaymentController {

    @Autowired
    private MoodleAPIClient moodleAPIClient;

    @GetMapping("/healthcheck")
    public String heatlhCheck() {
        return String.format("OK! Working");
    }
    @RequestMapping(value = "/freesubscription", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity freeSubscription(@RequestBody PotentialCustomerData customer) throws UnsupportedEncodingException {
        MoodleCourseDTO course = moodleAPIClient.getCourse(customer.getCourseId());
        if(course == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The course with the id " + customer.getCourseId() + " doesn't exists" );
        }

        if (moodleAPIClient.existsAnUserinThisCourse(customer.getCourseId(), customer.getEmail())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "The user has a subscription for this course");
        }

        MoodleUserDTO user = moodleAPIClient.getUserByMail(customer.getEmail());
        if(user == null) {
            user = moodleAPIClient.createAnUser(customer.getName(), customer.getSurname(), customer.getEmail());
        }
        moodleAPIClient.subscribeUserToTheCourse(course, user);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
