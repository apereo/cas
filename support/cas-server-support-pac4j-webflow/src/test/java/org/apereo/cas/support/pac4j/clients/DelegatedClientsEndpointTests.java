package org.apereo.cas.support.pac4j.clients;

import module java.base;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpoint;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@TestPropertySource(properties = {
    "management.endpoint.delegatedClients.access=UNRESTRICTED",
    
    "cas.authn.pac4j.cas[0].login-url=https://localhost:8444/cas/login",

    "cas.authn.pac4j.oauth2[0].id=123456",
    "cas.authn.pac4j.oauth2[0].secret=s3cr3t",

    "cas.authn.pac4j.oidc[0].google.id=123",
    "cas.authn.pac4j.oidc[0].google.secret=123",

    "cas.authn.pac4j.github.id=123",
    "cas.authn.pac4j.github.secret=123"
})
@Tag("ActuatorEndpoint")
@Import(BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
class DelegatedClientsEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("delegatedClientsEndpoint")
    private DelegatedClientsEndpoint endpoint;

    @Test
    void verifyOperation() {
        assertFalse(endpoint.getClients().isEmpty());
        assertFalse(endpoint.reload().isEmpty());
    }
}
