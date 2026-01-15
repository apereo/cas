package org.apereo.cas.oidc.web.controllers.jwks;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/oidc-jwks.jwks",
    "management.endpoint.oidcJwks.access=UNRESTRICTED"
})
@Tag("ActuatorEndpoint")
@Import(AbstractOidcTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
class OidcJwksRotationEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("jwksRotationEndpoint")
    private OidcJwksRotationEndpoint jwksRotationEndpoint;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(jwksRotationEndpoint.handleRotation());
        assertNotNull(jwksRotationEndpoint.handleRevocation());
    }

}
