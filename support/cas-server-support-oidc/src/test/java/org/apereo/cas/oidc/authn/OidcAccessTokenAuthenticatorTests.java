package org.apereo.cas.oidc.authn;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccessTokenAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcAccessTokenAuthenticatorTests extends AbstractOidcTests {

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        val token = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(), getClaims());
        val auth = new OidcAccessTokenAuthenticator(ticketRegistry, oidcTokenSigningAndEncryptionService,
            servicesManager, accessTokenJwtBuilder);
        val at = getAccessToken(token, "clientid");
        ticketRegistry.addTicket(at);
        val credentials = new TokenCredentials(at.getId());

        auth.validate(credentials, ctx);

        val userProfile = credentials.getUserProfile();
        assertNotNull(userProfile);
        assertEquals("casuser", userProfile.getId());
        assertTrue(userProfile.containsAttribute("client_id"));
        assertTrue(userProfile.containsAttribute("sub"));
        assertTrue(userProfile.containsAttribute("iss"));
        assertTrue(userProfile.containsAttribute("exp"));
        assertTrue(userProfile.containsAttribute("aud"));
        assertTrue(userProfile.containsAttribute("email"));
    }
}
