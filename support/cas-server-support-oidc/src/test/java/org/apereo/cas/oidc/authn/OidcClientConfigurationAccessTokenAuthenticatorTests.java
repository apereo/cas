package org.apereo.cas.oidc.authn;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcClientConfigurationAccessTokenAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAuthentication")
class OidcClientConfigurationAccessTokenAuthenticatorTests extends AbstractOidcTests {

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        val at = getAccessToken();
        when(at.getScopes()).thenReturn(Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE));
        ticketRegistry.addTicket(at);
        val credentials = new TokenCredentials(at.getId());
        getAuthenticator().validate(new CallContext(ctx, new JEESessionStore()), credentials);

        val userProfile = credentials.getUserProfile();
        assertNotNull(userProfile);
        assertEquals("casuser", userProfile.getId());
    }

    private OidcClientConfigurationAccessTokenAuthenticator getAuthenticator() {
        return new OidcClientConfigurationAccessTokenAuthenticator(
            oidcConfigurationContext.getTicketRegistry(), oidcAccessTokenJwtBuilder);
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        val at = getAccessToken();
        when(at.getScopes()).thenThrow(new IllegalArgumentException());
        ticketRegistry.addTicket(at);
        val credentials = new TokenCredentials(at.getId());
        getAuthenticator().validate(new CallContext(ctx, new JEESessionStore()), credentials);
        val userProfile = credentials.getUserProfile();
        assertNull(userProfile);
    }
}
