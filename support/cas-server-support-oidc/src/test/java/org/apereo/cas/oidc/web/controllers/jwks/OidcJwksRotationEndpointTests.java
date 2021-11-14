package org.apereo.cas.oidc.web.controllers.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJwksRotationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.mvc.pathmatch.matching-strategy=ant-path-matcher",
    "cas.authn.oidc.jwks.jwks-file=file:${#systemProperties['java.io.tmpdir']}/oidc-jwks.jwks",
    "management.endpoint.oidcJwks.enabled=true"
})
@Tag("ActuatorEndpoint")
@Import(AbstractOidcTests.SharedTestConfiguration.class)
public class OidcJwksRotationEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("jwksRotationEndpoint")
    private OidcJwksRotationEndpoint jwksRotationEndpoint;

    @Test
    public void verifyOperation() throws Exception {
        assertNotNull(jwksRotationEndpoint.handleRotation());
        assertNotNull(jwksRotationEndpoint.handleRevocation());
    }

}
