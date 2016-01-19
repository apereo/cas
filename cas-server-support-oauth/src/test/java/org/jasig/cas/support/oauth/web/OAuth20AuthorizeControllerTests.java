package org.jasig.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.ticket.OAuthCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.util.CommonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This class tests the {@link OAuth20AuthorizeController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/oauth-context.xml", "classpath:/META-INF/spring/cas-servlet-oauth.xml"})
@DirtiesContext()
public final class OAuth20AuthorizeControllerTests {

    private static final String ID = "id";
    private static final String FIRST_NAME_ATTRIBUTE = "firstName";
    private static final String FIRST_NAME = "jerome";

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
    private OAuth20AuthorizeController oAuth20AuthorizeController;

    @Test
    public void verifyNoClientId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oAuth20AuthorizeController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoRedirectUri() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oAuth20AuthorizeController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoCasService() throws Exception {
        clearAllServices();
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oAuth20AuthorizeController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyRedirectUriDoesNotStartWithServiceId() throws Exception {
        clearAllServices();
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        oAuth20AuthorizeController.getServicesManager().save(getRegisteredService(OTHER_REDIRECT_URI, CLIENT_ID));

        final ModelAndView modelAndView = oAuth20AuthorizeController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyRedirectToClient() throws Exception {
        clearAllServices();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        oAuth20AuthorizeController.getServicesManager().save(service);

        final CasProfile profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        profile.addAttributes(attributes);

        final MockHttpSession session = new MockHttpSession();
        mockRequest.setSession(session);
        session.putValue(Pac4jConstants.USER_PROFILE, profile);

        final ModelAndView modelAndView = oAuth20AuthorizeController.handleRequest(mockRequest, mockResponse);
        final View view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) view;
        final String redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=OC-"));

        final String code = StringUtils.substringAfter(redirectUrl, "?code=");
        final OAuthCode oAuthCode = (OAuthCode) oAuth20AuthorizeController.getTicketRegistry().getTicket(code);
        assertNotNull(oAuthCode);
        final Principal principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        final Map<String, Object> principalAttributes = principal.getAttributes();
        assertEquals(1, principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
    }

    @Test
    public void verifyRedirectToClientWithState() throws Exception {
        clearAllServices();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setParameter(OAuthConstants.STATE, STATE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(true);
        oAuth20AuthorizeController.getServicesManager().save(service);

        final CasProfile profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        profile.addAttributes(attributes);

        final MockHttpSession session = new MockHttpSession();
        mockRequest.setSession(session);
        session.putValue(Pac4jConstants.USER_PROFILE, profile);

        final ModelAndView modelAndView = oAuth20AuthorizeController.handleRequest(mockRequest, mockResponse);
        final View view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) view;
        final String redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=OC-"));

        final String code = StringUtils.substringBefore(StringUtils.substringAfter(redirectUrl, "?code="), "&state=");
        final OAuthCode oAuthCode = (OAuthCode) oAuth20AuthorizeController.getTicketRegistry().getTicket(code);
        assertNotNull(oAuthCode);
        final Principal principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        final Map<String, Object> principalAttributes = principal.getAttributes();
        assertEquals(1, principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
    }

    @Test
    public void verifyRedirectToClientApproved() throws Exception {
        clearAllServices();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        mockRequest.setParameter(OAuthConstants.BYPASS_APPROVAL_PROMPT, "true");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        oAuth20AuthorizeController.getServicesManager().save(service);

        final CasProfile profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        profile.addAttributes(attributes);

        final MockHttpSession session = new MockHttpSession();
        mockRequest.setSession(session);
        session.putValue(Pac4jConstants.USER_PROFILE, profile);

        final ModelAndView modelAndView = oAuth20AuthorizeController.handleRequest(mockRequest, mockResponse);
        final View view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) view;
        final String redirectUrl = redirectView.getUrl();
        assertTrue(redirectUrl.startsWith(REDIRECT_URI + "?code=OC-"));

        final String code = StringUtils.substringAfter(redirectUrl, "?code=");
        final OAuthCode oAuthCode = (OAuthCode) oAuth20AuthorizeController.getTicketRegistry().getTicket(code);
        assertNotNull(oAuthCode);
        final Principal principal = oAuthCode.getAuthentication().getPrincipal();
        assertEquals(ID, principal.getId());
        final Map<String, Object> principalAttributes = principal.getAttributes();
        assertEquals(1, principalAttributes.size());
        assertEquals(FIRST_NAME, principalAttributes.get(FIRST_NAME_ATTRIBUTE));
    }

    @Test
    public void verifyRedirectToApproval() throws Exception {
        clearAllServices();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final OAuthRegisteredService service = getRegisteredService(REDIRECT_URI, SERVICE_NAME);
        service.setBypassApprovalPrompt(false);
        oAuth20AuthorizeController.getServicesManager().save(service);

        final CasProfile profile = new CasProfile();
        profile.setId(ID);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(FIRST_NAME_ATTRIBUTE, FIRST_NAME);
        profile.addAttributes(attributes);

        final MockHttpSession session = new MockHttpSession();
        mockRequest.setSession(session);
        session.putValue(Pac4jConstants.USER_PROFILE, profile);

        final ModelAndView modelAndView = oAuth20AuthorizeController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.CONFIRM_VIEW, modelAndView.getViewName());
        final Map<String, Object> model = modelAndView.getModel();
        assertEquals(CommonHelper.addParameter(AUTHORIZE_URL, OAuthConstants.BYPASS_APPROVAL_PROMPT, "true"), model.get("callbackUrl"));
        assertEquals(SERVICE_NAME, model.get("serviceName"));
    }

    private OAuthRegisteredService getRegisteredService(final String serviceId, final String name) {
        final OAuthRegisteredService registeredServiceImpl = new OAuthRegisteredService();
        registeredServiceImpl.setName(name);
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);
        return registeredServiceImpl;
    }

    private void clearAllServices() {
        final Collection<RegisteredService> col  = oAuth20AuthorizeController.getServicesManager().getAllServices();

        for (final RegisteredService r : col) {
            oAuth20AuthorizeController.getServicesManager().delete(r.getId());
        }

    }
}
