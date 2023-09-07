package com.codurance.katalyst.payment.katalistpayment;

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
    public ResponseEntity freeSubscription(@RequestBody Customer customer) throws UnsupportedEncodingException {
        if (moodleAPIClient.existsAnUserinThisCourse(customer.getCourseId(), customer.getEmail())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The user has a subscription for this course");
        }

        MoodleUserDTO user = moodleAPIClient.getUser(customer.getEmail());
        if(user == null) {
            user = moodleAPIClient.createAnUser(customer.getName(), customer.getSurname(), customer.getEmail());
        }
        moodleAPIClient.subscribeUserToTheCourse(customer.getCourseId(), user);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
