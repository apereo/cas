package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
@Tag("OIDCWeb")
class OidcHandlerInterceptorAdapterTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.discovery.require-pushed-authorization-requests=true")
    class PushedAuthorizationTests extends AbstractOidcTests {
        @Test
        void verifyAuthzUrl() throws Throwable {
            val svc = getOAuthRegisteredService(UUID.randomUUID().toString(), "https://oauth.example.org");
            servicesManager.save(svc);

            val request = new MockHttpServletRequest();
            request.setRequestURI('/' + OidcConstants.AUTHORIZE_URL);
            request.addParameter(OAuth20Constants.CLIENT_ID, svc.getClientId());
            request.addParameter(OAuth20Constants.REDIRECT_URI, svc.getServiceId());
            request.addParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            request.setMethod(HttpMethod.GET.name());
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
            assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-mode=PROTECTED")
    class DefaultTests extends AbstractOidcTests {
        @Test
        void verifyNothing() throws Throwable {
            val request = new MockHttpServletRequest();
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            val response = new MockHttpServletResponse();
            assertTrue(oauthInterceptor.preHandle(request, response, new Object()));
        }

        @Test
        void verifyNoOIDC() throws Throwable {
            val request = new MockHttpServletRequest();
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            request.setRequestURI('/' + OAuth20Constants.DEVICE_AUTHZ_URL);
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
        }

        @Test
        void verifyConfigUrl() throws Throwable {
            val request = new MockHttpServletRequest();
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            request.setRequestURI('/' + OidcConstants.CLIENT_CONFIGURATION_URL);
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
        }

        @Test
        void verifyRegUrl() throws Throwable {
            val request = new MockHttpServletRequest();
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            request.setRequestURI('/' + OidcConstants.REGISTRATION_URL);
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
        }

        @Test
        void verifyPushAuthzUrl() throws Throwable {
            val request = new MockHttpServletRequest();
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            request.setRequestURI('/' + OidcConstants.PUSHED_AUTHORIZE_URL);
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
        }
    }

}
