package org.apereo.cas.oidc.authn;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.OAuth20AuthenticationClientProvider;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDPoPAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OIDCAuthentication")
class OidcDPoPAuthenticatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcDPoPClientProvider")
    private OAuth20AuthenticationClientProvider oidcDPoPClientProvider;

    @Test
    void verifyOperation() throws Throwable {
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.setRequestURI("/cas/oidc");

        val ctx = new JEEContext(request, new MockHttpServletResponse());
        new ProfileManager(ctx, new JEESessionStore()).removeProfiles();

        val ecJWK = new ECKeyGenerator(Curve.P_256).keyID("1").generate();
        val proofFactory = new DefaultDPoPProofFactory(ecJWK, JWSAlgorithm.ES256);
        val proof = proofFactory.createDPoPJWT("POST", new URI(request.getRequestURL().toString()));
        request.addHeader(OAuth20Constants.DPOP, proof.serialize());
        val client = (HeaderClient) oidcDPoPClientProvider.createClient();
        val credentials = new TokenCredentials(OAuth20Constants.DPOP);
        client.getAuthenticator().validate(new CallContext(ctx, new JEESessionStore()), credentials);
        val profile = credentials.getUserProfile();
        assertNotNull(profile);
        assertNotNull(profile.getAttribute(OAuth20Constants.DPOP));
        assertNotNull(profile.getAttribute(OAuth20Constants.DPOP_CONFIRMATION));
    }
}
