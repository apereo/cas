package org.jasig.cas.support.oauth.web;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.net.URL;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * This class tests the {@link OAuth20AuthorizeController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/oauth-context.xml")
@DirtiesContext()
public final class OAuth20AuthorizeControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String CLIENT_ID = "1";

    private static final String REDIRECT_URI = "http://someurl";

    private static final String OTHER_REDIRECT_URI = "http://someotherurl";

    private static final String CAS_SERVER = "casserver";

    private static final String CAS_SCHEME = "https";

    private static final int CAS_PORT = 443;

    private static final String CAS_URL = CAS_SCHEME + "://" + CAS_SERVER + ':' + CAS_PORT;

    private static final String SERVICE_NAME = "serviceName";

    private static final String STATE = "state";

    @Autowired
    private Controller oauth20WrapperController;

    @Test
    public void verifyNoClientId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyNoRedirectUri() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
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

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
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

        ((OAuth20WrapperController) oauth20WrapperController)
            .getServicesManager().save(getRegisteredService(OTHER_REDIRECT_URI, CLIENT_ID));



        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }

    @Test
    public void verifyOK() throws Exception {
        clearAllServices();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        ((OAuth20WrapperController) oauth20WrapperController)
            .getServicesManager().save(getRegisteredService(REDIRECT_URI, SERVICE_NAME));

        final Controller c = ((OAuth20WrapperController) oauth20WrapperController).getAuthorizeController();
        ((OAuth20AuthorizeController) c).setLoginUrl(CAS_URL);

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        final HttpSession session = mockRequest.getSession();
        assertEquals(REDIRECT_URI, session.getAttribute(OAuthConstants.OAUTH20_CALLBACKURL));
        assertEquals(SERVICE_NAME, session.getAttribute(OAuthConstants.OAUTH20_SERVICE_NAME));
        final View view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) view;
        
        final MockHttpServletRequest reqSvc = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        reqSvc.setServerName(CAS_SERVER);
        reqSvc.setServerPort(CAS_PORT);
        reqSvc.setScheme(CAS_SCHEME);
        final URL url = new URL(OAuthUtils.addParameter(CAS_URL, "service", reqSvc.getRequestURL().toString()));
        final URL url2 = new URL(redirectView.getUrl());

        assertEquals(url, url2);
    }

    @Test
    public void verifyOKWithState() throws Exception {
        clearAllServices();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.STATE, STATE);
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        ((OAuth20WrapperController) oauth20WrapperController)
            .getServicesManager().save(getRegisteredService(REDIRECT_URI, SERVICE_NAME));


        final Controller c = ((OAuth20WrapperController) oauth20WrapperController).getAuthorizeController();
        ((OAuth20AuthorizeController) c).setLoginUrl(CAS_URL);


        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        final HttpSession session = mockRequest.getSession();
        assertEquals(REDIRECT_URI, session.getAttribute(OAuthConstants.OAUTH20_CALLBACKURL));
        assertEquals(SERVICE_NAME, session.getAttribute(OAuthConstants.OAUTH20_SERVICE_NAME));
        assertEquals(STATE, session.getAttribute(OAuthConstants.OAUTH20_STATE));
        final View view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) view;
        
        final MockHttpServletRequest reqSvc = new MockHttpServletRequest("GET", CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        reqSvc.setServerName(CAS_SERVER);
        reqSvc.setServerPort(CAS_PORT);
        reqSvc.setScheme(CAS_SCHEME);
        final URL url = new URL(OAuthUtils.addParameter(CAS_URL, "service", reqSvc.getRequestURL().toString()));
        final URL url2 = new URL(redirectView.getUrl());

        assertEquals(url, url2);
    }

    private RegisteredService getRegisteredService(final String serviceId, final String name) {
        final OAuthRegisteredService registeredServiceImpl = new OAuthRegisteredService();
        registeredServiceImpl.setName(name);
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);
        return registeredServiceImpl;
    }

    private void clearAllServices() {
        final Collection<RegisteredService> col  =
            ((OAuth20WrapperController) oauth20WrapperController).getServicesManager().getAllServices();

        for (final RegisteredService r : col) {
            ((OAuth20WrapperController) oauth20WrapperController).getServicesManager().delete(r.getId());
        }

    }
}
