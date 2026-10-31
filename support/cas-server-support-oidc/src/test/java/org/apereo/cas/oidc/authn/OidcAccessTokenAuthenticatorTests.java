package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.OAuth20AccessTokenAuthenticator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
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
    @Qualifier("oidcDynamicRegistrationAuthenticator")
    private OAuth20AccessTokenAuthenticator oidcDynamicRegistrationAuthenticator;

    @Autowired
    @Qualifier("requiresAuthenticationDynamicRegistrationInterceptor")
    private HandlerInterceptor requiresAuthenticationDynamicRegistrationInterceptor;

    @Test
    void verifyOperation() throws Throwable {
        val token = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(), getClaims());
        val at = getAccessToken(token, "clientid");
        ticketRegistry.addTicket(at);

        val userProfile = getAuthenticator().authenticate(new MockHttpServletRequest(), at).orElseThrow();
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
        val at = getAccessToken("helloworld", "clientid");
        ticketRegistry.addTicket(at);
        assertTrue(getAuthenticator().authenticate(new MockHttpServletRequest(), at).isEmpty());
    }

    @Test
    void verifyFailsMissingScopes() throws Throwable {
        assertEquals(Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE), oidcDynamicRegistrationAuthenticator.getRequiredScopes());
        val token = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(), getClaims());
        val at = getAccessToken(token, "clientid");
        ticketRegistry.addTicket(at);
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + at.getId());
        val response = new MockHttpServletResponse();
        assertFalse(requiresAuthenticationDynamicRegistrationInterceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).contains("scope=\"" + OidcConstants.CLIENT_REGISTRATION_SCOPE + '"'));
    }

    @Test
    void verifyPassesWithRegistrationScope() throws Throwable {
        val registrationToken = getAccessToken(UUID.randomUUID().toString(),
            Set.of(OidcConstants.StandardScopes.OPENID.getScope(), OidcConstants.CLIENT_REGISTRATION_SCOPE));
        ticketRegistry.addTicket(registrationToken);
        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + registrationToken.getId());
        assertTrue(requiresAuthenticationDynamicRegistrationInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertNotNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }

    private OidcAccessTokenAuthenticator getAuthenticator() {
        return new OidcAccessTokenAuthenticator(oidcTokenSigningAndEncryptionService, servicesManager);
    }
}
