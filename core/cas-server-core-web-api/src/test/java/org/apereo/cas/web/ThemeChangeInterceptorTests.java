package org.apereo.cas.web;

import org.apereo.cas.web.support.ThemeChangeInterceptor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.theme.SessionThemeResolver;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ThemeChangeInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Web")
class ThemeChangeInterceptorTests {

    @Test
    void verifyOperation() throws Exception {
        val themeResolver = new SessionThemeResolver();
        val interceptor = new org.apereo.cas.web.support.ThemeChangeInterceptor(themeResolver, "theme");
        val request = new MockHttpServletRequest();
        request.setParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, "cas");
        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertEquals("cas", themeResolver.resolveThemeName(request));
    }
}
