package com.codurance.katalyst.payment.application.acceptance.doubles;

import com.codurance.katalyst.payment.application.model.ports.clock.Clock;

import java.time.Instant;
import java.util.Date;

public class ClockStub implements Clock {
    @Override
    public Instant getInstant() {
        Date date = new Date(2323223232L);
        return date.toInstant();
    }
}
