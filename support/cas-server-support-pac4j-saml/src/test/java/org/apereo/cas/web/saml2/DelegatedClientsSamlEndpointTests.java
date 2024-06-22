package org.apereo.cas.web.saml2;

import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpoint;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientsSamlEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@TestPropertySource(properties = {
    "management.endpoint.delegatedClients.enabled=true",

    "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
    "cas.authn.pac4j.saml[0].keystore-password=1234567890",
    "cas.authn.pac4j.saml[0].private-key-password=1234567890",
    "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
    "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
    "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid"
})
@Tag("ActuatorEndpoint")
@Import(BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class)
class DelegatedClientsSamlEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("delegatedClientsEndpoint")
    private DelegatedClientsEndpoint endpoint;

    @Test
    void verifyOperation() throws Throwable {
        assertFalse(endpoint.getClients().isEmpty());
        assertFalse(endpoint.reload().isEmpty());
    }
}
