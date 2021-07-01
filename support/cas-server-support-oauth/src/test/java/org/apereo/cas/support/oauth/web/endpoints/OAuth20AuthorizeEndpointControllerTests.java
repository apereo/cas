package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.util.Pac4jConstants;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link OAuth20AuthorizeEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Tag("OAuth")
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
    public void verifyNoRedirectUri() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        val mockResponse = new MockHttpServletResponse();

        assertThrows(IllegalArgumentException.class, () -> oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse));
    }

    @Test
    public void verifyNoResponseType() throws Exception {
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
        assertEquals(modelView.getUrl(), REDIRECT_URI);

        assertTrue(modelAndView.getModel().containsKey(OAuth20Constants.ERROR));
        assertEquals(modelAndView.getModel().get(OAuth20Constants.ERROR).toString(), OAuth20Constants.INVALID_REQUEST);
    }

    @Test
    public void verifyNoCasService() throws Exception {
        clearAllServices();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        assertThrows(UnauthorizedServiceException.class, () -> oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse));
    }

    @Test
    public void verifyRedirectUriDoesNotStartWithServiceId() throws Exception {
        clearAllServices();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        this.servicesManager.save(getRegisteredService(OTHER_REDIRECT_URI, CLIENT_ID));
        assertThrows(IllegalArgumentException.class, () -> oAuth20AuthorizeEndpointController.handleRequestPost(mockRequest, mockResponse));
    }

    @Test
    public void verifyCodeNoProfile() throws Exception {
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
        this.servicesManager.save(service);

        val session = new MockHttpSession();
        mockRequest.setSession(session);

        assertThrows(IllegalArgumentException.class, () -> oAuth20AuthorizeEndpointController.handleRequestPost(mockRequest, mockResponse));
    }

    @Test
    public void verifyMissingTicketGrantingTicket() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
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
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    public void verifyCodeRedirectToClient() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        val casProperties = oAuth20AuthorizeEndpointController.getConfigurationContext().getCasProperties();
        casProperties.getSessionReplication().getCookie().setAutoConfigureCookiePath(true);
        casProperties.getAuthn().getOauth().setReplicateSessions(true);
        oAuth20AuthorizeEndpointController.getConfigurationContext()
            .getOauthDistributedSessionCookieGenerator().setCookiePath(StringUtils.EMPTY);

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        oAuth20AuthorizeEndpointController.getConfigurationContext().getServicesManager().save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertEquals(redirectUrl, REDIRECT_URI);

        assertEquals("/", oAuth20AuthorizeEndpointController.getConfigurationContext()
            .getOauthDistributedSessionCookieGenerator().getCookiePath());
        val code = modelAndView.getModelMap().get("code");
        val oAuthCode = (OAuth20Code) this.ticketRegistry.getTicket(String.valueOf(code));
        assertNotNull(oAuthCode);
        val principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyTokenRedirectToClient() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setContextPath(StringUtils.EMPTY);
        val mockResponse = new MockHttpServletResponse();

        val oauthContext = oAuth20AuthorizeEndpointController.getConfigurationContext();
        oauthContext.getCasProperties().getSessionReplication().getCookie().setAutoConfigureCookiePath(false);
        oauthContext.getOauthDistributedSessionCookieGenerator().setCookiePath(StringUtils.EMPTY);

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        this.servicesManager.save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        assertEquals(StringUtils.EMPTY, oAuth20AuthorizeEndpointController.getConfigurationContext()
            .getOauthDistributedSessionCookieGenerator().getCookiePath());
        val code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
        val accessToken = (OAuth20AccessToken) this.ticketRegistry.getTicket(code);
        assertNotNull(accessToken);
        val principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
        val expiresIn = StringUtils.substringAfter(redirectUrl, "&expires_in=");
        assertEquals(getDefaultAccessTokenExpiration(), Long.parseLong(expiresIn));
    }

    @Test
    public void verifyPerServiceExpiration() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
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
        this.servicesManager.save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);

        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));
        val at = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
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
        this.servicesManager.save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertEquals(redirectUrl, REDIRECT_URI);

        val code = modelAndView.getModelMap().getAttribute("code");

        val oAuthCode = (OAuth20Code) this.ticketRegistry.getTicket(String.valueOf(code));
        assertNotNull(oAuthCode);
        val principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyTokenRedirectToClientWithState() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
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
        this.servicesManager.save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
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
        assertEquals(state, OAuth20Constants.STATE);
        val principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyCodeRedirectToClientApproved() throws Exception {
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
        service.setBypassApprovalPrompt(false);
        this.servicesManager.save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        sessionStore.set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertEquals(redirectUrl, REDIRECT_URI);

        val code = modelAndView.getModelMap().get("code");
        val oAuthCode = (OAuth20Code) this.ticketRegistry.getTicket(String.valueOf(code));
        assertNotNull(oAuthCode);
        val principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyTokenRedirectToClientApproved() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        this.servicesManager.save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        sessionStore.set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        val code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
        val accessToken = (OAuth20AccessToken) this.ticketRegistry.getTicket(code);
        assertNotNull(accessToken);
        val principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyRedirectToApproval() throws Exception {
        clearAllServices();

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT
            + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        val mockResponse = new MockHttpServletResponse();

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        this.servicesManager.save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
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
        this.servicesManager.save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        sessionStore.set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        val at = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
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
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE).get(0));
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
        val col = this.servicesManager.getAllServices();
        col.forEach(r -> this.servicesManager.delete(r.getId()));
    }

    private static void assertEqualsWithDelta(final long expected, final long actual, final long delta) {
        assertTrue(Math.abs(expected - actual) <= delta);
    }
}
