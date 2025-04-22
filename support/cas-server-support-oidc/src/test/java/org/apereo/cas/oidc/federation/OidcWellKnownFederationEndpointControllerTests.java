package org.apereo.cas.oidc.federation;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcWellKnownFederationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = {
    "CasFeatureModule.OpenIDConnect.federation.enabled=true",
    "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/federation.jwks"
})
class OidcWellKnownFederationEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcWellKnownFederationController")
    protected OidcWellKnownFederationEndpointController oidcWellKnownController;

    @Test
    void verifyOperation() throws Exception {
        var request = getHttpRequestForEndpoint("unknown/" + OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL);
        request.setRequestURI("unknown/issuer");
        var entity = oidcWellKnownController.getWellKnownDiscoveryConfiguration(request, new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());

        request = getHttpRequestForEndpoint(OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL);
        entity = oidcWellKnownController.getWellKnownDiscoveryConfiguration(request, new MockHttpServletResponse());
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }
}
