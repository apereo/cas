package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcHandlerInterceptorAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDCWeb")
class OidcHandlerInterceptorAdapterTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-enabled=false")
    class DisabledRegistrationTests extends AbstractOidcTests {
        @ParameterizedTest
        @ValueSource(strings = {"%72egister", "%63lientConfig"})
        void verifyEncodedRegistrationEndpointsRemainDisabled(final String endpoint) throws Throwable {
            val request = new MockHttpServletRequest();
            request.setRequestURI("/cas/oidc/" + endpoint);
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
            assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), response.getStatus());
        }
    }

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
        @ParameterizedTest
        @ValueSource(strings = {
            "%6FidcAuthorize", "%6FidcAccessToken", "%6FidcToken", "%6FidcCiba",
            "%6FidcVcCredentialOfferTransactions", "%6FidcVcPresentationRequest",
            "%6FidcPushAuthorize", "%72egister", "%63lientConfig"
        })
        void verifyEncodedEndpointPathsRequireAuthentication(final String endpoint) throws Throwable {
            val clientId = UUID.randomUUID().toString();
            servicesManager.save(getOidcRegisteredService(clientId));

            val request = new MockHttpServletRequest();
            request.setRequestURI("/cas/oidc/" + endpoint);
            request.setMethod(HttpMethod.POST.name());
            request.setParameter(OAuth20Constants.CLIENT_ID, clientId);
            request.setParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org");
            request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            OidcConstants.CIBA_URL, OidcConstants.PUSHED_AUTHORIZE_URL, OidcConstants.REVOCATION_URL,
            OidcConstants.VC_CREDENTIAL_OFFER_TRANSACTIONS_URL, OidcConstants.VC_PRESENTATION_REQUEST_URL
        })
        void verifyBearerAccessTokenIsNotClientAuthentication(final String endpoint) throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);
            val accessToken = getAccessToken(registeredService.getClientId());
            ticketRegistry.addTicket(accessToken);

            val request = new MockHttpServletRequest();
            request.setRequestURI("/cas/oidc/" + endpoint);
            request.setMethod(HttpMethod.POST.name());
            request.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
            request.setParameter(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/");
            request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
            request.setParameter(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope());
            request.setParameter(OAuth20Constants.TOKEN, accessToken.getId());
            request.addHeader(HttpHeaders.AUTHORIZATION, OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getId());
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        }

        @ParameterizedTest
        @ValueSource(strings = {OidcConstants.REGISTRATION_URL, OidcConstants.CLIENT_CONFIGURATION_URL})
        void verifySenderConstrainedAccessTokenIsNotBearerToken(final String endpoint) throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);
            val accessToken = getAccessToken(registeredService.getClientId(),
                Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE, OidcConstants.CLIENT_CONFIGURATION_SCOPE));
            val authentication = DefaultAuthenticationBuilder.newInstance(accessToken.getAuthentication())
                .addAttribute(OAuth20Constants.DPOP_CONFIRMATION, "thumbprint").build();
            when(accessToken.getAuthentication()).thenReturn(authentication);
            ticketRegistry.addTicket(accessToken);

            val request = newBearerTokenRequest(endpoint, registeredService.getClientId(),
                OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getId());
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(request, response, new Object()));
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
            assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"invalid_token\""));
        }

        @ParameterizedTest
        @ValueSource(strings = {OidcConstants.REGISTRATION_URL, OidcConstants.CLIENT_CONFIGURATION_URL})
        void verifyBearerTokenProtectedEndpoints(final String endpoint) throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);
            val accessToken = getAccessToken(registeredService.getClientId(),
                Set.of(OidcConstants.CLIENT_REGISTRATION_SCOPE, OidcConstants.CLIENT_CONFIGURATION_SCOPE));
            ticketRegistry.addTicket(accessToken);

            var response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(newBearerTokenRequest(endpoint, registeredService.getClientId(), null), response, new Object()));
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
            assertEquals(OAuth20Constants.TOKEN_TYPE_BEARER, response.getHeader(HttpHeaders.WWW_AUTHENTICATE));

            response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(newBearerTokenRequest(endpoint, registeredService.getClientId(),
                OAuth20Constants.TOKEN_TYPE_BEARER + " AT-unknown"), response, new Object()));
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
            assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"invalid_token\""));

            response = new MockHttpServletResponse();
            val request = newBearerTokenRequest(endpoint, registeredService.getClientId(), "bearer " + accessToken.getId());
            assertTrue(oauthInterceptor.preHandle(request, response, new Object()));
            assertNotNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
        }

        @Test
        void verifyClientConfigurationTokenOfAnotherClient() throws Throwable {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);
            val accessToken = getAccessToken(registeredService.getClientId(), Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            val response = new MockHttpServletResponse();
            assertFalse(oauthInterceptor.preHandle(newBearerTokenRequest(OidcConstants.CLIENT_CONFIGURATION_URL, "other-client",
                OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getId()), response, new Object()));
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
            assertTrue(response.getHeader(HttpHeaders.WWW_AUTHENTICATE).startsWith("Bearer error=\"invalid_token\""));
        }

        private static MockHttpServletRequest newBearerTokenRequest(final String endpoint, final String clientId,
                                                                    final @Nullable String authorization) {
            val request = new MockHttpServletRequest();
            request.setRequestURI("/cas/oidc/" + endpoint);
            request.setMethod(OidcConstants.REGISTRATION_URL.equals(endpoint) ? HttpMethod.POST.name() : HttpMethod.GET.name());
            request.setParameter(OAuth20Constants.CLIENT_ID, clientId);
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            if (authorization != null) {
                request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
            }
            return request;
        }

        @ParameterizedTest
        @ValueSource(strings = {
            OidcConstants.VC_PRESENTATION_REQUEST_URL, OidcConstants.VC_CREDENTIAL_OFFER_URL
        })
        void verifyWalletFacingLookupsRemainPubliclyAccessible(final String endpoint) throws Throwable {
            val request = new MockHttpServletRequest();
            request.setRequestURI("/cas/oidc/" + endpoint + '/' + UUID.randomUUID());
            request.setMethod(HttpMethod.GET.name());
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            val response = new MockHttpServletResponse();
            assertTrue(oauthInterceptor.preHandle(request, response, new Object()));
        }

        @Test
        void verifyNonceEndpointRemainsPubliclyAccessible() throws Throwable {
            val request = new MockHttpServletRequest();
            request.setRequestURI("/cas/oidc/" + OidcConstants.VC_NONCE_URL);
            request.setMethod(HttpMethod.POST.name());
            request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
            val response = new MockHttpServletResponse();
            assertTrue(oauthInterceptor.preHandle(request, response, new Object()));
        }

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
