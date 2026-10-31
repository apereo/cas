package org.apereo.cas.uma.web.authn;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link UmaRequestingPartyTokenAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
class UmaRequestingPartyTokenAuthenticatorTests extends BaseUmaEndpointControllerTests {

    @Test
    void verifyOperation() throws Throwable {
        val input = new UmaRequestingPartyTokenAuthenticator();
        assertEquals(Set.of(OAuth20Constants.UMA_PROTECTION_SCOPE), input.getRequiredScopes());
        val token = getAccessToken();
        when(token.getScopes()).thenReturn(Set.of(OAuth20Constants.UMA_PROTECTION_SCOPE));
        val profile = input.authenticate(new MockHttpServletRequest(), token).orElseThrow();
        assertEquals("casuser", profile.getId());
        assertEquals(Set.of(OAuth20Constants.UMA_PROTECTION_SCOPE), profile.getRoles());
        assertSame(token, profile.getAttribute(OAuth20AccessToken.class.getName()));
        assertEquals(token.getClientId(), profile.getAttribute(OAuth20Constants.CLIENT_ID));
    }

    @Test
    void verifyTokenWithoutProtectionScope() throws Throwable {
        val token = getAccessToken();
        ticketRegistry.addTicket(token);
        val response = new MockHttpServletResponse();
        assertFalse(umaRequestingPartyTokenSecurityInterceptor.preHandle(newRequest("Bearer " + token.getId()), response, new Object()));
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"insufficient_scope\""));
    }

    @Test
    void verifyUnknownToken() throws Throwable {
        val response = new MockHttpServletResponse();
        assertFalse(umaRequestingPartyTokenSecurityInterceptor.preHandle(newRequest("Bearer AT-unknown"), response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"invalid_token\""));
    }

    @Test
    void verifySchemeIsCaseInsensitive() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        val request = newRequest("bearer " + results.getRight());
        assertTrue(umaRequestingPartyTokenSecurityInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        val profile = (OAuth20UserProfile) request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE);
        assertNotNull(profile);
        assertTrue(profile.getRoles().contains(OAuth20Constants.UMA_PROTECTION_SCOPE));
    }

    private static MockHttpServletRequest newRequest(final String authorization) {
        val request = new MockHttpServletRequest(HttpMethod.POST.name(), CONTEXT + OAuth20Constants.UMA_REGISTRATION_URL);
        request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        return request;
    }
}
