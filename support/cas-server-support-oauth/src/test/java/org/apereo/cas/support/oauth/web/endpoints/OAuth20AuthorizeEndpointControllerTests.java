package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
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

    protected static OAuthRegisteredService getRegisteredService(final String serviceId, final String name) {
        val service = new OAuthRegisteredService();
        service.setName(name);
        service.setServiceId(serviceId);
        service.setClientId(CLIENT_ID);
        service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(List.of(FIRST_NAME_ATTRIBUTE)));
        return service;
    }

    @Test
    public void verifyNoClientId() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    public void verifyNoRedirectUri() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    public void verifyNoResponseType() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    public void verifyBadResponseType() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, "badvalue");
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    public void verifyNoCasService() throws Exception {
        clearAllServices();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
    }

    @Test
    public void verifyRedirectUriDoesNotStartWithServiceId() throws Exception {
        clearAllServices();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        this.servicesManager.save(getRegisteredService(OTHER_REDIRECT_URI, CLIENT_ID));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, modelAndView.getViewName());
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

        val casProperties = oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getCasProperties();
        casProperties.getSessionReplication().getCookie().setAutoConfigureCookiePath(true);
        casProperties.getAuthn().getOauth().setReplicateSessions(true);
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext()
            .getOauthDistributedSessionCookieGenerator().setCookiePath(StringUtils.EMPTY);

        val service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getServicesManager().save(service);

        val profile = new CasProfile();
        profile.setId(ID);
        val attributes = new HashMap<String, Object>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        val sessionStore = oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore();
        val context = new JEEContext(mockRequest, mockResponse, sessionStore);
        val ticket = new MockTicketGrantingTicket("casuser");
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getTicketRegistry().addTicket(ticket);
        sessionStore.set(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticket.getId());
        sessionStore.set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);

        assertEquals("/", oAuth20AuthorizeEndpointController.getOAuthConfigurationContext()
            .getOauthDistributedSessionCookieGenerator().getCookiePath());
        val code = new URIBuilder(redirectUrl).getQueryParams().get(0).getValue();
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

        val oauthContext = oAuth20AuthorizeEndpointController.getOAuthConfigurationContext();
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

        val session = new MockHttpSession();
        mockRequest.setSession(session);
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
            .set(new JEEContext(mockRequest, mockResponse, new JEESessionStore()),
                Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        assertEquals(StringUtils.EMPTY, oAuth20AuthorizeEndpointController.getOAuthConfigurationContext()
                .getOauthDistributedSessionCookieGenerator().getCookiePath());
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
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
            .set(new JEEContext(mockRequest, mockResponse, new JEESessionStore()),
                Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=" + OAuth20Code.PREFIX));

        val builder = new URIBuilder(redirectUrl);
        val code = builder.getQueryParams()
            .stream()
            .filter(a -> a.getName().equalsIgnoreCase("code"))
            .findFirst()
            .get()
            .getValue();

        val oAuthCode = (OAuth20Code) this.ticketRegistry.getTicket(code);
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
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
            .set(new JEEContext(mockRequest, mockResponse, new JEESessionStore()),
                Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

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
        val context = new JEEContext(mockRequest, mockResponse, new JEESessionStore());
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
            .set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
            .set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=OC-"));

        val builder = new URIBuilder(redirectUrl);
        val code = builder.getQueryParams()
            .stream()
            .filter(a -> a.getName().equalsIgnoreCase("code"))
            .findFirst()
            .get()
            .getValue();
        val oAuthCode = (OAuth20Code) this.ticketRegistry.getTicket(code);
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
        val context = new JEEContext(mockRequest, mockResponse, new JEESessionStore());
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
            .set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
            .set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

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
        val context = new JEEContext(mockRequest, mockResponse, new JEESessionStore());
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
            .set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

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
        val context = new JEEContext(mockRequest, mockResponse, new JEESessionStore());
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
            .set(context, Pac4jConstants.USER_PROFILES, CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        oAuth20AuthorizeEndpointController.getOAuthConfigurationContext().getSessionStore()
            .set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

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


    @Override
    protected void clearAllServices() {
        val col = this.servicesManager.getAllServices();
        col.forEach(r -> this.servicesManager.delete(r.getId()));
    }
}
