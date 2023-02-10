package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.ServiceTicketSessionTrackingPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class tests the {@link OAuth20AuthorizeEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Tag("OAuthWeb")
public class OAuth20AuthorizeEndpointControllerTests extends AbstractOAuth20Tests {
    private static final String AUTHORIZE_URL = CAS_SCHEME + "://" + CAS_SERVER + CONTEXT + "authorize";

    private static final String SERVICE_NAME = "serviceName";

    private static final String STATE = "state";

    @Autowired
    @Qualifier("authorizeController")
    private OAuth20AuthorizeEndpointController oAuth20AuthorizeEndpointController;

    @Test
    public void verifyNoClientId() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();
        assertThrows(UnauthorizedServiceException.class, () -> oAuth20AuthorizeEndpointController.handleRequestPost(mockRequest, mockResponse));
    }

    @Test
    public void verifyNoRedirectUri() {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        val mockResponse = new MockHttpServletResponse();

        assertThrows(IllegalArgumentException.class, () -> oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse));
    }

    @Test
    public void verifyNoResponseType() {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        assertThrows(IllegalArgumentException.class, () -> oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse));
    }

    @Test
    public void verifyBadResponseType() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, "badvalue");
        mockRequest.setAttribute(OAuth20Constants.ERROR, OAuth20Constants.INVALID_REQUEST);
        mockRequest.setAttribute(OAuth20Constants.ERROR_WITH_CALLBACK, true);
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertTrue(modelAndView.getView() instanceof RedirectView);
        val modelView = (RedirectView) modelAndView.getView();
        assertEquals(REDIRECT_URI, modelView.getUrl());

        assertTrue(modelAndView.getModel().containsKey(OAuth20Constants.ERROR));
        assertEquals(OAuth20Constants.INVALID_REQUEST, modelAndView.getModel().get(OAuth20Constants.ERROR).toString());
    }

    @Test
    public void verifyNoCasService() {
        clearAllServices();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        assertThrows(UnauthorizedServiceException.class, () -> oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse));
    }

    @Test
    public void verifyCasClientCanValidate() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        val mockResponse = new MockHttpServletResponse();
        val callContext = new CallContext(new JEEContext(mockRequest, mockResponse),
            oauthDistributedSessionStore, ProfileManagerFactory.DEFAULT);
        val redirect = oauthCasClient.getRedirectionAction(callContext);
        assertTrue(redirect.isPresent());

        val callbackUrl = ((WithLocationAction) redirect.get()).getLocation();
        val callback = RegisteredServiceTestUtils.getRegisteredService(callbackUrl);
        servicesManager.save(callback);

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        servicesManager.save(service);

        val tgt = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        ticketRegistry.addTicket(tgt);

        val trackingPolicy = mock(ServiceTicketSessionTrackingPolicy.class);
        val ticketService = RegisteredServiceTestUtils.getService(REDIRECT_URI);
        ticketService.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(service.getClientId()));
        val st1 = tgt.grantServiceTicket(ticketService, trackingPolicy);

        ticketRegistry.addTicket(st1);
        ticketRegistry.updateTicket(tgt);
        mockRequest.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.addParameter(CasProtocolConstants.PARAMETER_TICKET, st1.getId());
        mockRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, callbackUrl);

        val clientCallContext = new CallContext(new JEEContext(mockRequest, mockResponse),
            oauthDistributedSessionStore, ProfileManagerFactory.DEFAULT);
        val result = oauthCasClient.getCredentials(clientCallContext)
            .map(credentials -> oauthCasClient.validateCredentials(clientCallContext, credentials))
            .orElseThrow();
        assertTrue(result.isPresent());
        assertNotNull(result.get().getUserProfile());
    }

    @Test
    public void verifyRedirectUriDoesNotStartWithServiceId() {
        clearAllServices();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        servicesManager.save(getRegisteredService(OTHER_REDIRECT_URI, CLIENT_ID));
        assertThrows(IllegalArgumentException.class, () -> oAuth20AuthorizeEndpointController.handleRequestPost(mockRequest, mockResponse));
    }

    @Test
    public void verifyCodeNoProfile() {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val session = new MockHttpSession();
        mockRequest.setSession(session);

        assertThrows(IllegalArgumentException.class, () -> oAuth20AuthorizeEndpointController.handleRequestPost(mockRequest, mockResponse));
    }

    @Test
    public void verifyMissingTicketGrantingTicket() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        oAuth20AuthorizeEndpointController.getConfigurationContext().getServicesManager().save(service);

        val profile = new CasProfile();
        profile.setId(ID);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    public void verifyServiceAccessDenied() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(Map.of("required", Set.of("value1"))));
        oAuth20AuthorizeEndpointController.getConfigurationContext().getServicesManager().save(service);

        val profile = new CasProfile();
        profile.setId(ID);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    public void verifyCodeRedirectToClient() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        val properties = oAuth20AuthorizeEndpointController.getConfigurationContext().getCasProperties();
        properties.getAuthn().getOauth().getSessionReplication().getCookie().setAutoConfigureCookiePath(true);
        properties.getAuthn().getOauth().getSessionReplication().setReplicateSessions(true);
        oAuth20AuthorizeEndpointController.getConfigurationContext()
            .getOauthDistributedSessionCookieGenerator().setCookiePath(StringUtils.EMPTY);

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        oAuth20AuthorizeEndpointController.getConfigurationContext().getServicesManager().save(service);

        val profile = buildCasProfile();

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);

        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertEquals(REDIRECT_URI, redirectUrl);

        assertEquals("/", oAuth20AuthorizeEndpointController.getConfigurationContext()
            .getOauthDistributedSessionCookieGenerator().getCookiePath());
        val code = modelAndView.getModelMap().get("code");
        val oAuthCode = (OAuth20Code) this.ticketRegistry.getTicket(String.valueOf(code));
        assertNotNull(oAuthCode);
        val principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(profile.getAttributes().size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyTokenRedirectToClient() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        val oauthContext = oAuth20AuthorizeEndpointController.getConfigurationContext();
        oauthContext.getCasProperties().getAuthn().getOauth().getSessionReplication().getCookie().setAutoConfigureCookiePath(false);
        oauthContext.getOauthDistributedSessionCookieGenerator().setCookiePath(StringUtils.EMPTY);

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val profile = buildCasProfile();

        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);

        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        assertEquals("/", oAuth20AuthorizeEndpointController.getConfigurationContext()
            .getOauthDistributedSessionCookieGenerator().getCookiePath());
        val code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=Bearer");
        val accessToken = ticketRegistry.getTicket(code, OAuth20AccessToken.class);
        assertNotNull(accessToken);
        val principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(profile.getAttributes().size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
        val expiresIn = StringUtils.substringAfter(redirectUrl, "&expires_in=");
        assertEquals(getDefaultAccessTokenExpiration(), Long.parseLong(expiresIn));
    }

    @Test
    public void verifyPerServiceExpiration() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        val expirationPolicy = new DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy();
        expirationPolicy.setMaxTimeToLive("5005");
        expirationPolicy.setTimeToKill("1001");
        service.setAccessTokenExpirationPolicy(expirationPolicy);
        service.setJwtAccessToken(true);
        servicesManager.save(service);

        val profile = buildCasProfile();

        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);

        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);

        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));
        val at = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=Bearer");
        val decoded = this.oauthAccessTokenJwtCipherExecutor.decode(at).toString();
        assertNotNull(decoded);
        val jwt = JwtClaims.parse(decoded);
        assertNotNull(jwt);
        assertNotNull(jwt.getExpirationTime());
        assertNotNull(jwt.getIssuedAt());
        assertEqualsWithDelta(Long.parseLong(expirationPolicy.getMaxTimeToLive()),
            jwt.getExpirationTime().getValue() - jwt.getIssuedAt().getValue(),
            DELTA
        );

        val expiresIn = StringUtils.substringAfter(redirectUrl, "&expires_in=");
        assertEquals(expirationPolicy.getMaxTimeToLive(), expiresIn);
    }

    @Test
    public void verifyCodeRedirectToClientWithState() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setParameter(OAuth20Constants.STATE, STATE);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val profile = buildCasProfile();

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);

        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertEquals(REDIRECT_URI, redirectUrl);

        val code = modelAndView.getModelMap().getAttribute("code");

        val oAuthCode = (OAuth20Code) this.ticketRegistry.getTicket(String.valueOf(code));
        assertNotNull(oAuthCode);
        val principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(profile.getAttributes().size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyTokenRedirectToClientWithState() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setParameter(OAuth20Constants.STATE, STATE);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        val profile = buildCasProfile();

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);

        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        var redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        redirectUrl += "&";
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        val code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&");
        val state = StringUtils.substringBetween(redirectUrl, "state=", "&");
        val accessToken = (OAuth20AccessToken) this.ticketRegistry.getTicket(code);
        assertNotNull(accessToken);
        assertEquals(OAuth20Constants.STATE, state);
        val principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(profile.getAttributes().size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyCodeRedirectToClientApproved() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        servicesManager.save(service);

        val profile = buildCasProfile();

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        sessionStore.set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertEquals(REDIRECT_URI, redirectUrl);

        val code = modelAndView.getModelMap().get("code");
        val oAuthCode = (OAuth20Code) this.ticketRegistry.getTicket(String.valueOf(code));
        assertNotNull(oAuthCode);
        val principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(profile.getAttributes().size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyTokenRedirectToClientApproved() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        servicesManager.save(service);

        val profile = buildCasProfile();

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);

        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        sessionStore.set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        val code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=Bearer");
        val accessToken = (OAuth20AccessToken) this.ticketRegistry.getTicket(code);
        assertNotNull(accessToken);
        val principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(profile.getAttributes().size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyRedirectToApproval() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        servicesManager.save(service);

        val profile = buildCasProfile();

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);

        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.CONFIRM_VIEW, modelAndView.getViewName());
        val model = modelAndView.getModel();
        assertEquals(AUTHORIZE_URL + '?' + OAuth20Constants.BYPASS_APPROVAL_PROMPT + "=true", model.get("callbackUrl"));
        assertEquals(SERVICE_NAME, model.get("serviceName"));
    }

    @Test
    public void verifyTokenRedirectToClientApprovedWithJwtToken() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        service.setJwtAccessToken(true);
        servicesManager.save(service);

        val profile = buildCasProfile();

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        sessionStore.set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        val at = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=Bearer");
        assertNull(this.ticketRegistry.getTicket(at));

        val decoded = this.oauthAccessTokenJwtCipherExecutor.decode(at).toString();
        assertNotNull(decoded);

        val jwt = JwtClaims.parse(decoded);
        assertNotNull(jwt);

        val accessToken = this.ticketRegistry.getTicket(jwt.getJwtId(), OAuth20AccessToken.class);
        assertNotNull(accessToken);
        val principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(profile.getAttributes().size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @SneakyThrows
    protected CasProfile buildCasProfile() {
        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        attributes.put(Authentication.class.getName(), CoreAuthenticationTestUtils.getAuthentication());

        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        attributes.put(TicketGrantingTicket.class.getName(), ticket.getId());


        profile.addAttributes(attributes);
        return profile;
    }

    protected static OAuthRegisteredService getRegisteredService(final String serviceId, final String name) {
        val service = new OAuthRegisteredService();
        service.setName(name);
        service.setServiceId(serviceId);
        service.setClientId(CLIENT_ID);
        service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(List.of(FIRST_NAME_ATTRIBUTE)));
        return service;
    }

    @Override
    protected void clearAllServices() {
        val col = servicesManager.getAllServices();
        col.forEach(r -> servicesManager.delete(r.getId()));
    }

    private static void assertEqualsWithDelta(final long expected, final long actual, final long delta) {
        assertTrue(Math.abs(expected - actual) <= delta);
    }
}
