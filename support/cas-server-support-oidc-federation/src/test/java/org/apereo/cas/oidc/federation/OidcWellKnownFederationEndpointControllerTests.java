package org.apereo.cas.oidc.federation;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcWellKnownFederationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("OIDCWeb")
class OidcWellKnownFederationEndpointControllerTests extends AbstractOidcFederationTests {
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
