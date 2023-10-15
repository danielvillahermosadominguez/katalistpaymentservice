package com.codurance.katalyst.payment.application.common.logs;

public interface AbstractLog {
    void info(Class c, String message);
    void error(Class c, String message);
    void debug(Class c, String message);
    void warn(Class c, String message);
    void trace(Class c, String message);
}
