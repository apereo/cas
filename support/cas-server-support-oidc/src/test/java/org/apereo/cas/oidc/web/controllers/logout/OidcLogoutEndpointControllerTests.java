package org.apereo.cas.oidc.web.controllers.logout;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;

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

    @Test
    public void verifyOidcLogoutWithoutParams() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, request, response);
        assertTrue(result instanceof RedirectView);

        val redirectView = (RedirectView) result;
        val redirectUrl = redirectView.getUrl();
        assertEquals("https://cas.example.org:8443/cas/logout", redirectUrl);
    }

    @Test
    public void verifyOidcLogoutWithStateParam() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, "abcd1234", StringUtils.EMPTY, request, response);
        assertTrue(result instanceof RedirectView);

        val redirectView = (RedirectView) result;
        val redirectUrl = redirectView.getUrl();
        assertEquals("https://cas.example.org:8443/cas/logout?state=abcd1234", redirectUrl);
    }

    @Test
    public void verifyOidcLogoutWithIdTokenParam() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, StringUtils.EMPTY, idToken, request, response);
        assertTrue(result instanceof RedirectView);

        val redirectView = (RedirectView) result;
        val redirectUrl = redirectView.getUrl();
        assertEquals("https://cas.example.org:8443/cas/logout?service=https%3A%2F%2Foauth.example.org%2Flogout", redirectUrl);
    }

    @Test
    public void verifyOidcLogoutWithIdTokenAndStateParams() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal(StringUtils.EMPTY, "abcd1234", idToken, request, response);
        assertTrue(result instanceof RedirectView);

        val redirectView = (RedirectView) result;
        val redirectUrl = redirectView.getUrl();
        assertEquals("https://cas.example.org:8443/cas/logout?service=https%3A%2F%2Foauth.example.org%2Flogout%3Fstate%3Dabcd1234&state=abcd1234", redirectUrl);
    }

    @Test
    public void verifyOidcLogoutWithIdTokenAndValidPostLogoutRedirectUrlParams() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal("https://logout", "abcd1234", idToken, request, response);
        assertTrue(result instanceof RedirectView);

        val redirectView = (RedirectView) result;
        val redirectUrl = redirectView.getUrl();
        assertEquals("https://cas.example.org:8443/cas/logout?service=https%3A%2F%2Flogout%3Fstate%3Dabcd1234&state=abcd1234", redirectUrl);
    }

    @Test
    public void verifyOidcLogoutWithIdTokenAndInvalidPostLogoutRedirectUrlParams() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        val result = oidcLogoutEndpointController.handleRequestInternal("https://invalidlogouturl", "abcd1234", idToken, request, response);
        assertTrue(result instanceof RedirectView);

        val redirectView = (RedirectView) result;
        val redirectUrl = redirectView.getUrl();
        assertEquals("https://cas.example.org:8443/cas/logout?service=https%3A%2F%2Foauth.example.org%2Flogout%3Fstate%3Dabcd1234&state=abcd1234", redirectUrl);
    }
}
