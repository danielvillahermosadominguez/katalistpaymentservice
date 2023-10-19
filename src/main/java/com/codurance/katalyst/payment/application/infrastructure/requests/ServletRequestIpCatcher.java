package com.codurance.katalyst.payment.application.infrastructure.requests;

import com.codurance.katalyst.payment.application.common.requests.AbstractIpCatcher;
import jakarta.servlet.ServletRequest;

public class ServletRequestIpCatcher implements AbstractIpCatcher {
    @Override
    public String getIpFrom(ServletRequest request) {
        var ip =  request.getRemoteAddr();
        ip = removeNotIP4String(ip);
        return ip;
    }

    private String removeNotIP4String(String ip) {

        //TODO: We need to review this algorithm. This is because in production, we receive
        //a sufix in the IPv4 with ":". For example 83.38.119.19:49261
        if(!ip.contains(":")) {
           return ip;
        }
        var part = ip.split(":");
        if(part.length >= 1) {
            ip = part[0];
        }
        return ip;
    }

}
