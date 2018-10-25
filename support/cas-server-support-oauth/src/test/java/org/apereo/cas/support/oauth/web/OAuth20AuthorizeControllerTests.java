package org.apereo.cas.support.oauth.web;

import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.OAuthCode;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link OAuth20AuthorizeEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
public class OAuth20AuthorizeControllerTests extends AbstractOAuth20Tests {

    private static final String ID = "id";
    private static final String FIRST_NAME_ATTRIBUTE = "firstName";
    private static final String FIRST_NAME = "jerome";
    private static final String LAST_NAME_ATTRIBUTE = "lastName";
    private static final String LAST_NAME = "LELEU";
    private static final String CONTEXT = "/oauth2.0/";
    private static final String CLIENT_ID = "1";
    private static final String REDIRECT_URI = "http://someurl";
    private static final String OTHER_REDIRECT_URI = "http://someotherurl";
    private static final String CAS_SERVER = "casserver";
    private static final String CAS_SCHEME = "https";
    private static final int CAS_PORT = 443;
    private static final String AUTHORIZE_URL = CAS_SCHEME + "://" + CAS_SERVER + CONTEXT + "authorize";
    private static final String SERVICE_NAME = "serviceName";
    private static final String STATE = "state";

    @Autowired
    @Qualifier("authorizeController")
    private OAuth20AuthorizeEndpointController oAuth20AuthorizeEndpointController;

    protected static OAuthRegisteredService getRegisteredService(final String serviceId, final String name) {
        val registeredServiceImpl = new OAuthRegisteredService();
        registeredServiceImpl.setName(name);
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);
        registeredServiceImpl.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(Collections.singletonList(FIRST_NAME_ATTRIBUTE)));
        return registeredServiceImpl;
    }

    @Test
    public void verifyNoClientId() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoRedirectUri() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoResponseType() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyBadResponseType() throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, "badvalue");
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoCasService() throws Exception {
        clearAllServices();
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val mockResponse = new MockHttpServletResponse();

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
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
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
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
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
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
        session.putValue(Pac4jConstants.USER_PROFILES, profile);
        mockRequest.setSession(session);

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=OC-"));

        val code = StringUtils.substringAfter(redirectUrl, "?code=");
        val oAuthCode = (OAuthCode) this.ticketRegistry.getTicket(code);
        assertNotNull(oAuthCode);
        val principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
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
        session.putValue(Pac4jConstants.USER_PROFILES, profile);

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        val code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
        val accessToken = (AccessToken) this.ticketRegistry.getTicket(code);
        assertNotNull(accessToken);
        val principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
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
        session.putValue(Pac4jConstants.USER_PROFILES, profile);

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=OC-"));

        val code = StringUtils.substringBefore(StringUtils.substringAfter(redirectUrl, "?code="), "&state=");
        val oAuthCode = (OAuthCode) this.ticketRegistry.getTicket(code);
        assertNotNull(oAuthCode);
        val principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
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
        session.putValue(Pac4jConstants.USER_PROFILES, profile);

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));
        assertTrue(redirectUrl.contains('&' + OAuth20Constants.STATE + '=' + STATE));

        val code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
        val accessToken = (AccessToken) this.ticketRegistry.getTicket(code);
        assertNotNull(accessToken);
        val principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
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
        session.putValue(OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");
        session.putValue(Pac4jConstants.USER_PROFILES, profile);

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=OC-"));

        val code = StringUtils.substringAfter(redirectUrl, "?code=");
        val oAuthCode = (OAuthCode) this.ticketRegistry.getTicket(code);
        assertNotNull(oAuthCode);
        val principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
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
        session.putValue(Pac4jConstants.USER_PROFILES, profile);
        session.putValue(OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        val view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        val redirectView = (RedirectView) view;
        val redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        val code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
        val accessToken = (AccessToken) this.ticketRegistry.getTicket(code);
        assertNotNull(accessToken);
        val principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        val principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
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
        session.putValue(Pac4jConstants.USER_PROFILES, profile);

        val modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.CONFIRM_VIEW, modelAndView.getViewName());
        val model = modelAndView.getModel();
        assertEquals(AUTHORIZE_URL + '?' + OAuth20Constants.BYPASS_APPROVAL_PROMPT + "=true", model.get("callbackUrl"));
        assertEquals(SERVICE_NAME, model.get("serviceName"));
    }

    @Override
    protected void clearAllServices() {
        val col = this.servicesManager.getAllServices();
        col.forEach(r -> this.servicesManager.delete(r.getId()));
    }
}
