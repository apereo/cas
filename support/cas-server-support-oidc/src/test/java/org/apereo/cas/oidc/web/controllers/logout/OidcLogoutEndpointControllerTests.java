package org.apereo.cas.oidc.web.controllers.logout;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcLogoutEndpointControllerTests}.
 *
 * @author Julien Huon
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcLogoutEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcLogoutEndpointController")
    protected OidcLogoutEndpointController oidcLogoutEndpointController;

    @Autowired
    @Qualifier("oidcProtocolEndpointConfigurer")
    private ProtocolEndpointWebSecurityConfigurer<Void> oidcProtocolEndpointConfigurer;

    @Test
    public void verifyEndpoints() {
        assertFalse(oidcProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
    }

    @Test
    public void verifyBadEndpointRequest() {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        val mv = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, request, response);
        assertEquals(HttpStatus.NOT_FOUND, mv.getStatusCode());
    }

    @Test
    public void verifyOidcNoLogoutUrls() {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val id = UUID.randomUUID().toString();
        val claims = getClaims(id);
        val oidcRegisteredService = new OidcRegisteredService();
        oidcRegisteredService.setClientId(id);
        servicesManager.save(oidcRegisteredService);

        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, StringUtils.EMPTY,
            idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
    }

    @Test
    public void verifyOidcLogoutWithoutParams() {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();
        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertNull(redirectUrl);
    }

    @Test
    public void verifyOidcLogoutWithStateParam() {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, "abcd1234",
            StringUtils.EMPTY, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertNull(redirectUrl);
    }

    @Test
    public void verifyOidcLogoutWithIdTokenParam() {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, StringUtils.EMPTY,
            idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://oauth.example.org/logout?client_id=clientid", redirectUrl);
    }

    @Test
    public void verifyOidcLogoutWithIdTokenAndStateParams() {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, "abcd1234",
            idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://oauth.example.org/logout?state=abcd1234&client_id=clientid", redirectUrl);
    }

    @Test
    public void verifyOidcLogoutWithIdTokenAndValidPostLogoutRedirectUrlParams() {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal("https://logout", "abcd1234", idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://logout?state=abcd1234&client_id=clientid", redirectUrl);
    }

    @Test
    public void verifyOidcLogoutWithIdTokenAndInvalidPostLogoutRedirectUrlParams() {
        val request = getHttpRequestForEndpoint(OidcConstants.LOGOUT_URL);
        val response = new MockHttpServletResponse();

        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal("https://invalidlogouturl", "abcd1234", idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
        val redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://oauth.example.org/logout?state=abcd1234&client_id=clientid", redirectUrl);
    }

}
