package com.codurance.katalyst.payment.application.common.requests;

import jakarta.servlet.ServletRequest;

public interface AbstractIpCatcher {
    String getIpFrom(ServletRequest request);
}
