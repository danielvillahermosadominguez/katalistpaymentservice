package com.codurance.katalyst.payment.application.infrastructure.logs;

import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import org.slf4j.Logger;

public class AbstractLogSlf4j implements AbstractLog {
    private final Logger log;

    public AbstractLogSlf4j(Logger log) {
        this.log = log;
    }

    @Override
    public void info(Class c, String message) {
        log.info(formatMessage(c, message));
    }

    @Override
    public void error(Class c, String message) {
        log.error(formatMessage(c, message));
    }

    @Override
    public void debug(Class c, String message) {
        log.debug(formatMessage(c, message));
    }

    @Override
    public void warn(Class c, String message) {
        log.warn(formatMessage(c, message));
    }

    @Override
    public void trace(Class c, String message) {
        log.trace(formatMessage(c, message));
    }

    private String formatMessage(Class c, String message) {
        return String.format("[%s]: %s", c.getSimpleName(), message);
    }
}
