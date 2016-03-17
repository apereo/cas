package org.jasig.cas.support.oauth.web;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.BasicIdentifiableCredential;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.DefaultAuthenticationBuilder;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.services.OAuthWebApplicationService;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.code.DefaultOAuthCodeFactory;
import org.jasig.cas.support.oauth.ticket.code.OAuthCode;
import org.jasig.cas.support.oauth.validator.OAuthValidator;
import org.jasig.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This class tests the {@link OAuth20AccessTokenController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/oauth-context.xml"})
@DirtiesContext()
public class OAuth20AccessTokenControllerTests {

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

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private DefaultOAuthCodeFactory oAuthCodeFactory;

    @Autowired
    @Qualifier("accessTokenController")
    private OAuth20AccessTokenController oAuth20AccessTokenController;

    @Autowired
    @Qualifier("oAuthValidator")
    private OAuthValidator validator;

    @Autowired
    @Qualifier("oauthSecConfig")
    private Config oauthSecConfig;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("requiresAuthenticationAccessTokenInterceptor")
    private RequiresAuthenticationInterceptor requiresAuthenticationInterceptor;

    @Before
    public void setUp() {
        clearAllServices();
    }

    @Test
    public void verifyClientNoClientId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(401, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoRedirectUri() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
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
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
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
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoClientSecret() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(401, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoCode() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientNoCasService() throws Exception {

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        final OAuthCode code = addCode(principal, registeredService);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(401, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientRedirectUriDoesNotStartWithServiceId() throws Exception {

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, OTHER_REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientWrongSecret() throws Exception {

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase());
        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(401, mockResponse.getStatus());
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

        final Principal principal = org.jasig.cas.authentication.TestUtils.getPrincipal(ID, map);
        final Authentication authentication = getAuthentication(principal);
        final DefaultOAuthCodeFactory expiringOAuthCodeFactory = new DefaultOAuthCodeFactory();
        expiringOAuthCodeFactory.setExpirationPolicy(new AlwaysExpiresExpirationPolicy());
        final Service service = new OAuthWebApplicationService(registeredService);
        final OAuthCode code = expiringOAuthCodeFactory.create(service, authentication);
        oAuth20AccessTokenController.getTicketRegistry().addTicket(code);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase());
        servicesManager.save(getRegisteredService(REDIRECT_URI, CLIENT_SECRET));

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_GRANT, mockResponse.getContentAsString());
    }

    @Test
    public void verifyClientAuthByParameter() throws Exception {
        internalVerifyOK(false);
    }

    @Test
    public void verifyClientAuthByHeader() throws Exception {
        internalVerifyOK(true);
    }

    private void internalVerifyOK(final boolean basicAuth) throws Exception {

        final Principal principal = createPrincipal();
        final RegisteredService service = addRegisteredService();
        final OAuthCode code = addCode(principal, service);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase());
        if (basicAuth) {
            final String auth = CLIENT_ID + ':' + CLIENT_SECRET;
            final String value = Base64.encodeBase64String(auth.getBytes("UTF-8"));
            mockRequest.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);
        } else {
            mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
            mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        }
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertNull(oAuth20AccessTokenController.getTicketRegistry().getTicket((code.getId())));
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals(200, mockResponse.getStatus());
        final String body = mockResponse.getContentAsString();

        assertTrue(body.contains(OAuthConstants.ACCESS_TOKEN + '='));
        assertTrue(body.contains(OAuthConstants.EXPIRES + '='));

        final String accessTokenId = StringUtils.substringBetween(body, OAuthConstants.ACCESS_TOKEN + '=', "&");
        final AccessToken accessToken = oAuth20AccessTokenController.getTicketRegistry().getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        // delta = 2 seconds
        final int delta = 2;
        final int timeLeft = Integer.parseInt(StringUtils.substringAfter(body, '&' + OAuthConstants.EXPIRES + '='));
        assertTrue(timeLeft >= TIMEOUT - 10 - delta);
    }

    @Test
    public void verifyUserNoClientId() throws Exception {

        addRegisteredService();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserNoCasService() throws Exception {

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserBadAuthorizationCode() throws Exception {

        addRegisteredService();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserBadCredentials() throws Exception {

        addRegisteredService();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, "badPassword");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(401, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyUserAuth() throws Exception {

        addRegisteredService();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.GRANT_TYPE, OAuthGrantType.PASSWORD.name().toLowerCase());
        mockRequest.setParameter(USERNAME, GOOD_USERNAME);
        mockRequest.setParameter(PASSWORD, GOOD_PASSWORD);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals(200, mockResponse.getStatus());
        final String body = mockResponse.getContentAsString();

        assertTrue(body.contains(OAuthConstants.ACCESS_TOKEN + '='));
        assertTrue(body.contains(OAuthConstants.EXPIRES + '='));

        final String accessTokenId = StringUtils.substringBetween(body, OAuthConstants.ACCESS_TOKEN + '=', "&");
        final AccessToken accessToken = oAuth20AccessTokenController.getTicketRegistry().getTicket(accessTokenId, AccessToken.class);
        assertEquals(GOOD_USERNAME, accessToken.getAuthentication().getPrincipal().getId());

        // delta = 2 seconds
        final int delta = 2;
        final int timeLeft = Integer.parseInt(StringUtils.substringAfter(body, '&' + OAuthConstants.EXPIRES + '='));
        assertTrue(timeLeft >= TIMEOUT - 10 - delta);
    }

    private Principal createPrincipal() {
        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        return org.jasig.cas.authentication.TestUtils.getPrincipal(ID, map);
    }

    private RegisteredService addRegisteredService() {
        final RegisteredService registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        servicesManager.save(registeredService);
        return registeredService;
    }

    private OAuthCode addCode(final Principal principal, final RegisteredService registeredService) {
        final Authentication authentication = getAuthentication(principal);
        final Service service = new OAuthWebApplicationService(registeredService);
        final OAuthCode code = oAuthCodeFactory.create(service, authentication);
        oAuth20AccessTokenController.getTicketRegistry().addTicket(code);
        return code;
    }

    private RegisteredService getRegisteredService(final String serviceId, final String secret) {
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

        for (final RegisteredService r : col) {
            servicesManager.delete(r.getId());
        }

        ((ReloadableServicesManager) servicesManager).reload();
    }

    private Authentication getAuthentication(final Principal principal) {
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
