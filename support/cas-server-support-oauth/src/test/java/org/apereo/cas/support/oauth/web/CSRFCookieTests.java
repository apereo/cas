package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.configuration.model.support.oauth.CsrfCookieProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link CsrfCookieProperties} class.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
@Tag("OAuth")
@TestPropertySource(properties = {
    "cas.authn.oauth.csrf-cookie.max-age=3600",
    "cas.authn.oauth.csrf-cookie.path=/cas",
    "cas.authn.oauth.csrf-cookie.same-site-policy=None",
    "cas.authn.oauth.csrf-cookie.http-only=true",
    "cas.authn.oauth.csrf-cookie.secure=true",
    "cas.authn.oauth.csrf-cookie.domain=mellon.edu"
})
public class CSRFCookieTests extends AbstractOAuth20Tests {

    @Test
    public void verifyPropertiesSet() {
        val csrfCookie = casProperties.getAuthn().getOauth().getCsrfCookie();
        assertEquals(3600, csrfCookie.getMaxAge());
        assertEquals("/cas", csrfCookie.getPath());
        assertEquals("None", csrfCookie.getSameSitePolicy());
        assertEquals("mellon.edu", csrfCookie.getDomain());
        assertTrue(csrfCookie.isHttpOnly());
        assertTrue(csrfCookie.isSecure());
    }
}
