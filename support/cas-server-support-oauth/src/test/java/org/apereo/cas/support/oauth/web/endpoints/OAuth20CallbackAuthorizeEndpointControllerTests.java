package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20CallbackAuthorizeEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuthWeb")
class OAuth20CallbackAuthorizeEndpointControllerTests extends AbstractOAuth20Tests {
    private OAuthRegisteredService registeredService;

    @BeforeEach
    void initialize() {
        registeredService = addRegisteredService();
    }

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(REDIRECT_URI, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyOperationClientsWithSameRedirectUri() throws Throwable {
        addRegisteredService();
        val newRegisteredService = addRegisteredService();
        addRegisteredService();

        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.CLIENT_ID, newRegisteredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(REDIRECT_URI, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyOperationWithoutRedirectUri() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyOperationWithoutClientId() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyOperationBadClientId() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.CLIENT_ID, "badClientId");
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyOperationBadRedirectUri() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, "http://badredirecturi");
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, result.getResponse().getRedirectedUrl());
    }
}
