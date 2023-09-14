package com.codurance.katalyst.payment.application.acceptance.utils;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Component
public class HoldedServiceFake {
    public String token;
    private final static String URL_BASE = "/api/";
    private MockServer wireMockServer = null;

    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void start() {
        this.wireMockServer = new MockServer(options().port(this.port));
        this.wireMockServer.start();
        this.init();
    }

    private void init() {
        this.wireMockServer.addMockServiceRequestListener(new RequestListener() {
            @Override
            public void requestReceived(Request request, Response response) {

            }
        });
    }

    public void resetAndConfigure() {
        this.reset();
    }

    public void configureGenericStubs() {

    }

    public void reset() {
        this.wireMockServer.getClient().resetRequests();
    }
}
