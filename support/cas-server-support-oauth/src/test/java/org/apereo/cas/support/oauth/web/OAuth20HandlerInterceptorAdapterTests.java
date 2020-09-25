package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20HandlerInterceptorAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20HandlerInterceptorAdapterTests extends AbstractOAuth20Tests {
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
