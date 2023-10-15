package com.codurance.katalyst.payment.application.infrastructure.logs;


import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import com.codurance.katalyst.payment.application.common.logs.AbstractLogFactory;
import org.slf4j.LoggerFactory;

public class AbstractLogFactorySlf4j implements AbstractLogFactory {

    @Override
    public AbstractLog getLogger(Class c) {
        var log = LoggerFactory.getLogger(c);
        return new AbstractLogSlf4j(log);
    }
}
