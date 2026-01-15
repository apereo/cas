package org.apereo.cas.web.security.authentication;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.ActuatorEndpointProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link IpAddressAuthorizationManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebApp")
class IpAddressAuthorizationManagerTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyOperationBadPattern() {
        val results = getAuthorizationDecision(List.of(".***"), "127.0.0.1");
        assertFalse(results.isGranted());
    }

    @Test
    void verifyOperationFails() {
        val results = getAuthorizationDecision(List.of("192.+"), "127.0.0.1");
        assertFalse(results.isGranted());
    }

    @Test
    void verifyOperationPassesPattern() {
        val results = getAuthorizationDecision(List.of("127.+"), "127.0.0.1");
        assertTrue(results.isGranted());
    }

    @Test
    void verifyOperationPasses() {
        val results = getAuthorizationDecision(List.of("127.0.0.1"), "127.0.0.1");
        assertTrue(results.isGranted());
    }

    @Test
    void verifyOperationCIDR() {
        val results = getAuthorizationDecision(List.of("192.168.0.0/24"), "192.168.0.1");
        assertTrue(results.isGranted());
    }

    private AuthorizationResult getAuthorizationDecision(final List<String> addresses, final String remoteAddr) {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        val manager = new IpAddressAuthorizationManager(casProperties,
            new ActuatorEndpointProperties().setRequiredIpAddresses(addresses));
        return manager.authorize(() -> new TestingAuthenticationToken("cas", "cas"), new RequestAuthorizationContext(request));
    }
}
