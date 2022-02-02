package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcHandlerInterceptorAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcHandlerInterceptorAdapterTests {

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.authn.oidc.discovery.require-pushed-authorization-requests=true")
    public class PushedAuthorizationTests extends AbstractOidcTests {
        @Test
        public void verifyAuthzUrl() throws Exception {
            val svc = getOAuthRegisteredService(UUID.randomUUID().toString(), "https://oauth.example.org");
            servicesManager.save(svc);

            val request = new MockHttpServletRequest();
            request.setRequestURI('/' + OidcConstants.AUTHORIZE_URL);
            request.addParameter(OAuth20Constants.CLIENT_ID, svc.getClientId());
            request.addParameter(OAuth20Constants.REDIRECT_URI, svc.getServiceId());
            request.addParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
            request.setMethod(HttpMethod.GET.name());
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
            assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.authn.oidc.dynamic-client-registration-mode=PROTECTED")
    public class DefaultTests extends AbstractOidcTests {
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

        @Test
        public void verifyPushAuthzUrl() throws Exception {
            val request = new MockHttpServletRequest();
            request.setRequestURI('/' + OidcConstants.PUSHED_AUTHORIZE_URL);
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
        }
    }

}
