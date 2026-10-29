package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.configuration.model.support.oauth.OAuthCsrfCookieProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link OAuthCsrfCookieProperties} class.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
@Tag("OAuthWeb")
@TestPropertySource(properties = {
    "cas.authn.oauth.csrf-cookie.max-age=3600",
    "cas.authn.oauth.csrf-cookie.path=/cas",
    "cas.authn.oauth.csrf-cookie.same-site-policy=None",
    "cas.authn.oauth.csrf-cookie.http-only=true",
    "cas.authn.oauth.csrf-cookie.secure=true",
    "cas.authn.oauth.csrf-cookie.domain=mellon.edu"
})
class OAuth20CSRFCookieTests extends AbstractOAuth20Tests {

    @Test
    void verifyPropertiesSet() {
        val csrfCookie = casProperties.getAuthn().getOauth().getCsrfCookie();
        assertEquals("3600", csrfCookie.getMaxAge());
        assertEquals("/cas", csrfCookie.getPath());
        assertEquals("None", csrfCookie.getSameSitePolicy());
        assertEquals("mellon.edu", csrfCookie.getDomain());
        assertTrue(csrfCookie.isHttpOnly());
        assertTrue(csrfCookie.isSecure());
    }

    @Test
    void verifyNoCookieIssuedForLogin() throws Throwable {
        val registeredService = addRegisteredService();
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.AUTHORIZE_URL);
        request.setParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
        val response = performOAuthRequest(request).getResponse();
        assertEquals(HttpStatus.FOUND.value(), response.getStatus());
        assertTrue(Arrays.stream(response.getCookies()).noneMatch(cookie -> "mellon.edu".equals(cookie.getDomain())));
        assertNull(response.getCookie("pac4jCsrfToken"));
    }
}
