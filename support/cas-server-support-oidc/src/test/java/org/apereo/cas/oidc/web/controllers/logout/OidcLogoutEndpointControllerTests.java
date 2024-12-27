package org.apereo.cas.oidc.web.controllers.logout;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcLogoutEndpointControllerTests}.
 *
 * @author Julien Huon
 * @since 6.1.0
 */
@Tag("OIDCWeb")
class OidcLogoutEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcLogoutEndpointController")
    protected OidcLogoutEndpointController oidcLogoutEndpointController;

    @Autowired
    @Qualifier("oidcProtocolEndpointConfigurer")
    private CasWebSecurityConfigurer<HttpSecurity> oidcProtocolEndpointConfigurer;

    @Test
    void verifyEndpoints() {
        assertFalse(oidcProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
    }

    @Test
    void verifyBadEndpointRequest() throws Throwable {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        val mv = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, request, response);
        assertEquals(HttpStatus.BAD_REQUEST, mv.getStatusCode());
    }

    @Test
    void verifyOidcNoLogoutUrls() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val id = UUID.randomUUID().toString();
        val claims = getClaims(id);
        val oidcRegisteredService = new OidcRegisteredService();
        oidcRegisteredService.setClientId(id);
        oidcRegisteredService.setServiceId("https://example.org");
        oidcRegisteredService.setName(id);
        servicesManager.save(oidcRegisteredService);

        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, StringUtils.EMPTY,
            idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCode().value());
    }

    @Test
    void verifyOidcLogoutWithoutParams() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();
        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCode().value());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertNull(redirectUrl);
    }

    @Test
    void verifyOidcLogoutWithStateParam() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, "abcd1234",
            StringUtils.EMPTY, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCode().value());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertNull(redirectUrl);
    }

    @Test
    void verifyOidcLogoutWithIdTokenParam() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl(), true, false);
        val claims = getClaims(oidcRegisteredService.getClientId());
        servicesManager.save(oidcRegisteredService);

        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, StringUtils.EMPTY,
            idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCode().value());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://oauth.example.org/logout?client_id=%s".formatted(oidcRegisteredService.getClientId()), redirectUrl);
    }

    @Test
    void verifyOidcLogoutWithIdTokenAndStateParams() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val oidcRegisteredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl(), true, false);
        val claims = getClaims(oidcRegisteredService.getClientId());
        servicesManager.save(oidcRegisteredService);
        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, "abcd1234",
            idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCode().value());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://oauth.example.org/logout?state=abcd1234&client_id=%s".formatted(oidcRegisteredService.getClientId()), redirectUrl);
    }

    @Test
    void verifyOidcLogoutWithIdTokenAndValidPostLogoutRedirectUrlParams() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl(), true, false);
        val claims = getClaims(registeredService.getClientId());
        servicesManager.save(registeredService);
        val idToken = oidcTokenSigningAndEncryptionService.encode(registeredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal("https://logout", "abcd1234", idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCode().value());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://logout?state=abcd1234&client_id=%s".formatted(registeredService.getClientId()), redirectUrl);
    }

    @Test
    void verifyOidcLogoutWithIdTokenAndInvalidPostLogoutRedirectUrlParams() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString(), randomServiceUrl(), true, false);
        val claims = getClaims(registeredService.getClientId());
        servicesManager.save(registeredService);
        val idToken = oidcTokenSigningAndEncryptionService.encode(registeredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal("https://invalidlogouturl", "abcd1234", idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCode().value());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://oauth.example.org/logout?state=abcd1234&client_id=%s".formatted(registeredService.getClientId()), redirectUrl);
    }

}
