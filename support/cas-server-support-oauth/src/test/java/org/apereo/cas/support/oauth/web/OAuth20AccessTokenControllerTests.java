package org.apereo.cas.support.oauth.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20DeviceUserCodeApprovalEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link OAuth20AccessTokenEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
public class OAuth20AccessTokenControllerTests extends AbstractOAuth20Tests {

    @BeforeEach
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
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyClientNoRedirectUri() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyClientNoAuthorizationCode() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyClientBadGrantType() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, "badValue");
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyClientDisallowedGrantType() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.getType());
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyClientNoClientSecret() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyClientNoCode() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));

        addCode(principal, service);

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyClientNoCasService() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val registeredService = getRegisteredService(
                REDIRECT_URI, CLIENT_SECRET, CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, registeredService);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyClientRedirectUriDoesNotStartWithServiceId() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, OTHER_REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyClientWrongSecret() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyClientEmptySecret() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, StringUtils.EMPTY);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE), StringUtils.EMPTY);
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
    }

    @Test
    public void verifyClientExpiredCode() throws Exception {
        val registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET,
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        servicesManager.save(registeredService);

        val map = new HashMap<String, Object>();
        map.put(NAME, VALUE);
        val list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        val principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        val authentication = getAuthentication(principal);
        val expiringOAuthCodeFactory = new DefaultOAuthCodeFactory(new AlwaysExpiresExpirationPolicy());
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getServiceId());
        val code = expiringOAuthCodeFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>(), null, null);
        this.ticketRegistry.addTicket(code);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        servicesManager.save(getRegisteredService(REDIRECT_URI, CLIENT_SECRET,
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE)));

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyClientAuthByParameter() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        internalVerifyClientOK(service, false);
    }

    @Test
    public void verifyDeviceFlowGeneratesCode() throws Exception {
        addRegisteredService();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        val model = mv.getModel();
        assertTrue(model.containsKey(OAuth20Constants.DEVICE_CODE));
        assertTrue(model.containsKey(OAuth20Constants.DEVICE_VERIFICATION_URI));
        assertTrue(model.containsKey(OAuth20Constants.DEVICE_USER_CODE));
        assertTrue(model.containsKey(OAuth20Constants.DEVICE_INTERVAL));
        assertTrue(model.containsKey(OAuth20Constants.EXPIRES_IN));

        val devCode = model.get(OAuth20Constants.DEVICE_CODE).toString();
        val userCode = model.get(OAuth20Constants.DEVICE_USER_CODE).toString();

        val devReq = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.DEVICE_AUTHZ_URL);
        devReq.setParameter(OAuth20DeviceUserCodeApprovalEndpointController.PARAMETER_USER_CODE, userCode);
        val devResp = new MockHttpServletResponse();
        val mvDev = deviceController.handlePostRequest(devReq, devResp);
        assertTrue(mvDev.getStatus().is2xxSuccessful());

        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
        mockRequest.setParameter(OAuth20Constants.CODE, devCode);
        val approveResp = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, approveResp, null);
        val mvApproved = controller.handleRequest(mockRequest, approveResp);
        assertTrue(mvApproved.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        assertTrue(mvApproved.getModel().containsKey(OAuth20Constants.EXPIRES_IN));
        assertTrue(mvApproved.getModel().containsKey(OAuth20Constants.TOKEN_TYPE));
    }

    @Test
    public void verifyClientAuthByHeader() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        internalVerifyClientOK(service, false);
    }

    @Test
    public void verifyClientAuthByParameterWithRefreshToken() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        internalVerifyClientOK(service, true);
    }

    @Test
    public void verifyClientAuthByHeaderWithRefreshToken() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        internalVerifyClientOK(service, true);
    }

    @Test
    public void verifyClientAuthJsonByParameter() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        internalVerifyClientOK(service, false);
    }

    @Test
    public void verifyClientAuthJsonByHeader() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        internalVerifyClientOK(service, false);
    }

    @Test
    public void verifyClientAuthJsonByParameterWithRefreshToken() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        internalVerifyClientOK(service, true);
    }

    @Test
    public void verifyClientAuthJsonByHeaderWithRefreshToken() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        internalVerifyClientOK(service, true);
    }

    @Test
    public void ensureOnlyRefreshTokenIsAcceptedForRefreshGrant() throws Exception {
        addRegisteredService(true, CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD,
                OAuth20GrantTypes.REFRESH_TOKEN));
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        val mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);

        var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        var mv = controller.handleRequest(mockRequest, mockResponse);

        assertTrue(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        val refreshToken = mv.getModel().get(OAuth20Constants.REFRESH_TOKEN).toString();
        val accessToken = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, accessToken);

        mockResponse = new MockHttpServletResponse();
        controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());

        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken);
        mockResponse = new MockHttpServletResponse();
        mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
    }

    @Test
    public void verifyUserNoClientId() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
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
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyUserBadAuthorizationCode() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyUserBadCredentials() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, "badPassword");
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyUserAuth() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        internalVerifyUserAuth(false);
    }

    @Test
    public void verifyUserAuthWithRefreshToken() throws Exception {
        val registeredService = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        registeredService.setGenerateRefreshToken(true);
        internalVerifyUserAuth(true);
    }

    @Test
    public void verifyJsonUserAuth() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        internalVerifyUserAuth(false);
    }

    @Test
    public void verifyJsonUserAuthWithRefreshToken() throws Exception {
        val registeredService = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        registeredService.setGenerateRefreshToken(true);
        internalVerifyUserAuth(true);
    }

    private void internalVerifyUserAuth(final boolean refreshToken) throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        mockRequest.addHeader(CasProtocolConstants.PARAMETER_SERVICE, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        var accessTokenId = StringUtils.EMPTY;
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        if (refreshToken) {
            assertTrue(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
        }
        assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));

        accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(GOOD_USERNAME, accessToken.getAuthentication().getPrincipal().getId());

        val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    @Test
    public void verifyRefreshTokenExpiredToken() throws Exception {
        val principal = createPrincipal();
        val registeredService = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
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
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyRefreshTokenBadCredentials() throws Exception {
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        val refreshToken = addRefreshToken(principal, service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyRefreshTokenEmptySecret() throws Exception {
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN), StringUtils.EMPTY);
        val refreshToken = addRefreshToken(principal, service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, StringUtils.EMPTY);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
    }

    @Test
    public void verifyRefreshTokenMissingToken() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyRefreshTokenOKWithExpiredTicketGrantingTicket() throws Exception {
        val principal = createPrincipal();
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        val refreshToken = addRefreshToken(principal, service);

        refreshToken.getTicketGrantingTicket().markTicketExpired();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());

        val accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    @Test
    public void verifyRefreshTokenOK() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        internalVerifyRefreshTokenOk(service);
    }

    @Test
    public void verifyRefreshTokenOKWithRefreshToken() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        service.setGenerateRefreshToken(true);
        internalVerifyRefreshTokenOk(service);
    }

    @Test
    public void verifyJsonRefreshTokenOK() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));

        internalVerifyRefreshTokenOk(service);
    }

    @Test
    public void verifyJsonRefreshTokenOKWithRefreshToken() throws Exception {
        val service = addRegisteredService(
                CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        service.setGenerateRefreshToken(true);
        internalVerifyRefreshTokenOk(service);
    }

}
