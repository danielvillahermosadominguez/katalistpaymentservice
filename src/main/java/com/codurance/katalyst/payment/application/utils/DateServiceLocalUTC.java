package com.codurance.katalyst.payment.application.utils;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Component
public class DateServiceLocalUTC implements DateService{
    public Instant getInstant() {
        var utc = OffsetDateTime.now(ZoneOffset.UTC);
        var date = Date.from(utc.toInstant());
        return date.toInstant();
    }
}
