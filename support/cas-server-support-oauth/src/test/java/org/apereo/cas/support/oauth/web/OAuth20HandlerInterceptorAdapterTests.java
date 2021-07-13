package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * This is {@link OAuth20HandlerInterceptorAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20HandlerInterceptorAdapterTests extends AbstractOAuth20Tests {
    @BeforeEach
    public void setup() {
        super.setup();
        servicesManager.deleteAll();
    }

    @Test
    public void verifyAuthorizationAuth() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        request.setRequestURI('/' + OAuth20Constants.AUTHORIZE_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        request.setParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org");
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());

        val service = getRegisteredService("https://oauth.example.org", CLIENT_ID, CLIENT_SECRET);
        servicesManager.save(service);
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
        assertFalse(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());

        request.removeAllParameters();
        assertTrue(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
        assertTrue(context.getRequestAttribute(OAuth20Constants.ERROR).isPresent());
        assertEquals(context.getRequestAttribute(OAuth20Constants.ERROR).get().toString(), OAuth20Constants.INVALID_REQUEST);
    }

    @Test
    public void verifyRevocationNeedsAuthn() throws Exception {
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
    public void verifyRevocationAuth() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.setRequestURI('/' + OAuth20Constants.REVOCATION_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);

        val service = getRegisteredService(CLIENT_ID, CLIENT_SECRET);
        servicesManager.save(service);
        assertFalse(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }

    @Test
    public void verifyAccessToken() throws Exception {
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
    public void verifyProfile() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.setRequestURI('/' + OAuth20Constants.PROFILE_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.DEVICE_CODE.getType());
        assertTrue(oauthHandlerInterceptorAdapter.preHandle(request, response, new Object()));
    }
}
