package com.codurance.katalyst.payment.application.model.ports.clock;

import java.time.Instant;

public interface Clock {
    Instant getInstant();
}
