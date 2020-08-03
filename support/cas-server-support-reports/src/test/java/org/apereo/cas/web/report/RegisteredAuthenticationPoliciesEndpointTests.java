package org.apereo.cas.web.report;

import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredAuthenticationPoliciesEndpointTests}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.3.0
 */
@TestPropertySource(properties = "management.endpoint.authenticationPolicies.enabled=true")
@Tag("ActuatorEndpoint")
public class RegisteredAuthenticationPoliciesEndpointTests extends AbstractCasEndpointTests {

    @Autowired
    @Qualifier("registeredAuthenticationPoliciesEndpoint")
    private RegisteredAuthenticationPoliciesEndpoint endpoint;

    @Test
    public void verifyOperation() {
        assertFalse(endpoint.handle().isEmpty());
        assertNotNull(endpoint.fetchPolicy(AtLeastOneCredentialValidatedAuthenticationPolicy.class.getSimpleName()));
    }
}
