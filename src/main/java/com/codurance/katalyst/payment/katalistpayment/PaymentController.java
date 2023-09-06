package com.codurance.katalyst.payment.katalistpayment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity freeSubscription(@RequestBody Customer customer) {
        if (moodleAPIClient.existsAnUserinThisCourse(customer.getCourseId(), customer.getEmail())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The user has a suscription for this course");
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
