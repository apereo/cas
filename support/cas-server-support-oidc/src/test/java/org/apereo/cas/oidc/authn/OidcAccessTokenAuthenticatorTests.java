package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccessTokenAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAuthentication")
class OidcAccessTokenAuthenticatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oauthAccessTokenAuthenticator")
    private Authenticator oauthAccessTokenAuthenticator;

    @Autowired
    @Qualifier("oidcDynamicRegistrationAuthenticator")
    private Authenticator oidcDynamicRegistrationAuthenticator;

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        new ProfileManager(ctx, new JEESessionStore()).removeProfiles();

        val token = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(), getClaims());
        val at = getAccessToken(token, "clientid");
        ticketRegistry.addTicket(at);
        val credentials = new TokenCredentials(at.getId());

        oauthAccessTokenAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);

        val userProfile = credentials.getUserProfile();
        assertNotNull(userProfile);
        assertEquals("casuser", userProfile.getId());
        assertTrue(userProfile.containsAttribute("client_id"));
        assertTrue(userProfile.containsAttribute(OAuth20Constants.CLAIM_SUB));
        assertTrue(userProfile.containsAttribute(OidcConstants.ISS));
        assertTrue(userProfile.containsAttribute(OAuth20Constants.CLAIM_EXP));
        assertTrue(userProfile.containsAttribute(OidcConstants.AUD));
        assertTrue(userProfile.containsAttribute("email"));
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        new ProfileManager(ctx, new JEESessionStore()).removeProfiles();

        val at = getAccessToken("helloworld", "clientid");
        ticketRegistry.addTicket(at);
        val credentials = new TokenCredentials(at.getId());
        oauthAccessTokenAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNull(credentials.getUserProfile());
    }

    @Test
    void verifyFailsMissingScopes() throws Throwable {
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        val token = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(), getClaims());
        val at = getAccessToken(token, "clientid");
        ticketRegistry.addTicket(at);
        val credentials = new TokenCredentials(at.getId());
        oidcDynamicRegistrationAuthenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNull(credentials.getUserProfile());
    }
}
