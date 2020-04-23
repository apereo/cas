package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.CollectionUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link OAuth20AccessTokenEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Tag("OAuth")
public class OAuth20AccessTokenEndpointControllerTests extends AbstractOAuth20Tests {

    /**
     * Check the registered services always contain empty allowed grant types.
     * These tests are run to ensure that
     * the change that adds proper support for supported grant types does not break existing CAS
     * setups that does not specify allowed grant types. Briefly, it checks that empty  supported grant types
     * is equivalent to supported grant types with all valid values.
     *
     * @return stream of services for tests
     */
    public static Stream<OAuthRegisteredService> getParameters() {
        return Stream.of(
            getRegisteredService(REDIRECT_URI, CLIENT_SECRET, CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE)),
            getRegisteredService(REDIRECT_URI, CLIENT_SECRET, new HashSet<>())
        );
    }

    @BeforeEach
    public void initialize() {
        clearAllServices();
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    @SneakyThrows
    public void verifyClientNoClientId(final OAuthRegisteredService registeredService) {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        this.servicesManager.save(registeredService);
        val code = addCode(principal, registeredService);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
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
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    @SneakyThrows
    public void verifyClientNoAuthorizationCode() {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        val principal = createPrincipal();
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    @SneakyThrows
    public void verifyClientBadGrantType() {
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
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyClientDisallowedGrantType() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CLIENT_CREDENTIALS.getType());
        val principal = createPrincipal();
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    @SneakyThrows
    public void verifyClientNoClientSecret() {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
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
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));

        addCode(principal, service);

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    @SneakyThrows
    public void verifyClientNoCasService(final OAuthRegisteredService registeredService) {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val code = addCode(principal, registeredService);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifyClientRedirectUriDoesNotStartWithServiceId(final OAuthRegisteredService registeredService) throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, OTHER_REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();

        this.servicesManager.save(registeredService);
        val code = addCode(principal, registeredService);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifyClientWrongSecret(final OAuthRegisteredService registeredService) throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        this.servicesManager.save(registeredService);
        val code = addCode(principal, registeredService);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    @SneakyThrows
    public void verifyClientEmptySecret() {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, StringUtils.EMPTY);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE), StringUtils.EMPTY);
        val code = addCode(principal, service);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
    }

    @Test
    @SneakyThrows
    public void verifyPKCECodeVerifier() {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.CODE_VERIFIER, CODE_CHALLENGE);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE), CLIENT_SECRET);
        val code = addCodeWithChallenge(principal, service, CODE_CHALLENGE, CODE_CHALLENGE_METHOD_PLAIN);

        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    @SneakyThrows
    public void verifyPKCEInvalidCodeVerifier(final OAuthRegisteredService registeredService) {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.CODE_VERIFIER, "invalidcode");
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        servicesManager.save(registeredService);
        val code = addCodeWithChallenge(principal, registeredService, CODE_CHALLENGE, CODE_CHALLENGE_METHOD_PLAIN);

        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    @SneakyThrows
    public void verifyPKCEEmptySecret() {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, StringUtils.EMPTY);
        mockRequest.setParameter(OAuth20Constants.CODE_VERIFIER, CODE_CHALLENGE);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE), StringUtils.EMPTY);
        val code = addCodeWithChallenge(principal, service, CODE_CHALLENGE, CODE_CHALLENGE_METHOD_PLAIN);

        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    @SneakyThrows
    public void verifyPKCEWrongSecret(final OAuthRegisteredService registeredService) {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.CODE_VERIFIER, CODE_CHALLENGE);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val principal = createPrincipal();
        this.servicesManager.save(registeredService);
        val code = addCodeWithChallenge(principal, registeredService, CODE_CHALLENGE, CODE_CHALLENGE_METHOD_PLAIN);

        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifyClientExpiredCode(final OAuthRegisteredService registeredService) throws Exception {
        servicesManager.save(registeredService);

        val map = new HashMap<String, List<Object>>();
        map.put(NAME, List.of(VALUE));
        val list = List.of(VALUE, VALUE);
        map.put(NAME2, (List) list);

        val principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        val authentication = getAuthentication(principal);
        val expiringOAuthCodeFactory = new OAuth20DefaultOAuthCodeFactory(alwaysExpiresExpirationPolicyBuilder(), servicesManager);
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getServiceId());
        val code = expiringOAuthCodeFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>(), null, null, CLIENT_ID, new HashMap<>());
        this.ticketRegistry.addTicket(code);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifyClientAuthByParameter(final OAuthRegisteredService registeredService) {
        servicesManager.save(registeredService);
        assertClientOK(registeredService, false);
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifyClientAuthWithJwtAccessToken(final OAuthRegisteredService registeredService) {
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);
        assertClientOK(registeredService, false);
    }

    @Test
    public void verifyDeviceFlowGeneratesCode() throws Exception {
        addRegisteredService();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
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
        assertNotNull(mvDev);
        val status = mvDev.getStatus();
        assertNotNull(status);
        assertTrue(status.is2xxSuccessful());

        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
        mockRequest.setParameter(OAuth20Constants.CODE, devCode);
        val approveResp = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, approveResp, null);
        val mvApproved = accessTokenController.handleRequest(mockRequest, approveResp);
        assertTrue(mvApproved.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        assertTrue(mvApproved.getModel().containsKey(OAuth20Constants.EXPIRES_IN));
        assertTrue(mvApproved.getModel().containsKey(OAuth20Constants.TOKEN_TYPE));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifyClientAuthByHeader(final OAuthRegisteredService registeredService) {
        servicesManager.save(registeredService);
        assertClientOK(registeredService, false);
    }

    @Test
    public void verifyClientAuthByParameterWithRefreshToken() {
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        assertClientOK(service, true);
    }

    @Test
    public void verifyClientAuthByHeaderWithRefreshToken() {
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        assertClientOK(service, true);
    }

    @Test
    public void verifyClientAuthJsonByParameterWithRefreshToken() {
        val service = addRegisteredService(
            CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        assertClientOK(service, true);
    }

    @Test
    public void verifyClientAuthJsonByHeaderWithRefreshToken() {
        val service = addRegisteredService(
            CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        service.setGenerateRefreshToken(true);
        assertClientOK(service, true);
    }

    @Test
    @SneakyThrows
    public void ensureOnlyRefreshTokenIsAcceptedForRefreshGrant() {
        addRegisteredService(true, CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD,
            OAuth20GrantTypes.REFRESH_TOKEN));
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        val mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);

        var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        var mv = accessTokenController.handleRequest(mockRequest, mockResponse);

        assertTrue(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        val refreshToken = mv.getModel().get(OAuth20Constants.REFRESH_TOKEN).toString();
        val accessToken = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, accessToken);

        mockResponse = new MockHttpServletResponse();
        accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());

        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken);
        mockResponse = new MockHttpServletResponse();
        mv = accessTokenController.handleRequest(mockRequest, mockResponse);
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
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
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
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyUserBadAuthorizationCode() throws Exception {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
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
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get(OAuth20Constants.ERROR));
    }

    @Test
    public void verifyUserAuth() {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        assertUserAuth(false, true);
    }

    @Test
    public void verifyUserAuthForServiceWithoutSecret() {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD), StringUtils.EMPTY);
        assertUserAuth(false, false);
    }

    @Test
    public void verifyUserAuthWithRefreshToken() {
        val registeredService = addRegisteredService(
            CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        registeredService.setGenerateRefreshToken(true);
        assertUserAuth(true, true);
    }

    @Test
    public void verifyJsonUserAuth() {
        addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        assertUserAuth(false, true);
    }

    @Test
    public void verifyJsonUserAuthWithRefreshToken() {
        val registeredService = addRegisteredService(
            CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        registeredService.setGenerateRefreshToken(true);
        assertUserAuth(true, true);
    }

    @SneakyThrows
    private void assertUserAuth(final boolean refreshToken, final boolean withClientSecret) {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        if (withClientSecret) {
            mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        }
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        mockRequest.addHeader(CasProtocolConstants.PARAMETER_SERVICE, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        if (refreshToken) {
            assertTrue(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
        }
        assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));

        val accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, OAuth20AccessToken.class);
        assertEquals(GOOD_USERNAME, accessToken.getAuthentication().getPrincipal().getId());

        val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    @Test
    @SneakyThrows
    public void verifyRefreshTokenExpiredToken() {
        val principal = createPrincipal();
        val registeredService = addRegisteredService(
            CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        val authentication = getAuthentication(principal);
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getServiceId());
        val expiringRefreshTokenFactory = new OAuth20DefaultRefreshTokenFactory(alwaysExpiresExpirationPolicyBuilder(), servicesManager);
        val refreshToken = expiringRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>(), CLIENT_ID, StringUtils.EMPTY, new HashMap<>());
        this.ticketRegistry.addTicket(refreshToken);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
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
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
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
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
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
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
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
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());

        val accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, OAuth20AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    @Test
    public void verifyRefreshTokenOK() {
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        assertRefreshTokenOk(service);
    }

    @Test
    public void verifyRefreshTokenOKWithRefreshToken() {
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        service.setGenerateRefreshToken(true);
        service.setRenewRefreshToken(true);
        assertRefreshTokenOk(service);
    }

    @Test
    public void verifyJsonRefreshTokenOK() {
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        assertRefreshTokenOk(service);
    }

    @Test
    public void verifyJsonRefreshTokenOKWithRefreshToken() {
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        service.setGenerateRefreshToken(true);
        service.setRenewRefreshToken(true);
        assertRefreshTokenOk(service);
    }

    @Test
    public void verifyAccessTokenRequestWithRefreshTokenCannotExceedScopes() throws Exception {
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        val principal = createPrincipal();
        val refreshToken = addRefreshTokenWithScope(principal, List.of("profile"), service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        mockRequest.setParameter(OAuth20Constants.SCOPE, "email");

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);

        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_BAD_REQUEST, mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_SCOPE, mv.getModel().get(OAuth20Constants.ERROR).toString());
    }

    @Test
    public void verifyAccessTokenRequestWithRefreshTokenWithoutRequestingScopes() throws Exception {
        val service = addRegisteredService(CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        val principal = createPrincipal();
        val refreshToken = addRefreshTokenWithScope(principal, List.of("profile"), service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());

        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);

        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        if (!service.isRenewRefreshToken()) {
            assertFalse(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
        } else {
            assertTrue(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
        }
        val newRefreshToken = service.isRenewRefreshToken()
            ? this.ticketRegistry.getTicket(mv.getModel().get(OAuth20Constants.REFRESH_TOKEN).toString(), OAuth20RefreshToken.class)
            : refreshToken;
        assertNotNull(newRefreshToken);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));
        val accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, OAuth20AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);
    }

    private OAuth20RefreshToken addRefreshTokenWithScope(final Principal principal, final List<String> scopes,
                                                         final OAuthRegisteredService registeredService) {
        val authentication = getAuthentication(principal);
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getServiceId());
        val refreshToken = oAuthRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"),
            scopes, CLIENT_ID, StringUtils.EMPTY, new HashMap<>());
        this.ticketRegistry.addTicket(refreshToken);
        return refreshToken;
    }
}
