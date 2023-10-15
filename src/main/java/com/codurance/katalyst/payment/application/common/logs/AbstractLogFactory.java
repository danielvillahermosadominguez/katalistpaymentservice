package com.codurance.katalyst.payment.application.common.logs;

public interface AbstractLogFactory {
    AbstractLog getLogger(Class c);
}
