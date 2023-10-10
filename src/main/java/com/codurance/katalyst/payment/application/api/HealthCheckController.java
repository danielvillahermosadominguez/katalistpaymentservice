package com.codurance.katalyst.payment.application.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/healthcheck")
    public String heatlhCheck() {
        return "OK! Working";
    }
}
