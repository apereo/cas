package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
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
        request.setParameter(OAuth20Constants.CLIENT_ID, "client");
        val at = getAccessToken();
        when(at.getClientId()).thenReturn("client");
        when(at.getScopes()).thenReturn(Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE));
        val authenticator = new OidcClientConfigurationAccessTokenAuthenticator();
        assertEquals(Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE), authenticator.getRequiredScopes());
        val userProfile = authenticator.authenticate(request, at).orElseThrow();
        assertEquals("casuser", userProfile.getId());
        assertEquals("client", userProfile.getAttribute(OAuth20Constants.CLIENT_ID));
    }

    @Test
    void verifyRejectsTokenIssuedToDifferentClient() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(OAuth20Constants.CLIENT_ID, "requested-client");
        val at = getAccessToken();
        when(at.getClientId()).thenReturn("token-client");
        assertTrue(new OidcClientConfigurationAccessTokenAuthenticator().authenticate(request, at).isEmpty());
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        val at = getAccessToken();
        when(at.getClientId()).thenReturn("client");
        assertTrue(new OidcClientConfigurationAccessTokenAuthenticator().authenticate(new MockHttpServletRequest(), at).isEmpty());
    }
}
