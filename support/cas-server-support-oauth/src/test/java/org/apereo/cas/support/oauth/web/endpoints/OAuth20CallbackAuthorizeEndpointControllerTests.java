package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20CallbackAuthorizeEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuth")
public class OAuth20CallbackAuthorizeEndpointControllerTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("callbackAuthorizeController")
    private OAuth20CallbackAuthorizeEndpointController callbackAuthorizeController;

    @BeforeEach
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val response = new MockHttpServletResponse();
        val view = callbackAuthorizeController.handleRequest(request, response);
        assertNotNull(view);
        assertEquals(REDIRECT_URI, ((RedirectView) view.getView()).getUrl());
    }

    @Test
    public void verifyOperationWithoutRedirectUri() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        val response = new MockHttpServletResponse();
        val view = callbackAuthorizeController.handleRequest(request, response);
        assertNotNull(view);
        assertEquals("http://localhost", ((RedirectView) view.getView()).getUrl());
    }
}
