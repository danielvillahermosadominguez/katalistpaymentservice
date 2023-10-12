package com.codurance.katalyst.payment.application.infrastructure.adapters.clock;

import com.codurance.katalyst.payment.application.model.ports.clock.Clock;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Component
public class ClockLocalUTC implements Clock {
    public Instant getInstant() {
        var utc = OffsetDateTime.now(ZoneOffset.UTC);
        var date = Date.from(utc.toInstant());
        return date.toInstant();
    }
}
