package com.codurance.katalyst.payment.katalistpayment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {
    @GetMapping("/healthcheck")
    public String heatlhCheck() {
        return String.format("OK! Working");
    }
   // @RequestMapping(value = "/freesuscription", method = RequestMethod.POST)
    @PostMapping("/freesuscription")
    @ResponseBody
    public ResponseEntity freeSuscription(@RequestBody String customer) {
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
