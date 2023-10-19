package com.codurance.katalyst.payment.application.unit.infrastructure;

import com.codurance.katalyst.payment.application.infrastructure.requests.ServletRequestIpCatcher;
import jakarta.servlet.ServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServeletRequestIpCatcherShould {

    private ServletRequestIpCatcher ipCatcher;

    @BeforeEach
    void beforeEach() {
        ipCatcher = new ServletRequestIpCatcher();
    }

    @ParameterizedTest
    @CsvSource({
            "83.38.119.19,83.38.119.19",
            "83.38.119.19:49261,83.38.119.19"
    })
    void return_an_ip4_valid(String ip, String expectedIp) {
        var request = mock(ServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(ip);

        var transformedIp = ipCatcher.getIpFrom(request);

        assertThat(transformedIp).isEqualTo(expectedIp);
    }
}
