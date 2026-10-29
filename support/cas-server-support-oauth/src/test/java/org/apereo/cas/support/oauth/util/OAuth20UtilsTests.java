package org.apereo.cas.support.oauth.util;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.FullRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeFactory;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20UtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
class OAuth20UtilsTests extends AbstractOAuth20Tests {
    @Test
    void verifyRequestedTokenReadOncePerRequest() throws Throwable {
        val code = mock(OAuth20Code.class);
        val registry = mock(TicketRegistry.class);
        when(registry.getTicket("OC-1", OAuth20Token.class)).thenReturn(code);
        val request = new MockHttpServletRequest();

        assertSame(code, OAuth20Utils.getRequestedOAuthToken(request, registry, "OC-1", OAuth20Code.class));
        assertSame(code, OAuth20Utils.getRequestedOAuthToken(request, registry, "OC-1", OAuth20Token.class));
        assertNull(OAuth20Utils.getRequestedOAuthToken(request, registry, "OC-1", OAuth20RefreshToken.class));
        verify(registry, times(1)).getTicket("OC-1", OAuth20Token.class);

        when(registry.getTicket("OC-2", OAuth20Token.class)).thenThrow(new InvalidTicketException("OC-2"));
        assertNull(OAuth20Utils.getRequestedOAuthToken(request, registry, "OC-2", OAuth20Code.class));
        assertNull(OAuth20Utils.getRequestedOAuthToken(request, registry, " ", OAuth20Code.class));
    }

    @Test
    void verifyRequestHeaderBad() {
        assertNull(OAuth20Utils.getClientIdFromAuthenticatedProfile(new DefaultOAuth20UserProfile("casuser")));
    }

    @Test
    void verifyUnauthzView() {
        val mv = OAuth20Utils.produceUnauthorizedErrorView();
        assertEquals(HttpStatus.UNAUTHORIZED, mv.getStatus());
    }

    @Test
    void verifyNoClientId() {
        assertNull(OAuth20Utils.getRegisteredOAuthServiceByClientId(mock(ServicesManager.class), null));
    }

    @Test
    void verifyRequestParams() {
        val request = new MockHttpServletRequest();
        request.addParameter("attr1", "value1");
        request.addParameter("attr2", "value2", "value3");
        assertFalse(oauthRequestParameterResolver.resolveRequestParameters(List.of("attr1", "attr2"), request).isEmpty());
    }

    @Test
    void verifyRequestParam() {
        val request = new MockHttpServletRequest();
        request.addParameter("attr1", "value1");
        request.addParameter("attr2", "value2", "value3");
        assertFalse(oauthRequestParameterResolver.resolveRequestParameter(request, "attr1", String.class).isEmpty());
        assertFalse(oauthRequestParameterResolver.resolveRequestParameter(request, "attr2", List.class).isEmpty());
        assertFalse(oauthRequestParameterResolver.resolveRequestParameter(request, "attr2", String[].class).isEmpty());
    }

    @Test
    void verifyRequestParamJwt() {
        val request = new MockHttpServletRequest();

        val claims = new JWTClaimsSet.Builder().subject("cas")
            .claim("scope", new String[]{"openid", "profile"})
            .claim("response", "code")
            .claim("client_id", List.of("client1", "client2"))
            .claim(OAuth20Constants.RESOURCE, "https://api.example.org/")
            .build();
        val jwt = new PlainJWT(claims);
        val jwtString = jwt.serialize();
        request.removeAllParameters();
        request.addParameter(OAuth20Constants.REQUEST, jwtString);

        assertFalse(oauthRequestParameterResolver.resolveRequestParameter(request, "response", String.class).isEmpty());
        assertFalse(oauthRequestParameterResolver.resolveRequestParameter(request, "client_id", List.class).isEmpty());
        assertFalse(oauthRequestParameterResolver.resolveRequestParameter(request, "scope", String[].class).isEmpty());
        assertEquals(Set.of("https://api.example.org/"), oauthRequestParameterResolver.resolveRequestedResources(request));
        assertEquals(List.of("https://api.example.org/"),
            oauthRequestParameterResolver.resolveRequestParameter(request, OAuth20Constants.RESOURCE, List.class).orElseThrow());
    }

    @Test
    void verifyScopes() {
        val request = new MockHttpServletRequest();
        request.addParameter("scope", "openid", "profile");
        assertFalse(oauthRequestParameterResolver.resolveRequestedScopes(request).isEmpty());
        assertTrue(oauthRequestParameterResolver.resolveRequestedScopes(new MockHttpServletRequest()).isEmpty());
    }

    @Test
    void verifyRequestedResources() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.RESOURCE, "https://api.example.org/one", "https://api.example.org/two");

        assertEquals(Set.of("https://api.example.org/one", "https://api.example.org/two"),
            oauthRequestParameterResolver.resolveRequestedResources(request));
    }

    @Test
    void verifyPostResponse() {
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setResponseMode("post");
        assertTrue(OAuth20ResponseModeFactory.isResponseModeTypeFormPost(registeredService, OAuth20ResponseModeTypes.NONE));
        assertTrue(OAuth20Utils.isResponseModeType("form_post", OAuth20ResponseModeTypes.FORM_POST));
    }

    @Test
    void verifyGrants() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.getType());
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setSupportedGrantTypes(CollectionUtils.wrapHashSet(OAuth20GrantTypes.CLIENT_CREDENTIALS.getType()));
        assertTrue(oauthRequestParameterResolver.isAuthorizedGrantTypeForService(request, registeredService));
        assertTrue(OAuth20RequestParameterResolver.isAuthorizedGrantTypeForService(
            OAuth20GrantTypes.PASSWORD.getType(), new OAuthRegisteredService()));
    }

    @Test
    void verifyCheckCallbackValid() {
        val registeredService = new OAuthRegisteredService();
        registeredService.setServiceId("http://test.org/.*");
        registeredService.setMatchingStrategy(null);
        assertFalse(OAuth20Utils.checkCallbackValid(registeredService, "http://test.org/cas"));
        registeredService.setMatchingStrategy(new FullRegexRegisteredServiceMatchingStrategy());
        assertTrue(OAuth20Utils.checkCallbackValid(registeredService, "http://test.org/cas"));
        assertFalse(OAuth20Utils.checkCallbackValid(registeredService, "http://test2.org/cas"));
    }


    @Test
    void verifyServiceHeader() {
        val request = new MockHttpServletRequest();
        request.addHeader("X-".concat(CasProtocolConstants.PARAMETER_SERVICE), RegisteredServiceTestUtils.CONST_TEST_URL);
        val result = OAuth20Utils.getServiceRequestHeaderIfAny(request);
        assertNotNull(result);
    }

    @Test
    void verifyUserInfoClaims() throws Throwable {
        val request = new MockHttpServletRequest();
        val claims = "\"userinfo\": {\"given_name\": {\"essential\": true}}";
        request.addParameter(OAuth20Constants.CLAIMS, claims);
        val result = oauthRequestParameterResolver.resolveRequestClaims(request);
        assertFalse(result.isEmpty());
        val token = mock(OAuth20Token.class);
        when(token.getClaims()).thenReturn(Map.of("userinfo", Map.of("givenName", "CAS")));
        assertFalse(OAuth20Utils.parseUserInfoRequestClaims(token).isEmpty());
    }


    @Test
    void verifyIsAuthorizedResponseTypeForService() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.ID_TOKEN.getType());
        val registeredService = new OAuthRegisteredService();
        val supportedResponseTypes = new HashSet<String>();

        registeredService.setSupportedResponseTypes(supportedResponseTypes);
        assertTrue(oauthRequestParameterResolver.isAuthorizedResponseTypeForService(request, registeredService));

        supportedResponseTypes.add(OAuth20ResponseTypes.IDTOKEN_TOKEN.getType());
        registeredService.setSupportedResponseTypes(supportedResponseTypes);
        assertFalse(oauthRequestParameterResolver.isAuthorizedResponseTypeForService(request, registeredService));

        supportedResponseTypes.add(OAuth20ResponseTypes.ID_TOKEN.getType());
        registeredService.setSupportedResponseTypes(supportedResponseTypes);
        assertTrue(oauthRequestParameterResolver.isAuthorizedResponseTypeForService(request, registeredService));
    }

    @Test
    void verifyFindStatelessRequest() {
        val profile = new DefaultOAuth20UserProfile("casuser");
        assertFalse(OAuth20Utils.isStatelessAuthentication(profile));
        profile.addAttribute(Principal.class.getName(), RegisteredServiceTestUtils.getPrincipal("casuser"));
        assertFalse(OAuth20Utils.isStatelessAuthentication(profile));
        profile.addAttribute(OAuth20Constants.CAS_OAUTH_STATELESS_PROPERTY, Boolean.TRUE);
        assertTrue(OAuth20Utils.isStatelessAuthentication(profile));
    }

    @Test
    void verifyClientIdFromParameter() {
        val manager = mock(ServicesManager.class);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("another:" + CLIENT_SECRET));
        assertEquals("client", OAuth20Utils.resolveClientIdFromRequest(request, manager, oauthRequestParameterResolver));
        verifyNoInteractions(manager);
    }

    @Test
    void verifyClientIdFromBasicAuthentication() {
        val registeredService = addRegisteredService();
        val manager = mock(ServicesManager.class);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64(registeredService.getClientId() + ':' + CLIENT_SECRET));
        assertEquals(registeredService.getClientId(), OAuth20Utils.resolveClientIdFromRequest(request, manager, oauthRequestParameterResolver));

        val lowerCaseRequest = new MockHttpServletRequest();
        lowerCaseRequest.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        lowerCaseRequest.addHeader(HttpHeaders.AUTHORIZATION, "basic " + EncodingUtils.encodeBase64(registeredService.getClientId() + ':' + CLIENT_SECRET));
        assertEquals(registeredService.getClientId(), OAuth20Utils.resolveClientIdFromRequest(lowerCaseRequest, manager, oauthRequestParameterResolver));
        val credentials = OAuth20Utils.readBasicAuthorization(lowerCaseRequest).orElseThrow();
        assertEquals(registeredService.getClientId(), credentials.getLeft());
        assertEquals(CLIENT_SECRET, credentials.getRight());

        val encodedRequest = new MockHttpServletRequest();
        encodedRequest.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("client%3A1:se%2Bcret+value"));
        val decodedCredentials = OAuth20Utils.readClientBasicAuthorization(encodedRequest).orElseThrow();
        assertEquals("client:1", decodedCredentials.getLeft());
        assertEquals("se+cret value", decodedCredentials.getRight());
        assertEquals("client%3A1", OAuth20Utils.readBasicAuthorization(encodedRequest).orElseThrow().getLeft());
        verify(manager, never()).getAllServicesOfType(any());
    }

    @Test
    void verifyBearerAuthorizationHeaderIsIgnored() {
        verifyAuthorizationHeaderFallsBackToRedirectUri("Bearer AT-1-123456");
    }

    @Test
    void verifyMalformedBasicAuthorizationHeaderIsIgnored() {
        verifyAuthorizationHeaderFallsBackToRedirectUri("Basic " + EncodingUtils.encodeBase64("no-separator"));
    }

    @Test
    void verifyRedirectUriLookupOncePerRequest() {
        val registeredService = addRegisteredService();
        val manager = mock(ServicesManager.class);
        when(manager.getAllServicesOfType(OAuthRegisteredService.class)).thenReturn(List.of(registeredService));
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        assertEquals(registeredService.getClientId(), OAuth20Utils.resolveClientIdFromRequest(request, manager, oauthRequestParameterResolver));
        assertEquals(registeredService.getClientId(), OAuth20Utils.resolveClientIdFromRequest(request, manager, oauthRequestParameterResolver));
        verify(manager, times(1)).getAllServicesOfType(OAuthRegisteredService.class);
    }

    @Test
    void verifyClientIdFromClientAssertion() {
        val registeredService = addRegisteredService();
        val manager = mock(ServicesManager.class);
        val assertion = new PlainJWT(new JWTClaimsSet.Builder()
            .issuer(registeredService.getClientId())
            .subject(registeredService.getClientId())
            .build()).serialize();
        val request = newRequestWithClientAssertion(assertion);
        assertEquals(registeredService.getClientId(), OAuth20Utils.resolveClientIdFromRequest(request, manager, oauthRequestParameterResolver));
        verify(manager, never()).getAllServicesOfType(any());
    }

    @Test
    void verifyClientAssertionWithMismatchedIssuerIsIgnored() {
        val registeredService = addRegisteredService();
        val manager = mock(ServicesManager.class);
        when(manager.getAllServicesOfType(OAuthRegisteredService.class)).thenReturn(List.of(registeredService));
        val assertion = new PlainJWT(new JWTClaimsSet.Builder()
            .issuer("another-client")
            .subject(registeredService.getClientId())
            .build()).serialize();
        val request = newRequestWithClientAssertion(assertion);
        assertEquals(registeredService.getClientId(), OAuth20Utils.resolveClientIdFromRequest(request, manager, oauthRequestParameterResolver));
        verify(manager).getAllServicesOfType(OAuthRegisteredService.class);
    }

    @Test
    void verifyNoClientIdResolved() {
        val manager = mock(ServicesManager.class);
        when(manager.getAllServicesOfType(OAuthRegisteredService.class)).thenReturn(List.of());
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        assertEquals(StringUtils.EMPTY, OAuth20Utils.resolveClientIdFromRequest(request, manager, oauthRequestParameterResolver));
    }

    @Test
    void verifyAccessTokenTimeout() {
        val accessToken = getAccessToken();
        when(accessToken.getExpiresIn()).thenReturn(60L);
        val result = OAuth20TokenGeneratedResult.builder().accessToken(accessToken).build();
        val timeout = OAuth20Utils.getAccessTokenTimeout(result);
        assertEquals(accessToken.getExpiresIn(), timeout);
    }

    @Test
    void verifyStatelessAccessTokenTimeout() {
        val accessToken = getAccessToken();
        when(accessToken.getExpiresIn()).thenReturn(60L);
        when(accessToken.isStateless()).thenReturn(Boolean.TRUE);
        val result = OAuth20TokenGeneratedResult.builder().accessToken(accessToken).build();
        val timeout = OAuth20Utils.getAccessTokenTimeout(result);
        assertTrue(timeout > 0);
        assertNotEquals(accessToken.getExpiresIn(), timeout);
    }

    private void verifyAuthorizationHeaderFallsBackToRedirectUri(final String authorization) {
        val registeredService = addRegisteredService();
        val manager = mock(ServicesManager.class);
        when(manager.getAllServicesOfType(OAuthRegisteredService.class)).thenReturn(List.of(registeredService));
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        assertEquals(registeredService.getClientId(), OAuth20Utils.resolveClientIdFromRequest(request, manager, oauthRequestParameterResolver));
    }

    private static MockHttpServletRequest newRequestWithClientAssertion(final String assertion) {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.CLIENT_ASSERTION_TYPE, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER);
        request.addParameter(OAuth20Constants.CLIENT_ASSERTION, assertion);
        return request;
    }
}
