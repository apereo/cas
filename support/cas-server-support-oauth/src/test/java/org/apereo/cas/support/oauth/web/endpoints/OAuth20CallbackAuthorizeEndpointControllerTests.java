package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20CallbackAuthorizeEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuthWeb")
class OAuth20CallbackAuthorizeEndpointControllerTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("callbackAuthorizeController")
    private OAuth20CallbackAuthorizeEndpointController callbackAuthorizeController;

    private OAuthRegisteredService registeredService;

    @BeforeEach
    void initialize() {

        registeredService = addRegisteredService();
    }

    @Test
    void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val response = new MockHttpServletResponse();
        val view = callbackAuthorizeController.handleRequest(request, response);
        assertNotNull(view);
        assertEquals(REDIRECT_URI, ((AbstractUrlBasedView) view.getView()).getUrl());
    }

    @Test
    void verifyOperationClientsWithSameRedirectUri() {
        addRegisteredService();
        val newRegisteredService = addRegisteredService();
        addRegisteredService();

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, newRegisteredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val response = new MockHttpServletResponse();
        val view = callbackAuthorizeController.handleRequest(request, response);
        assertNotNull(view);
        assertEquals(REDIRECT_URI, ((AbstractUrlBasedView) view.getView()).getUrl());
    }

    @Test
    void verifyOperationWithoutRedirectUri() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        val response = new MockHttpServletResponse();
        val view = callbackAuthorizeController.handleRequest(request, response);
        assertNotNull(view);
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, ((AbstractUrlBasedView) view.getView()).getUrl());
    }

    @Test
    void verifyOperationWithoutClientId() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val response = new MockHttpServletResponse();
        val view = callbackAuthorizeController.handleRequest(request, response);
        assertNotNull(view);
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, ((AbstractUrlBasedView) view.getView()).getUrl());
    }

    @Test
    void verifyOperationBadClientId() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "badClientId");
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val response = new MockHttpServletResponse();
        val view = callbackAuthorizeController.handleRequest(request, response);
        assertNotNull(view);
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, ((AbstractUrlBasedView) view.getView()).getUrl());
    }

    @Test
    void verifyOperationBadRedirectUri() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, "http://badredirecturi");
        val response = new MockHttpServletResponse();
        val view = callbackAuthorizeController.handleRequest(request, response);
        assertNotNull(view);
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, ((AbstractUrlBasedView) view.getView()).getUrl());
    }
}
