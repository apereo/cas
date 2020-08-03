package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcHandlerInterceptorAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oidc.dynamicClientRegistrationMode=PROTECTED")
public class OidcHandlerInterceptorAdapterTests extends AbstractOidcTests {

    @Test
    public void verifyNothing() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertTrue(oauthInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    public void verifyNoOIDC() throws Exception {
        val request = new MockHttpServletRequest();
        request.setRequestURI('/' + OAuth20Constants.DEVICE_AUTHZ_URL);
        val response = new MockHttpServletResponse();
        assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    public void verifyConfigUrl() throws Exception {
        val request = new MockHttpServletRequest();
        request.setRequestURI('/' + OidcConstants.CLIENT_CONFIGURATION_URL);
        val response = new MockHttpServletResponse();
        assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    public void verifyRegUrl() throws Exception {
        val request = new MockHttpServletRequest();
        request.setRequestURI('/' + OidcConstants.REGISTRATION_URL);
        val response = new MockHttpServletResponse();
        assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
    }

}
