package org.jasig.cas.support.oauth.web;

import static org.junit.Assert.*;

import java.util.Map;

import org.jasig.cas.support.oauth.OAuthConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * This class tests the {@link OAuth20CallbackAuthorizeController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/oauth-context.xml")
@DirtiesContext()
public final class OAuth20CallbackAuthorizeControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String SERVICE_TICKET = "ST-1";

    private static final String REDIRECT_URI = "http://someurl";

    private static final String SERVICE_NAME = "serviceName";

    private static final String STATE = "state";

    @Autowired
    private Controller oauth20WrapperController;

    @Test
    public void verifyOK() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET",
                CONTEXT
                + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        mockRequest.addParameter(OAuthConstants.TICKET, SERVICE_TICKET);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_CALLBACKURL, REDIRECT_URI);
        mockSession.putValue(OAuthConstants.OAUTH20_SERVICE_NAME, SERVICE_NAME);
        mockRequest.setSession(mockSession);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.CONFIRM_VIEW, modelAndView.getViewName());
        final Map<String, Object> map = modelAndView.getModel();
        assertEquals(SERVICE_NAME, map.get("serviceName"));
        assertEquals(REDIRECT_URI + '?' + OAuthConstants.CODE + '=' + SERVICE_TICKET, map.get("callbackUrl"));
    }

    @Test
    public void verifyOKWithState() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET",
                CONTEXT
                + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        mockRequest.addParameter(OAuthConstants.TICKET, SERVICE_TICKET);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_CALLBACKURL, REDIRECT_URI);
        mockSession.putValue(OAuthConstants.OAUTH20_SERVICE_NAME, SERVICE_NAME);
        mockSession.putValue(OAuthConstants.OAUTH20_STATE, STATE);
        mockRequest.setSession(mockSession);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.CONFIRM_VIEW, modelAndView.getViewName());
        final Map<String, Object> map = modelAndView.getModel();
        assertEquals(SERVICE_NAME, map.get("serviceName"));
        assertEquals(REDIRECT_URI + '?' + OAuthConstants.CODE + '=' + SERVICE_TICKET + '&' + OAuthConstants.STATE + '='
                + STATE, map.get("callbackUrl"));
    }
}
