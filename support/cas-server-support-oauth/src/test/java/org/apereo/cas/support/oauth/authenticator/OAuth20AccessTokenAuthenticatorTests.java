package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenAuthenticationInterceptor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AccessTokenAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
class OAuth20AccessTokenAuthenticatorTests extends BaseOAuth20AuthenticatorTests {

    @Test
    void verifyAuthenticationWithJwtAccessToken() throws Throwable {
        val accessToken = getAccessToken();
        ticketRegistry.addTicket(accessToken);
        val encodedAccessToken = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext, serviceJwtAccessToken,
            accessToken, accessToken.getService(), false).encode(accessToken.getId());
        val request = newRequest(OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + encodedAccessToken);
        val response = new MockHttpServletResponse();
        assertTrue(newInterceptor(new DefaultOAuth20AccessTokenAuthenticator()).preHandle(request, response, new Object()));
        val profile = (OAuth20UserProfile) request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE);
        assertNotNull(profile);
        assertEquals("casuser", profile.getId());
        assertEquals("client", profile.getAttribute(OAuth20Constants.CLIENT_ID));
    }

    @Test
    void verifyAuthenticationFailsWithUnknownToken() throws Throwable {
        val accessToken = getAccessToken();
        val encodedAccessToken = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext, serviceJwtAccessToken,
            accessToken, accessToken.getService(), false).encode(accessToken.getId());
        val request = newRequest(OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + encodedAccessToken);
        val response = new MockHttpServletResponse();
        assertFalse(newInterceptor(new DefaultOAuth20AccessTokenAuthenticator()).preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"invalid_token\""));
        assertTrue(response.getContentAsString().contains(OAuth20Constants.INVALID_TOKEN));
        assertNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer ", "bearer ", "BEARER "})
    void verifyAuthentication(final String scheme) throws Throwable {
        val accessToken = getAccessToken();
        ticketRegistry.addTicket(accessToken);
        val encodedAccessToken = OAuth20JwtAccessTokenEncoder.toEncodableCipher(configurationContext, service,
            accessToken, accessToken.getService(), false).encode(accessToken.getId());
        val request = newRequest(scheme + encodedAccessToken);
        val response = new MockHttpServletResponse();
        assertTrue(newInterceptor(new DefaultOAuth20AccessTokenAuthenticator()).preHandle(request, response, new Object()));
        assertEquals("DENY", response.getHeader("X-Frame-Options"));
        assertNotNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }

    @ParameterizedTest
    @ValueSource(strings = {StringUtils.EMPTY, "Basic Y2xpZW50OnNlY3JldA==", "Bearer "})
    void verifyChallengeWithoutAccessToken(final String authorization) throws Throwable {
        val request = newRequest(authorization);
        val response = new MockHttpServletResponse();
        assertFalse(newInterceptor(new DefaultOAuth20AccessTokenAuthenticator()).preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(OAuth20Constants.TOKEN_TYPE_BEARER, response.getHeader(HttpHeaders.WWW_AUTHENTICATE));
        assertEquals(StringUtils.EMPTY, response.getContentAsString());
        assertEquals("no-cache, no-store, max-age=0, must-revalidate", response.getHeader(HttpHeaders.CACHE_CONTROL));
    }

    @Test
    void verifyInsufficientScope() throws Throwable {
        val accessToken = getAccessToken();
        when(accessToken.getScopes()).thenReturn(Set.of("openid"));
        ticketRegistry.addTicket(accessToken);
        val authenticator = new DefaultOAuth20AccessTokenAuthenticator();
        authenticator.setRequiredScopes(Set.of(OAuth20Constants.UMA_PROTECTION_SCOPE));
        val request = newRequest(OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getId());
        val response = new MockHttpServletResponse();
        assertFalse(newInterceptor(authenticator).preHandle(request, response, new Object()));
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        val challenge = response.getHeader(HttpHeaders.WWW_AUTHENTICATE);
        assertTrue(challenge.startsWith("Bearer error=\"insufficient_scope\""));
        assertTrue(challenge.contains("scope=\"" + OAuth20Constants.UMA_PROTECTION_SCOPE + '"'));
    }

    @Test
    void verifyAuthenticatorRefusesToken() throws Throwable {
        val accessToken = getAccessToken();
        ticketRegistry.addTicket(accessToken);
        val request = newRequest(OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getId());
        val response = new MockHttpServletResponse();
        assertFalse(newInterceptor((req, token) -> Optional.empty()).preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"invalid_token\""));
    }

    @Test
    void verifyExpiredAccessToken() throws Throwable {
        val accessToken = getAccessToken();
        when(accessToken.isExpired()).thenReturn(true);
        val registry = mock(TicketRegistry.class);
        when(registry.getTicket(accessToken.getId(), OAuth20AccessToken.class)).thenReturn(accessToken);
        val request = newRequest(OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getId());
        val response = new MockHttpServletResponse();
        val interceptor = new OAuth20AccessTokenAuthenticationInterceptor(new DefaultOAuth20AccessTokenAuthenticator(), registry,
            accessTokenJwtBuilder, configurationContext.getProofOfPossessionValidator());
        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"invalid_token\""));
        verify(registry).getTicket(accessToken.getId(), OAuth20AccessToken.class);
        verify(accessToken, atLeastOnce()).isExpired();
    }

    @Test
    void verifyAuthenticatorFailureRefusesToken() throws Throwable {
        val accessToken = getAccessToken();
        ticketRegistry.addTicket(accessToken);
        val request = newRequest(OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getId());
        val response = new MockHttpServletResponse();
        assertFalse(newInterceptor((req, token) -> {
            throw new IllegalStateException("Unable to build the profile");
        }).preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"invalid_token\""));
    }

    @Test
    void verifyUnboundAccessTokenUnderDPoPSchemeIsRefused() throws Throwable {
        val accessToken = getAccessToken();
        ticketRegistry.addTicket(accessToken);
        val request = newRequest(OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getId());
        val response = new MockHttpServletResponse();
        assertFalse(newInterceptor(new DefaultOAuth20AccessTokenAuthenticator()).preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("DPoP error=\"invalid_token\""));
        assertNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }

    @Test
    void verifyDPoPBoundAccessTokenRequiresProof() throws Throwable {
        val accessToken = boundAccessToken(OAuth20Constants.DPOP_CONFIRMATION, "thumbprint");
        val interceptor = newInterceptor(new DefaultOAuth20AccessTokenAuthenticator());

        val bearerRequest = newRequest(OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getId());
        val bearerResponse = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(bearerRequest, bearerResponse, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), bearerResponse.getStatus());
        assertTrue(bearerResponse.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"invalid_token\""));

        val dpopRequest = newRequest(OAuth20Constants.TOKEN_TYPE_DPOP + ' ' + accessToken.getId());
        val dpopResponse = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(dpopRequest, dpopResponse, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), dpopResponse.getStatus());
        assertTrue(dpopResponse.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("DPoP error=\"invalid_dpop_proof\""));
        assertNull(dpopRequest.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }

    @Test
    void verifyCertificateBoundAccessTokenRequiresCertificate() throws Throwable {
        val accessToken = boundAccessToken(OAuth20Constants.X509_CERTIFICATE_DIGEST, "thumbprint");
        val request = newRequest(OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getId());
        val response = new MockHttpServletResponse();
        assertFalse(newInterceptor(new DefaultOAuth20AccessTokenAuthenticator()).preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"invalid_token\""));
    }

    private OAuth20AccessToken boundAccessToken(final String attribute, final String value) throws Exception {
        val accessToken = getAccessToken();
        val authentication = DefaultAuthenticationBuilder.newInstance(accessToken.getAuthentication())
            .addAttribute(attribute, value).build();
        when(accessToken.getAuthentication()).thenReturn(authentication);
        ticketRegistry.addTicket(accessToken);
        return accessToken;
    }

    private OAuth20AccessTokenAuthenticationInterceptor newInterceptor(final OAuth20AccessTokenAuthenticator authenticator) {
        return new OAuth20AccessTokenAuthenticationInterceptor(authenticator, ticketRegistry,
            accessTokenJwtBuilder, configurationContext.getProofOfPossessionValidator());
    }

    private static MockHttpServletRequest newRequest(final String authorization) {
        val request = new MockHttpServletRequest("POST", "/cas/oidc/register");
        request.setRequestURI("/cas/oidc/register");
        if (StringUtils.isNotBlank(authorization)) {
            request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        }
        return request;
    }
}
