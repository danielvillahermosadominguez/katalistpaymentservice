package com.codurance.katalyst.payment.application.utils;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Component
public class DateServiceLocalUTC implements DateService{
    public Instant getInstant() {
        OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
        Date date = Date.from(utc.toInstant());
        return date.toInstant();
    }
}
