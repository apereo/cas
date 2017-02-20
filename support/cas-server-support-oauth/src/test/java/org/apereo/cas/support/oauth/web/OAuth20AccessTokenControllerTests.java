package org.apereo.cas.support.oauth.web;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
public class OAuth20AccessTokenControllerTests extends AbstractOAuth20Tests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String CLIENT_ID = "1";

    private static final String CLIENT_SECRET = "secret";

    private static final String WRONG_CLIENT_SECRET = "wrongSecret";

    private static final String REDIRECT_URI = "http://someurl";

    private static final String OTHER_REDIRECT_URI = "http://someotherurl";

    private static final int TIMEOUT = 7200;

    private static final String ID = "1234";

    private static final String NAME = "attributeName";

    private static final String NAME2 = "attributeName2";

    private static final String VALUE = "attributeValue";

    private static final String USERNAME = "username";

    private static final String PASSWORD = "password";

    private static final String GOOD_USERNAME = "test";

    private static final String GOOD_PASSWORD = "test";

    private static final int DELTA = 2;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private OAuthCodeFactory oAuthCodeFactory;

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    private RefreshTokenFactory oAuthRefreshTokenFactory;

    @Autowired
    @Qualifier("accessTokenController")
    private OAuth20AccessTokenEndpointController oAuth20AccessTokenController;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("requiresAuthenticationAccessTokenInterceptor")
    private SecurityInterceptor requiresAuthenticationInterceptor;

    @Before
    public void setUp() {
        clearAllServices();
    }

    //
    //
    // authorization code grant type tests
    //
    //
    @Test
    public void verifyClientNoClientId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoRedirectUri() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoAuthorizationCode() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientBadAuthorizationCode() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, "badValue");
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoClientSecret() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoCode() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();

        addCode(principal, service);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoCasService() throws Exception {

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        final OAuthCode code = addCode(principal, registeredService);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientRedirectUriDoesNotStartWithServiceId() throws Exception {

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, OTHER_REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientWrongSecret() throws Exception {

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientExpiredCode() throws Exception {

        final RegisteredService registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
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
        final OAuthCode code = expiringOAuthCodeFactory.create(service, authentication);
        oAuth20AccessTokenController.getTicketRegistry().addTicket(code);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        servicesManager.save(getRegisteredService(REDIRECT_URI, CLIENT_SECRET));

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_GRANT, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientAuthByParameter() throws Exception {

        final RegisteredService service = addRegisteredService();

        internalVerifyClientOK(service, false, false, false);
    }

    @Test
    public void verifyClientAuthByHeader() throws Exception {

        final RegisteredService service = addRegisteredService();

        internalVerifyClientOK(service, true, false, false);
    }

    @Test
    public void verifyClientAuthByParameterWithRefreshToken() throws Exception {

        final OAuthRegisteredService service = addRegisteredService();
        service.setGenerateRefreshToken(true);

        internalVerifyClientOK(service, false, true, false);
    }

    @Test
    public void verifyClientAuthByHeaderWithRefreshToken() throws Exception {

        final OAuthRegisteredService service = addRegisteredService();
        service.setGenerateRefreshToken(true);

        internalVerifyClientOK(service, true, true, false);
    }

    @Test
    public void verifyClientAuthJsonByParameter() throws Exception {

        final OAuthRegisteredService service = addRegisteredService();
        service.setJsonFormat(true);

        internalVerifyClientOK(service, false, false, true);
    }

    @Test
    public void verifyClientAuthJsonByHeader() throws Exception {

        final OAuthRegisteredService service = addRegisteredService();
        service.setJsonFormat(true);

        internalVerifyClientOK(service, true, false, true);
    }

    @Test
    public void verifyClientAuthJsonByParameterWithRefreshToken() throws Exception {

        final OAuthRegisteredService service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);

        internalVerifyClientOK(service, false, true, true);
    }

    @Test
    public void verifyClientAuthJsonByHeaderWithRefreshToken() throws Exception {

        final OAuthRegisteredService service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);

        internalVerifyClientOK(service, true, true, true);
    }

    private void internalVerifyClientOK(final RegisteredService service, final boolean basicAuth, final boolean refreshToken,
                                        final boolean json)
            throws Exception {

        final Principal principal = createPrincipal();
        final OAuthCode code = addCode(principal, service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        if (basicAuth) {
            final String auth = CLIENT_ID + ':' + CLIENT_SECRET;
            final String value = Base64.encodeBase64String(auth.getBytes(StandardCharsets.UTF_8));
            mockRequest.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);
        } else {
            mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
            mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        }
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertNull(oAuth20AccessTokenController.getTicketRegistry().getTicket(code.getId()));
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        final String body = mockResponse.getContentAsString();

        final String accessTokenId;
        if (json) {
            assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());
            assertTrue(body.contains('"' + OAuthConstants.ACCESS_TOKEN + "\":\"AT-"));
            if (refreshToken) {
                assertTrue(body.contains('"' + OAuthConstants.REFRESH_TOKEN + "\":\"RT-"));
            }
            assertTrue(body.contains('"' + OAuthConstants.EXPIRES_IN + "\":7"));
            accessTokenId = StringUtils.substringBetween(body, OAuthConstants.ACCESS_TOKEN + "\":\"", "\",\"");
        } else {
            assertEquals(MediaType.TEXT_PLAIN_VALUE, mockResponse.getContentType());
            assertTrue(body.contains(OAuthConstants.ACCESS_TOKEN + "=AT-"));
            if (refreshToken) {
                assertTrue(body.contains(OAuthConstants.REFRESH_TOKEN + "=RT-"));
            }
            assertTrue(body.contains(OAuthConstants.EXPIRES_IN + '='));
            accessTokenId = StringUtils.substringBetween(body, OAuthConstants.ACCESS_TOKEN + '=', "&");
        }

        final AccessToken accessToken = oAuth20AccessTokenController.getTicketRegistry().getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        final int timeLeft = getTimeLeft(body, refreshToken, json);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    private static int getTimeLeft(final String body, final boolean refreshToken, final boolean json) {
        final int timeLeft;
        if (json) {
            if (refreshToken) {
                timeLeft = Integer.parseInt(StringUtils.substringBetween(body, OAuthConstants.EXPIRES_IN + "\":", ","));
            } else {
                timeLeft = Integer.parseInt(StringUtils.substringBetween(body, OAuthConstants.EXPIRES_IN + "\":", "}"));
            }
        } else {
            if (refreshToken) {
                timeLeft = Integer.parseInt(StringUtils.substringBetween(body, '&' + OAuthConstants.EXPIRES_IN + '=',
                        '&' + OAuthConstants.REFRESH_TOKEN));
            } else {
                timeLeft = Integer.parseInt(StringUtils.substringAfter(body, '&' + OAuthConstants.EXPIRES_IN + '='));
            }
        }
        return timeLeft;
    }

    //
    //
    // resource owner password grant type tests
    //
    //

    @Test
    public void verifyUserNoClientId() throws Exception {

        addRegisteredService();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserNoCasService() throws Exception {

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserBadAuthorizationCode() throws Exception {

        addRegisteredService();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserBadCredentials() throws Exception {

        addRegisteredService();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, "badPassword");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserAuth() throws Exception {

        addRegisteredService();

        internalVerifyUserAuth(false, false);
    }

    @Test
    public void verifyUserAuthWithRefreshToken() throws Exception {
        final OAuthRegisteredService registeredService = addRegisteredService();
        registeredService.setGenerateRefreshToken(true);
        internalVerifyUserAuth(true, false);
    }

    @Test
    public void verifyJsonUserAuth() throws Exception {

        final OAuthRegisteredService registeredService = addRegisteredService();
        registeredService.setJsonFormat(true);

        internalVerifyUserAuth(false, true);
    }

    @Test
    public void verifyJsonUserAuthWithRefreshToken() throws Exception {

        final OAuthRegisteredService registeredService = addRegisteredService();
        registeredService.setGenerateRefreshToken(true);
        registeredService.setJsonFormat(true);

        internalVerifyUserAuth(true, true);
    }

    private void internalVerifyUserAuth(final boolean refreshToken, final boolean json) throws Exception {

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        final String body = mockResponse.getContentAsString();

        final String accessTokenId;
        if (json) {
            assertEquals("application/json", mockResponse.getContentType());
            assertTrue(body.contains('"' + OAuthConstants.ACCESS_TOKEN + "\":\"AT-"));
            if (refreshToken) {
                assertTrue(body.contains('"' + OAuthConstants.REFRESH_TOKEN + "\":\"RT-"));
            }
            assertTrue(body.contains('"' + OAuthConstants.EXPIRES_IN + "\":7"));
            accessTokenId = StringUtils.substringBetween(body, OAuthConstants.ACCESS_TOKEN + "\":\"", "\",\"");
        } else {
            assertEquals("text/plain", mockResponse.getContentType());
            assertTrue(body.contains(OAuthConstants.ACCESS_TOKEN + '='));
            if (refreshToken) {
                assertTrue(body.contains(OAuthConstants.REFRESH_TOKEN + '='));
            }
            assertTrue(body.contains(OAuthConstants.EXPIRES_IN + '='));
            accessTokenId = StringUtils.substringBetween(body, OAuthConstants.ACCESS_TOKEN + '=', "&");
        }

        final AccessToken accessToken = oAuth20AccessTokenController.getTicketRegistry().getTicket(accessTokenId, AccessToken.class);
        assertEquals(GOOD_USERNAME, accessToken.getAuthentication().getPrincipal().getId());

        final int timeLeft = getTimeLeft(body, refreshToken, json);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    //
    //
    // refresh token grant type tests
    //
    //
    @Test
    public void verifyRefreshTokenExpiredToken() throws Exception {

        final Principal principal = createPrincipal();
        final RegisteredService registeredService = addRegisteredService();
        final Authentication authentication = getAuthentication(principal);
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final Service service = factory.createService(registeredService.getServiceId());
        final DefaultRefreshTokenFactory expiringRefreshTokenFactory = new DefaultRefreshTokenFactory(new AlwaysExpiresExpirationPolicy());
        final RefreshToken refreshToken = expiringRefreshTokenFactory.create(service, authentication);
        oAuth20AccessTokenController.getTicketRegistry().addTicket(refreshToken);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REFRESH_TOKEN, refreshToken.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_GRANT, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenBadCredentials() throws Exception {

        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final RefreshToken refreshToken = addRefreshToken(principal, service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REFRESH_TOKEN, refreshToken.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenMissingToken() throws Exception {

        addRegisteredService();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRefreshTokenOK() throws Exception {

        final RegisteredService service = addRegisteredService();

        internalVerifyRefreshTokenOk(service, false);
    }

    @Test
    public void verifyRefreshTokenOKWithRefreshToken() throws Exception {

        final OAuthRegisteredService service = addRegisteredService();
        service.setGenerateRefreshToken(true);

        internalVerifyRefreshTokenOk(service, false);
    }

    @Test
    public void verifyJsonRefreshTokenOK() throws Exception {

        final OAuthRegisteredService service = addRegisteredService();
        service.setJsonFormat(true);

        internalVerifyRefreshTokenOk(service, true);
    }

    @Test
    public void verifyJsonRefreshTokenOKWithRefreshToken() throws Exception {

        final OAuthRegisteredService service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);

        internalVerifyRefreshTokenOk(service, true);
    }

    private void internalVerifyRefreshTokenOk(final RegisteredService service, final boolean json) throws Exception {

        final Principal principal = createPrincipal();
        final RefreshToken refreshToken = addRefreshToken(principal, service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.REFRESH_TOKEN, refreshToken.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequestInternal(mockRequest, mockResponse);
        //This assert fails because deep down inside Oauth2 access token ctrl the refresh token gets deleted
        //assertNotNull(oAuth20AccessTokenController.getRegistry().getTicket((refreshToken.getId())));
        assertEquals(200, mockResponse.getStatus());
        final String body = mockResponse.getContentAsString();

        final String accessTokenId;
        if (json) {
            assertEquals("application/json", mockResponse.getContentType());
            assertTrue(body.contains('"' + OAuthConstants.ACCESS_TOKEN + "\":\"AT-"));
            assertFalse(body.contains('"' + OAuthConstants.REFRESH_TOKEN + "\":\"RT-"));
            assertTrue(body.contains('"' + OAuthConstants.EXPIRES_IN + "\":7"));
            accessTokenId = StringUtils.substringBetween(body, OAuthConstants.ACCESS_TOKEN + "\":\"", "\",\"");
        } else {
            assertEquals("text/plain", mockResponse.getContentType());
            assertTrue(body.contains(OAuthConstants.ACCESS_TOKEN + '='));
            assertFalse(body.contains(OAuthConstants.REFRESH_TOKEN + '='));
            assertTrue(body.contains(OAuthConstants.EXPIRES_IN + '='));
            accessTokenId = StringUtils.substringBetween(body, OAuthConstants.ACCESS_TOKEN + '=', "&");
        }

        final AccessToken accessToken = oAuth20AccessTokenController.getTicketRegistry().getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        final int timeLeft = getTimeLeft(body, false, json);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    private static Principal createPrincipal() {
        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        return CoreAuthenticationTestUtils.getPrincipal(ID, map);
    }

    private OAuthRegisteredService addRegisteredService() {
        final OAuthRegisteredService registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        servicesManager.save(registeredService);
        return registeredService;
    }

    private OAuthCode addCode(final Principal principal, final RegisteredService registeredService) {
        final Authentication authentication = getAuthentication(principal);
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final Service service = factory.createService(registeredService.getServiceId());
        final OAuthCode code = oAuthCodeFactory.create(service, authentication);
        oAuth20AccessTokenController.getTicketRegistry().addTicket(code);
        return code;
    }

    private RefreshToken addRefreshToken(final Principal principal, final RegisteredService registeredService) {
        final Authentication authentication = getAuthentication(principal);
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final Service service = factory.createService(registeredService.getServiceId());
        final RefreshToken refreshToken = oAuthRefreshTokenFactory.create(service, authentication);
        oAuth20AccessTokenController.getTicketRegistry().addTicket(refreshToken);
        return refreshToken;
    }

    private static OAuthRegisteredService getRegisteredService(final String serviceId, final String secret) {
        final OAuthRegisteredService registeredServiceImpl = new OAuthRegisteredService();
        registeredServiceImpl.setName("The registered service name");
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);
        registeredServiceImpl.setClientSecret(secret);
        registeredServiceImpl.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        return registeredServiceImpl;
    }

    private void clearAllServices() {
        final Collection<RegisteredService> col = servicesManager.getAllServices();
        col.forEach(r -> servicesManager.delete(r.getId()));
        servicesManager.load();
    }

    private static Authentication getAuthentication(final Principal principal) {
        final CredentialMetaData metadata = new BasicCredentialMetaData(
                new BasicIdentifiableCredential(principal.getId()));
        final HandlerResult handlerResult = new DefaultHandlerResult(principal.getClass().getCanonicalName(),
                metadata, principal, new ArrayList<>());

        return DefaultAuthenticationBuilder.newInstance()
                .setPrincipal(principal)
                .setAuthenticationDate(ZonedDateTime.now())
                .addCredential(metadata)
                .addSuccess(principal.getClass().getCanonicalName(), handlerResult)
                .build();
    }
}
