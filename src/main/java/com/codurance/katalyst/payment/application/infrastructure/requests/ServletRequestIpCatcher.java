package com.codurance.katalyst.payment.application.infrastructure.requests;

import com.codurance.katalyst.payment.application.common.requests.AbstractIpCatcher;
import jakarta.servlet.ServletRequest;

public class ServletRequestIpCatcher implements AbstractIpCatcher {
    @Override
    public String getIpFrom(ServletRequest request) {
        return request.getRemoteAddr();
    }
}
