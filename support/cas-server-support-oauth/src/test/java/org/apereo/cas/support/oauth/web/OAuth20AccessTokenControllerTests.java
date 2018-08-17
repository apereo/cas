package org.apereo.cas.support.oauth.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.refreshtoken.DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    public void setUp() {
        clearAllServices();
    }

    @Test
    public void verifyClientNoClientId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoRedirectUri() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoAuthorizationCode() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientBadGrantType() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, "badValue");
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientDisallowedGrantType() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.getType());
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoClientSecret() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoCode() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));

        addCode(principal, service);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoCasService() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final OAuthRegisteredService registeredService = getRegisteredService(
                REDIRECT_URI, CLIENT_SECRET, CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        final OAuthCode code = addCode(principal, registeredService);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientRedirectUriDoesNotStartWithServiceId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, OTHER_REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientWrongSecret() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientExpiredCode() throws Exception {
        final RegisteredService registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET,
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        servicesManager.save(registeredService);

        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final Principal principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        final Authentication authentication = getAuthentication(principal);
        final DefaultOAuthCodeFactory expiringOAuthCodeFactory = new DefaultOAuthCodeFactory(new AlwaysExpiresExpirationPolicy());
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final Service service = factory.createService(registeredService.getServiceId());
        final OAuthCode code = expiringOAuthCodeFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(code);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        servicesManager.save(getRegisteredService(REDIRECT_URI, CLIENT_SECRET,
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE)));

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientAuthByParameter() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        internalVerifyClientOK(service, false, false);
    }

    @Test
    public void verifyClientAuthByHeader() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        internalVerifyClientOK(service, false, false);
    }

    @Test
    public void verifyClientAuthByParameterWithRefreshToken() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        internalVerifyClientOK(service, true, false);
    }

    @Test
    public void verifyClientAuthByHeaderWithRefreshToken() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        internalVerifyClientOK(service, true, false);
    }

    @Test
    public void verifyClientAuthJsonByParameter() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setJsonFormat(true);
        internalVerifyClientOK(service, false, true);
    }

    @Test
    public void verifyClientAuthJsonByHeader() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setJsonFormat(true);
        internalVerifyClientOK(service, false, true);
    }

    @Test
    public void verifyClientAuthJsonByParameterWithRefreshToken() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);
        internalVerifyClientOK(service, true, true);
    }

    @Test
    public void verifyClientAuthJsonByHeaderWithRefreshToken() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);
        internalVerifyClientOK(service, true, true);
    }

    @Test
    public void ensureOnlyRefreshTokenIsAcceptedForRefreshGrant() throws Exception {
        addRegisteredService(true, CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD,
                OAuth20GrantTypes.REFRESH_TOKEN));
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(),
                CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        final MockHttpSession mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        String response = mockResponse.getContentAsString();

        final String refreshToken = Arrays.stream(response.split("&"))
            .filter(f -> f.startsWith(OAuth20Constants.REFRESH_TOKEN))
            .map(f -> StringUtils.remove(f, OAuth20Constants.REFRESH_TOKEN + "="))
            .findFirst()
            .get();
        final String accessToken = Arrays.stream(response.split("&"))
            .filter(f -> f.startsWith(OAuth20Constants.ACCESS_TOKEN))
            .map(f -> StringUtils.remove(f, OAuth20Constants.ACCESS_TOKEN + "="))
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
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserNoCasService() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserBadAuthorizationCode() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserBadCredentials() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, "badPassword");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserAuth() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        internalVerifyUserAuth(false, false);
    }

    @Test
    public void verifyUserAuthWithRefreshToken() throws Exception {
        final OAuthRegisteredService registeredService = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        registeredService.setGenerateRefreshToken(true);
        internalVerifyUserAuth(true, false);
    }

    @Test
    public void verifyJsonUserAuth() throws Exception {
        final OAuthRegisteredService registeredService =
                addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        registeredService.setJsonFormat(true);

        internalVerifyUserAuth(false, true);
    }

    @Test
    public void verifyJsonUserAuthWithRefreshToken() throws Exception {
        final OAuthRegisteredService registeredService = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        registeredService.setGenerateRefreshToken(true);
        registeredService.setJsonFormat(true);
        internalVerifyUserAuth(true, true);
    }

    private void internalVerifyUserAuth(final boolean refreshToken, final boolean json) throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(),
                CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        mockRequest.addHeader(CasProtocolConstants.PARAMETER_SERVICE, REDIRECT_URI);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        final String body = mockResponse.getContentAsString();

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

        final AccessToken accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(GOOD_USERNAME, accessToken.getAuthentication().getPrincipal().getId());

        final int timeLeft = getTimeLeft(body, refreshToken, json);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    @Test
    public void verifyRefreshTokenExpiredToken() throws Exception {
        final Principal principal = createPrincipal();
        final RegisteredService registeredService = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        final Authentication authentication = getAuthentication(principal);
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final Service service = factory.createService(registeredService.getServiceId());
        final DefaultRefreshTokenFactory expiringRefreshTokenFactory = new DefaultRefreshTokenFactory(new AlwaysExpiresExpirationPolicy());
        final RefreshToken refreshToken = expiringRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(refreshToken);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenBadCredentials() throws Exception {
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        final RefreshToken refreshToken = addRefreshToken(principal, service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenMissingToken() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(ERROR_EQUALS + OAuth20Constants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenOKWithExpiredTicketGrantingTicket() throws Exception {
        final Principal principal = createPrincipal();
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        final RefreshToken refreshToken = addRefreshToken(principal, service);

        refreshToken.getTicketGrantingTicket().markTicketExpired();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertEquals("text/plain", mockResponse.getContentType());
        final String body = mockResponse.getContentAsString();

        assertTrue(body.contains(OAuth20Constants.ACCESS_TOKEN + '='));
        assertFalse(body.contains(OAuth20Constants.REFRESH_TOKEN + '='));
        assertTrue(body.contains(OAuth20Constants.EXPIRES_IN + '='));

        final String accessTokenId = StringUtils.substringBetween(body, OAuth20Constants.ACCESS_TOKEN + '=', "&");

        final AccessToken accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        final int timeLeft = getTimeLeft(body, false, false);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    @Test
    public void verifyRefreshTokenOK() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        internalVerifyRefreshTokenOk(service, false);
    }

    @Test
    public void verifyRefreshTokenOKWithRefreshToken() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        service.setGenerateRefreshToken(true);
        internalVerifyRefreshTokenOk(service, false);
    }

    @Test
    public void verifyJsonRefreshTokenOK() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        service.setJsonFormat(true);
        internalVerifyRefreshTokenOk(service, true);
    }

    @Test
    public void verifyJsonRefreshTokenOKWithRefreshToken() throws Exception {
        final OAuthRegisteredService service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);
        internalVerifyRefreshTokenOk(service, true);
    }

}
