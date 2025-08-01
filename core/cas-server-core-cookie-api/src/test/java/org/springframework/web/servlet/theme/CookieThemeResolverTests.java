package org.springframework.web.servlet.theme;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CookieThemeResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Cookie")
class CookieThemeResolverTests {
    @Test
    void verifyThemeResolution() throws Exception {
        val cookieProperties = new CookieProperties().withMaxAge("PT1H");
        val resolver = new CookieThemeResolver(cookieProperties);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        resolver.setThemeName(request, response, "testTheme");
        var cookie = response.getCookie(CookieThemeResolver.DEFAULT_COOKIE_NAME);
        assertNotNull(cookie);
        assertNotEquals(0, cookie.getMaxAge());
        assertEquals("testTheme",
            request.getAttribute(CookieThemeResolver.THEME_REQUEST_ATTRIBUTE_NAME));

        request.setCookies(response.getCookies());

        assertEquals("testTheme", resolver.resolveThemeName(request));
        request.removeAttribute(CookieThemeResolver.THEME_REQUEST_ATTRIBUTE_NAME);
        assertEquals("testTheme", resolver.resolveThemeName(request));

        request.removeAttribute(CookieThemeResolver.THEME_REQUEST_ATTRIBUTE_NAME);
        request.setCookies();
        assertEquals(resolver.getDefaultThemeName(), resolver.resolveThemeName(request));

        response.reset();
        resolver.setThemeName(request, response, StringUtils.EMPTY);
        cookie = response.getCookie(CookieThemeResolver.DEFAULT_COOKIE_NAME);
        assertNotNull(cookie);
        assertEquals(0, cookie.getMaxAge());
        assertEquals(resolver.getDefaultThemeName(),
            request.getAttribute(CookieThemeResolver.THEME_REQUEST_ATTRIBUTE_NAME));

    }
}
