package org.apereo.cas.support.oauth.web;

import lombok.val;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.junit.Test;
import org.junit.Before;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This class tests the {@link OAuth20AccessTokenEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Slf4j
public class OAuth20AccessTokenControllerTests extends AbstractOAuth20Tests {

    @Before
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyClientNoClientId() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService();
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoRedirectUri() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService();
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoAuthorizationCode() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        val principal = createPrincipal();
        val service = addRegisteredService();
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientBadAuthorizationCode() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, "badValue");
        val principal = createPrincipal();
        val service = addRegisteredService();
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoClientSecret() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService();
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoCode() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService();

        addCode(principal, service);

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoCasService() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        val code = addCode(principal, registeredService);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientRedirectUriDoesNotStartWithServiceId() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, OTHER_REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService();
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientWrongSecret() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService();
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientExpiredCode() throws Exception {
        val registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        servicesManager.save(registeredService);

        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        val list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        val principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        val authentication = getAuthentication(principal);
        val expiringOAuthCodeFactory = new DefaultOAuthCodeFactory(new AlwaysExpiresExpirationPolicy());
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getServiceId());
        val code = expiringOAuthCodeFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(code);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        servicesManager.save(getRegisteredService(REDIRECT_URI, CLIENT_SECRET));

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientAuthByParameter() throws Exception {
        val service = addRegisteredService();
        internalVerifyClientOK(service, false, false);
    }

    @Test
    public void verifyClientAuthByHeader() throws Exception {
        val service = addRegisteredService();
        internalVerifyClientOK(service, false, false);
    }

    @Test
    public void verifyClientAuthByParameterWithRefreshToken() throws Exception {
        val service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        internalVerifyClientOK(service, true, false);
    }

    @Test
    public void verifyClientAuthByHeaderWithRefreshToken() throws Exception {
        val service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        internalVerifyClientOK(service, true, false);
    }

    @Test
    public void verifyClientAuthJsonByParameter() throws Exception {
        val service = addRegisteredService();
        service.setJsonFormat(true);
        internalVerifyClientOK(service, false, true);
    }

    @Test
    public void verifyClientAuthJsonByHeader() throws Exception {
        val service = addRegisteredService();
        service.setJsonFormat(true);
        internalVerifyClientOK(service, false, true);
    }

    @Test
    public void verifyClientAuthJsonByParameterWithRefreshToken() throws Exception {
        val service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);
        internalVerifyClientOK(service, true, true);
    }

    @Test
    public void verifyClientAuthJsonByHeaderWithRefreshToken() throws Exception {
        val service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);
        internalVerifyClientOK(service, true, true);
    }

    @Test
    public void ensureOnlyRefreshTokenIsAcceptedForRefreshGrant() throws Exception {
        addRegisteredService(true);
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        val mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);

        var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        var response = mockResponse.getContentAsString();

        val refreshToken = Arrays.stream(response.split("&"))
            .filter(f -> f.startsWith(OAuth20Constants.REFRESH_TOKEN))
            .map(f -> StringUtils.remove(f, OAuth20Constants.REFRESH_TOKEN + '='))
            .findFirst()
            .get();
        val accessToken = Arrays.stream(response.split("&"))
            .filter(f -> f.startsWith(OAuth20Constants.ACCESS_TOKEN))
            .map(f -> StringUtils.remove(f, OAuth20Constants.ACCESS_TOKEN + '='))
            .findFirst()
            .get();

        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, accessToken);

        mockResponse = new MockHttpServletResponse();
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());

        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken);
        mockResponse = new MockHttpServletResponse();
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        response = mockResponse.getContentAsString();
        assertTrue(response.contains(OAuth20Constants.ACCESS_TOKEN));
    }

    @Test
    public void verifyUserNoClientId() throws Exception {
        addRegisteredService();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserNoCasService() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserBadAuthorizationCode() throws Exception {
        addRegisteredService();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserBadCredentials() throws Exception {
        addRegisteredService();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, "badPassword");
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserAuth() throws Exception {
        addRegisteredService();
        internalVerifyUserAuth(false, false);
    }

    @Test
    public void verifyUserAuthWithRefreshToken() throws Exception {
        val registeredService = addRegisteredService();
        registeredService.setGenerateRefreshToken(true);
        internalVerifyUserAuth(true, false);
    }

    @Test
    public void verifyJsonUserAuth() throws Exception {
        val registeredService = addRegisteredService();
        registeredService.setJsonFormat(true);

        internalVerifyUserAuth(false, true);
    }

    @Test
    public void verifyJsonUserAuthWithRefreshToken() throws Exception {
        val registeredService = addRegisteredService();
        registeredService.setGenerateRefreshToken(true);
        registeredService.setJsonFormat(true);
        internalVerifyUserAuth(true, true);
    }

    private void internalVerifyUserAuth(final boolean refreshToken, final boolean json) throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        mockRequest.addHeader(CasProtocolConstants.PARAMETER_SERVICE, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        val body = mockResponse.getContentAsString();

        final String accessTokenId;
        if (json) {
            assertEquals("application/json", mockResponse.getContentType());
            assertTrue(body.contains('"' + OAuth20Constants.ACCESS_TOKEN + "\":\"AT-"));
            if (refreshToken) {
                assertTrue(body.contains('"' + OAuth20Constants.REFRESH_TOKEN + "\":\"RT-"));
            }
            assertTrue(body.contains('"' + OAuth20Constants.EXPIRES_IN + "\":"));
            accessTokenId = StringUtils.substringBetween(body, OAuth20Constants.ACCESS_TOKEN + "\":\"", "\",\"");
        } else {
            assertEquals("text/plain", mockResponse.getContentType());
            assertTrue(body.contains(OAuth20Constants.ACCESS_TOKEN + '='));
            if (refreshToken) {
                assertTrue(body.contains(OAuth20Constants.REFRESH_TOKEN + '='));
            }
            assertTrue(body.contains(OAuth20Constants.EXPIRES_IN + '='));
            accessTokenId = StringUtils.substringBetween(body, OAuth20Constants.ACCESS_TOKEN + '=', "&");
        }

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(GOOD_USERNAME, accessToken.getAuthentication().getPrincipal().getId());

        val timeLeft = getTimeLeft(body, refreshToken, json);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    @Test
    public void verifyRefreshTokenExpiredToken() throws Exception {
        val principal = createPrincipal();
        val registeredService = addRegisteredService();
        val authentication = getAuthentication(principal);
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getServiceId());
        val expiringRefreshTokenFactory = new DefaultRefreshTokenFactory(new AlwaysExpiresExpirationPolicy());
        val refreshToken = expiringRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(refreshToken);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenBadCredentials() throws Exception {
        val principal = createPrincipal();
        val service = addRegisteredService();
        val refreshToken = addRefreshToken(principal, service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenMissingToken() throws Exception {
        addRegisteredService();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenOKWithExpiredTicketGrantingTicket() throws Exception {
        val principal = createPrincipal();
        val service = addRegisteredService();
        val refreshToken = addRefreshToken(principal, service);

        refreshToken.getTicketGrantingTicket().markTicketExpired();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        val body = mockResponse.getContentAsString();

        assertTrue(body.contains(OAuth20Constants.ACCESS_TOKEN + '='));
        assertFalse(body.contains(OAuth20Constants.REFRESH_TOKEN + '='));
        assertTrue(body.contains(OAuth20Constants.EXPIRES_IN + '='));

        val accessTokenId = StringUtils.substringBetween(body, OAuth20Constants.ACCESS_TOKEN + '=', "&");

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        val timeLeft = getTimeLeft(body, false, false);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    @Test
    public void verifyRefreshTokenOK() throws Exception {
        val service = addRegisteredService();
        internalVerifyRefreshTokenOk(service, false);
    }

    @Test
    public void verifyRefreshTokenOKWithRefreshToken() throws Exception {
        val service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        internalVerifyRefreshTokenOk(service, false);
    }

    @Test
    public void verifyJsonRefreshTokenOK() throws Exception {
        val service = addRegisteredService();
        service.setJsonFormat(true);
        internalVerifyRefreshTokenOk(service, true);
    }

    @Test
    public void verifyJsonRefreshTokenOKWithRefreshToken() throws Exception {
        val service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);
        internalVerifyRefreshTokenOk(service, true);
    }

}
