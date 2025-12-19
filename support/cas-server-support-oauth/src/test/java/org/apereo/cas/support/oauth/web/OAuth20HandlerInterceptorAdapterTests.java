package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;


/**
 * This is {@link OAuth20HandlerInterceptorAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuthWeb")
class OAuth20HandlerInterceptorAdapterTests extends AbstractOAuth20Tests {

    @Test
    void verifyAuthorizationAuth() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val clientId = UUID.randomUUID().toString();
        request.setRequestURI('/' + OAuth20Constants.AUTHORIZE_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, clientId);
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        request.setParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org");
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());

        val service = getRegisteredService("https://oauth.example.org", clientId, CLIENT_SECRET);
        servicesManager.save(service);
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
        assertFalse(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());

        request.removeAllParameters();
        assertTrue(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(OAuth20Constants.INVALID_REQUEST, context.getRequestAttribute(OAuth20Constants.ERROR).get().toString());
    }

    @Test
    void verifyRevocationNeedsAuthn() throws Throwable {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.setRequestURI('/' + OAuth20Constants.REVOCATION_URL);
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRequestURI('/' + OAuth20Constants.REVOCATION_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, "unknown123456");
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }

    @Test
    void verifyRevocationAuth() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val clientId = UUID.randomUUID().toString();
        request.setRequestURI('/' + OAuth20Constants.REVOCATION_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, clientId);

        val service = getRegisteredService(clientId, CLIENT_SECRET);
        servicesManager.save(service);
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }

    @Test
    void verifyAccessToken() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.setRequestURI('/' + OAuth20Constants.ACCESS_TOKEN_URL);
        request.setParameter(OAuth20Constants.ACCESS_TOKEN, getAccessToken().getId());
        val id = UUID.randomUUID().toString();
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, id);
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }

    @Test
    void verifyProfile() throws Throwable {
        val clientId = UUID.randomUUID().toString();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.setRequestURI('/' + OAuth20Constants.PROFILE_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, clientId);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
        assertTrue(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }
}
