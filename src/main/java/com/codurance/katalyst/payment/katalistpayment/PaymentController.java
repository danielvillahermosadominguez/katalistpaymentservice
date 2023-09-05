package com.codurance.katalyst.payment.katalistpayment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PaymentController {

    @Autowired
    private MoodleAPIClient moodleAPIClient;

    @GetMapping("/healthcheck")
    public String heatlhCheck() {
        return String.format("OK! Working");
    }
    @RequestMapping(value = "/freesuscription", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity freeSuscription(@RequestBody Customer customer) {
        List<MoodleUserDTO> users = moodleAPIClient.getUsersForCourse(customer.getCourseId());
        List<MoodleUserDTO> filtered = users.stream().filter( c-> c.getEmail().toLowerCase().equals(customer.getEmail().toLowerCase())).collect(Collectors.toList());
        if(!filtered.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The user has a suscription for this course");
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
