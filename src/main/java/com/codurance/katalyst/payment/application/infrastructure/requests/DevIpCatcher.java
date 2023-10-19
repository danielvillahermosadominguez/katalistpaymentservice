package com.codurance.katalyst.payment.application.infrastructure.requests;

import com.codurance.katalyst.payment.application.common.requests.AbstractIpCatcher;
import jakarta.servlet.ServletRequest;
import org.springframework.beans.factory.annotation.Value;

public class DevIpCatcher implements AbstractIpCatcher {
    @Value("${ipcatcher.devIp}")
    private String devIp;

    @Override
    public String getIpFrom(ServletRequest request) {
        return devIp;
    }
}
