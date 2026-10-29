package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.DefaultOAuth20ClientAuthenticationProvider;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationProvider;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationResponseException;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationToken;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientCredentialsAuthenticator;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.web.session.DefaultCasSessionStore;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * This is {@link OAuth20HandlerInterceptorAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuthWeb")
class OAuth20HandlerInterceptorAdapterTests extends AbstractOAuth20Tests {

    @ParameterizedTest
    @ValueSource(strings = {"%61uthorize", "%61ccessToken", "%74oken", "%72evoke", "%64evice"})
    void verifyEncodedEndpointPathsRequireAuthentication(final String endpoint) throws Throwable {
        val clientId = UUID.randomUUID().toString();
        val service = getRegisteredService("https://oauth.example.org", clientId, CLIENT_SECRET);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.setRequestURI("/oauth2/" + endpoint);
        request.setParameter(OAuth20Constants.CLIENT_ID, clientId);
        request.setParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org");
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        val response = new MockHttpServletResponse();
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }

    @Test
    void verifySessionCookiePathIsConfiguredBeforeSecurityChecks() throws Throwable {
        val configurationContext = mock(OAuth20ConfigurationContext.class);
        val adapter = new OAuth20HandlerInterceptorAdapter(
            new DirectObjectProvider<>(mock(HandlerInterceptor.class)),
            new DirectObjectProvider<>(mock(HandlerInterceptor.class)),
            new DirectObjectProvider<>(List.of()),
            new DirectObjectProvider<>(servicesManager),
            new DirectObjectProvider<>(List.of()),
            new DirectObjectProvider<>(mock(OAuth20RequestParameterResolver.class)),
            new DirectObjectProvider<>(configurationContext));
        val request = new MockHttpServletRequest();
        request.setContextPath("/cas");
        request.setRequestURI("/cas" + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.AUTHORIZE_URL);
        adapter.preHandle(request, new MockHttpServletResponse(), new Object());
        verify(configurationContext).configureSessionReplicationCookiePath(request);
    }

    @Test
    void verifyAuthorizationAuth() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val clientId = UUID.randomUUID().toString();
        request.setRequestURI('/' + OAuth20Constants.AUTHORIZE_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, clientId);
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        request.setParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org");
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());

        val service = getRegisteredService("https://oauth.example.org", clientId, CLIENT_SECRET);
        servicesManager.save(service);
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
        assertNull(request.getAttribute(OAuth20Constants.ERROR));

        request.removeAllParameters();
        assertTrue(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
        assertNotNull(request.getAttribute(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.INVALID_REQUEST, request.getAttribute(OAuth20Constants.ERROR).toString());
    }

    @Test
    void verifyRevocationNeedsAuthn() throws Throwable {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.setRequestURI('/' + OAuth20Constants.REVOCATION_URL);
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRequestURI('/' + OAuth20Constants.REVOCATION_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, "unknown123456");
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }

    @Test
    void verifyRevocationAuth() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val clientId = UUID.randomUUID().toString();
        request.setRequestURI('/' + OAuth20Constants.REVOCATION_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, clientId);

        val service = getRegisteredService(clientId, CLIENT_SECRET);
        servicesManager.save(service);
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }

    @Test
    void verifyAccessToken() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.setRequestURI('/' + OAuth20Constants.ACCESS_TOKEN_URL);
        request.setParameter(OAuth20Constants.ACCESS_TOKEN, getAccessToken().getId());
        val id = UUID.randomUUID().toString();
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, id);
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }

    @Test
    void verifyProfile() throws Throwable {
        val clientId = UUID.randomUUID().toString();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.setRequestURI('/' + OAuth20Constants.PROFILE_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, clientId);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
        assertTrue(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }

    @Test
    void verifyClientAuthenticationAsksForBasicCredentials() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setRequestURI('/' + OAuth20Constants.REVOCATION_URL);
        val response = new MockHttpServletResponse();
        assertFalse(requiresAuthenticationInterceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(OAuth20ClientAuthenticationInterceptor.BASIC_AUTHENTICATION_CHALLENGE, response.getHeader(HttpHeaders.WWW_AUTHENTICATE));
        assertEquals("no-cache, no-store, max-age=0, must-revalidate", response.getHeader(HttpHeaders.CACHE_CONTROL));
        assertEquals("no-cache", response.getHeader(HttpHeaders.PRAGMA));
        assertEquals("0", response.getHeader(HttpHeaders.EXPIRES));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", response.getHeader("X-Frame-Options"));
        assertEquals("1; mode=block", response.getHeader("X-XSS-Protection"));
        assertNull(response.getHeader("Strict-Transport-Security"));
        assertNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }

    @Test
    void verifyClientAuthenticationWithInvalidSecret() throws Throwable {
        val service = getRegisteredService(UUID.randomUUID().toString(), CLIENT_SECRET);
        servicesManager.save(service);
        val request = new MockHttpServletRequest();
        request.setRequestURI('/' + OAuth20Constants.TOKEN_URL);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        val response = new MockHttpServletResponse();
        assertFalse(requiresAuthenticationInterceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(OAuth20ClientAuthenticationInterceptor.BASIC_AUTHENTICATION_CHALLENGE, response.getHeader(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void verifyClientAuthenticationForAnotherClientIsRefused() throws Throwable {
        val named = getRegisteredService(UUID.randomUUID().toString(), CLIENT_SECRET);
        servicesManager.save(named);
        val authenticated = getRegisteredService(UUID.randomUUID().toString(), CLIENT_SECRET);
        servicesManager.save(authenticated);
        val request = new MockHttpServletRequest();
        request.setRequestURI('/' + OAuth20Constants.TOKEN_URL);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.getType());
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64(named.getClientId() + ':' + WRONG_CLIENT_SECRET));
        request.addParameter(OAuth20Constants.CLIENT_ID, authenticated.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        val response = new MockHttpServletResponse();
        assertFalse(requiresAuthenticationInterceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }

    @Test
    void verifyRegisteredClientAuthenticationMethodIsEnforcedForRevocation() throws Throwable {
        val service = getRegisteredService(UUID.randomUUID().toString(), CLIENT_SECRET);
        service.setTokenEndpointAuthenticationMethod("client_secret_post");
        servicesManager.save(service);
        val basicRequest = new MockHttpServletRequest();
        basicRequest.setRequestURI('/' + OAuth20Constants.REVOCATION_URL);
        basicRequest.addParameter(OAuth20Constants.TOKEN, UUID.randomUUID().toString());
        basicRequest.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64(service.getClientId() + ':' + CLIENT_SECRET));
        val basicResponse = new MockHttpServletResponse();
        assertFalse(requiresAuthenticationInterceptor.preHandle(basicRequest, basicResponse, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), basicResponse.getStatus());

        val postRequest = new MockHttpServletRequest();
        postRequest.setRequestURI('/' + OAuth20Constants.REVOCATION_URL);
        postRequest.addParameter(OAuth20Constants.TOKEN, UUID.randomUUID().toString());
        postRequest.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        postRequest.addParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        assertTrue(requiresAuthenticationInterceptor.preHandle(postRequest, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void verifyClientAuthenticationKeepsProfile() throws Throwable {
        val service = getRegisteredService(UUID.randomUUID().toString(), CLIENT_SECRET);
        servicesManager.save(service);
        val request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setSecure(true);
        request.setRequestURI('/' + OAuth20Constants.TOKEN_URL);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.getType());
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64(service.getClientId() + ':' + CLIENT_SECRET));
        val response = new MockHttpServletResponse();
        assertTrue(requiresAuthenticationInterceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("max-age=15768000 ; includeSubDomains", response.getHeader("Strict-Transport-Security"));
        val profile = OAuth20Utils.findAuthenticatedUserProfile(request, response, new DefaultCasSessionStore()).orElseThrow();
        assertSame(profile, request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
        assertEquals(service.getClientId(), profile.getAttribute(OAuth20Constants.CLIENT_ID));
        assertEquals(service.getClientId(), profile.getAttribute(OAuth20Constants.PROFILE_ATTRIBUTE_CLIENT_ID));
    }

    @Test
    void verifyFirstClientAuthenticationMethodWins() throws Throwable {
        val first = newClientAuthenticationProvider("first", 1, token -> Optional.of(new DefaultOAuth20UserProfile("first")));
        val second = newClientAuthenticationProvider("second", 2, token -> Optional.of(new DefaultOAuth20UserProfile("second")));
        val interceptor = newClientAuthenticationInterceptor(second, first);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request, response, new Object()));
        val profile = (OAuth20UserProfile) request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE);
        assertNotNull(profile);
        assertEquals("first", profile.getId());
    }

    @Test
    void verifyClientAuthenticationMethodErrorIsFailure() throws Throwable {
        val failing = newClientAuthenticationProvider("failing", 1, token -> {
            throw new IllegalStateException("Unknown client");
        });
        val interceptor = newClientAuthenticationInterceptor(failing);
        val response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(new MockHttpServletRequest(), response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(OAuth20ClientAuthenticationInterceptor.BASIC_AUTHENTICATION_CHALLENGE, response.getHeader(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void verifyClientAuthenticationMethodAnswersRequest() throws Throwable {
        val challenge = newClientAuthenticationProvider("challenge", 1, token -> {
            throw new OAuth20ClientAuthenticationResponseException(HttpStatus.BAD_REQUEST.value(),
                Map.of(HttpHeaders.CACHE_CONTROL, "no-store"), "{\"error\":\"use_dpop_nonce\"}");
        });
        val next = newClientAuthenticationProvider("next", 2, token -> Optional.of(new DefaultOAuth20UserProfile("next")));
        val interceptor = newClientAuthenticationInterceptor(challenge, next);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("no-store", response.getHeader(HttpHeaders.CACHE_CONTROL));
        assertEquals("{\"error\":\"use_dpop_nonce\"}", response.getContentAsString());
        assertNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }

    @Test
    void verifyClientAuthenticationWithoutMethods() throws Throwable {
        val interceptor = newClientAuthenticationInterceptor();
        val response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(new MockHttpServletRequest(), response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(OAuth20ClientAuthenticationInterceptor.BASIC_AUTHENTICATION_CHALLENGE, response.getHeader(HttpHeaders.WWW_AUTHENTICATE));
    }

    private OAuth20ClientAuthenticationInterceptor newClientAuthenticationInterceptor(final OAuth20ClientAuthenticationProvider... providers) {
        return new OAuth20ClientAuthenticationInterceptor(
            new DirectObjectProvider<List<OAuth20ClientAuthenticationProvider>>(List.of(providers)),
            new DirectObjectProvider<>(servicesManager),
            new DirectObjectProvider<>(oauthRequestParameterResolver));
    }

    private static OAuth20ClientAuthenticationProvider newClientAuthenticationProvider(final String name, final int order,
                                                                                       final OAuth20ClientCredentialsAuthenticator authenticator) {
        return DefaultOAuth20ClientAuthenticationProvider.builder()
            .name(name)
            .order(order)
            .authenticationConverter(request -> new OAuth20ClientAuthenticationToken(request,
                OAuth20ClientAuthenticationToken.CredentialSource.HEADER, name, CLIENT_SECRET))
            .authenticator(authenticator)
            .build();
    }
}
