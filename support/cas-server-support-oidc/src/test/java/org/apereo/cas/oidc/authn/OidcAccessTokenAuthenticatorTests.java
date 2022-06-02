package org.apereo.cas.oidc.authn;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Set;

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
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        val token = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(), getClaims());
        val at = getAccessToken(token, "clientid");
        ticketRegistry.addTicket(at);
        val credentials = new TokenCredentials(at.getId());

        getAuthenticator().validate(credentials, ctx, JEESessionStore.INSTANCE);

        val userProfile = credentials.getUserProfile();
        assertNotNull(userProfile);
        assertEquals("casuser", userProfile.getId());
        assertTrue(userProfile.containsAttribute("client_id"));
        assertTrue(userProfile.containsAttribute("sub"));
        assertTrue(userProfile.containsAttribute("iss"));
        assertTrue(userProfile.containsAttribute("exp"));
        assertTrue(userProfile.containsAttribute("aud"));
        assertTrue(userProfile.containsAttribute("email"));

        assertSame(OAuth20AccessToken.class, accessTokenExpirationPolicy.getTicketType());
    }

    private OidcAccessTokenAuthenticator getAuthenticator() {
        return new OidcAccessTokenAuthenticator(
            oidcConfigurationContext.getCentralAuthenticationService(), oidcTokenSigningAndEncryptionService,
            servicesManager, oidcAccessTokenJwtBuilder);
    }

    @Test
    public void verifyFailsOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        val at = getAccessToken("helloworld", "clientid");
        ticketRegistry.addTicket(at);
        val credentials = new TokenCredentials(at.getId());
        getAuthenticator().validate(credentials, ctx, JEESessionStore.INSTANCE);
        assertNull(credentials.getUserProfile());
    }

    @Test
    public void verifyFailsMissingScopes() throws Exception {
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        val token = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(), getClaims());
        val at = getAccessToken(token, "clientid");
        ticketRegistry.addTicket(at);
        val credentials = new TokenCredentials(at.getId());
        val authn = getAuthenticator();
        authn.setRequiredScopes(Set.of("required-scope"));
        authn.validate(credentials, ctx, JEESessionStore.INSTANCE);
        assertNull(credentials.getUserProfile());
    }
}
