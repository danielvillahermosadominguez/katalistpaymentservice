package com.codurance.katalyst.payment.application.acceptance.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;

public class MockServer extends WireMockServer {
    public MockServer(Options options) {
        super(options);
    }

    public WireMock getClient() {
        return client;
    }
}

