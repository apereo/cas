package org.apereo.cas.support.oauth.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.OAuthCode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This class tests the {@link OAuth20AuthorizeEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Slf4j
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("authorizeController")
    private OAuth20AuthorizeEndpointController oAuth20AuthorizeEndpointController;

    @Test
    public void verifyNoClientId() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        final var mockResponse = new MockHttpServletResponse();

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoRedirectUri() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        final var mockResponse = new MockHttpServletResponse();

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoResponseType() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        final var mockResponse = new MockHttpServletResponse();

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyBadResponseType() throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, "badvalue");
        final var mockResponse = new MockHttpServletResponse();

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoCasService() throws Exception {
        clearAllServices();
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        final var mockResponse = new MockHttpServletResponse();

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyRedirectUriDoesNotStartWithServiceId() throws Exception {
        clearAllServices();
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        final var mockResponse = new MockHttpServletResponse();

        this.servicesManager.save(getRegisteredService(OTHER_REDIRECT_URI, CLIENT_ID));

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyCodeNoProfile() throws Exception {
        clearAllServices();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final var mockResponse = new MockHttpServletResponse();

        final var service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        this.servicesManager.save(service);

        final var session = new MockHttpSession();
        mockRequest.setSession(session);

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyCodeRedirectToClient() throws Exception {
        clearAllServices();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final var mockResponse = new MockHttpServletResponse();

        final var service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        this.servicesManager.save(service);

        final var profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        final var session = new MockHttpSession();
        session.putValue(Pac4jConstants.USER_PROFILES, profile);
        mockRequest.setSession(session);

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        final var view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final var redirectView = (RedirectView) view;
        final var redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=OC-"));

        final var code = StringUtils.substringAfter(redirectUrl, "?code=");
        final var oAuthCode = (OAuthCode) this.ticketRegistry.getTicket(code);
        assertNotNull(oAuthCode);
        final var principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        final var principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
    }

    @Test
    public void verifyTokenRedirectToClient() throws Exception {
        clearAllServices();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final var mockResponse = new MockHttpServletResponse();

        final var service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        this.servicesManager.save(service);

        final var profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        final var session = new MockHttpSession();
        mockRequest.setSession(session);
        session.putValue(Pac4jConstants.USER_PROFILES, profile);

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        final var view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final var redirectView = (RedirectView) view;
        final var redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        final var code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
        final var accessToken = (AccessToken) this.ticketRegistry.getTicket(code);
        assertNotNull(accessToken);
        final var principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        final var principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
    }

    @Test
    public void verifyCodeRedirectToClientWithState() throws Exception {
        clearAllServices();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setParameter(OAuth20Constants.STATE, STATE);
        final var mockResponse = new MockHttpServletResponse();

        final var service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        this.servicesManager.save(service);

        final var profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        final var session = new MockHttpSession();
        mockRequest.setSession(session);
        session.putValue(Pac4jConstants.USER_PROFILES, profile);

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        final var view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final var redirectView = (RedirectView) view;
        final var redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=OC-"));

        final var code = StringUtils.substringBefore(StringUtils.substringAfter(redirectUrl, "?code="), "&state=");
        final var oAuthCode = (OAuthCode) this.ticketRegistry.getTicket(code);
        assertNotNull(oAuthCode);
        final var principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        final var principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
    }

    @Test
    public void verifyTokenRedirectToClientWithState() throws Exception {
        clearAllServices();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setParameter(OAuth20Constants.STATE, STATE);
        final var mockResponse = new MockHttpServletResponse();

        final var service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        this.servicesManager.save(service);

        final var profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        final var session = new MockHttpSession();
        mockRequest.setSession(session);
        session.putValue(Pac4jConstants.USER_PROFILES, profile);

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        final var view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final var redirectView = (RedirectView) view;
        final var redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));
        assertTrue(redirectUrl.contains('&' + OAuth20Constants.STATE + '=' + STATE));

        final var code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
        final var accessToken = (AccessToken) this.ticketRegistry.getTicket(code);
        assertNotNull(accessToken);
        final var principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        final var principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
    }

    @Test
    public void verifyCodeRedirectToClientApproved() throws Exception {
        clearAllServices();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final var mockResponse = new MockHttpServletResponse();

        final var service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        this.servicesManager.save(service);

        final var profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        final var session = new MockHttpSession();
        mockRequest.setSession(session);
        session.putValue(OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");
        session.putValue(Pac4jConstants.USER_PROFILES, profile);

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        final var view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final var redirectView = (RedirectView) view;
        final var redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=OC-"));

        final var code = StringUtils.substringAfter(redirectUrl, "?code=");
        final var oAuthCode = (OAuthCode) this.ticketRegistry.getTicket(code);
        assertNotNull(oAuthCode);
        final var principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        final var principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
    }

    @Test
    public void verifyTokenRedirectToClientApproved() throws Exception {
        clearAllServices();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final var mockResponse = new MockHttpServletResponse();

        final var service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        this.servicesManager.save(service);

        final var profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        final var session = new MockHttpSession();
        mockRequest.setSession(session);
        session.putValue(Pac4jConstants.USER_PROFILES, profile);
        session.putValue(OAuth20Constants.BYPASS_APPROVAL_PROMPT, "true");

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        final var view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final var redirectView = (RedirectView) view;
        final var redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "#access_token="));

        final var code = StringUtils.substringBetween(redirectUrl, "#access_token=", "&token_type=bearer");
        final var accessToken = (AccessToken) this.ticketRegistry.getTicket(code);
        assertNotNull(accessToken);
        final var principal = accessToken.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        final var principalAttributes = principal.getAttributes();
        assertEquals(attributes.size(), principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
    }

    @Test
    public void verifyRedirectToApproval() throws Exception {
        clearAllServices();

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT
            + OAuth20Constants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase());
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final var mockResponse = new MockHttpServletResponse();

        final var service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        this.servicesManager.save(service);

        final var profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        attributes.put(LAST_NAME_ATTRIBUTE, LAST_NAME);
        profile.addAttributes(attributes);

        final var session = new MockHttpSession();
        mockRequest.setSession(session);
        session.putValue(Pac4jConstants.USER_PROFILES, profile);

        final var modelAndView = oAuth20AuthorizeEndpointController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuth20Constants.CONFIRM_VIEW, modelAndView.getViewName());
        final var model = modelAndView.getModel();
        assertEquals(AUTHORIZE_URL, model.get("callbackUrl"));
        assertEquals(SERVICE_NAME, model.get("serviceName"));
    }

    protected static OAuthRegisteredService getRegisteredService(final String serviceId, final String name) {
        final var registeredServiceImpl = new OAuthRegisteredService();
        registeredServiceImpl.setName(name);
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);
        registeredServiceImpl.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(Arrays.asList(FIRST_NAME_ATTRIBUTE)));
        return registeredServiceImpl;
    }

    @Override
    protected void clearAllServices() {
        final var col = this.servicesManager.getAllServices();
        col.forEach(r -> this.servicesManager.delete(r.getId()));
    }
}
