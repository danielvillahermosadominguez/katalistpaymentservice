package com.codurance.katalyst.payment.katalistpayment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    @GetMapping("/healthcheck")
    public String heatlhCheck() {
        return String.format("OK!");
    }
}
