package org.apereo.cas.web.report;

import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredAuthenticationHandlersEndpointTests}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.3.0
 */
@TestPropertySource(properties = "management.endpoint.authenticationHandlers.enabled=true")
@Tag("ActuatorEndpoint")
public class RegisteredAuthenticationHandlersEndpointTests extends AbstractCasEndpointTests {

    @Autowired
    @Qualifier("registeredAuthenticationHandlersEndpoint")
    private RegisteredAuthenticationHandlersEndpoint endpoint;

    @Test
    public void verifyOperation() {
        assertFalse(endpoint.handle().isEmpty());
        assertNotNull(endpoint.fetchAuthnHandler(HttpBasedServiceCredentialsAuthenticationHandler.class.getSimpleName()));
    }
}
