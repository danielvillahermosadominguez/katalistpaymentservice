package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.utils.DateService;

import java.time.Instant;
import java.util.Date;

public class TestDateService implements DateService {
    @Override
    public Instant getInstant() {
        Date date = new Date(2323223232L);
        return date.toInstant();
    }
}
