package org.apereo.cas.web.security.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.ActuatorEndpointProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link IpAddressAuthorizationManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebApp")
public class IpAddressAuthorizationManagerTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperationBadPattern() throws Exception {
        val results = getAuthorizationDecision(List.of(".***"));
        assertFalse(results.isGranted());
    }

    @Test
    public void verifyOperationFails() throws Exception {
        val results = getAuthorizationDecision(List.of("192.+"));
        assertFalse(results.isGranted());
    }

    @Test
    public void verifyOperationPassesPattern() throws Exception {
        val results = getAuthorizationDecision(List.of("127.+"));
        assertTrue(results.isGranted());
    }

    @Test
    public void verifyOperationPasses() throws Exception {
        val results = getAuthorizationDecision(List.of("127.0.0.1"));
        assertTrue(results.isGranted());
    }

    private AuthorizationDecision getAuthorizationDecision(final List<String> addresses) {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        val manager = new IpAddressAuthorizationManager(casProperties,
            new ActuatorEndpointProperties().setRequiredIpAddresses(addresses));
        return manager.check(() -> new TestingAuthenticationToken("cas", "cas"), new RequestAuthorizationContext(request));
    }
}
